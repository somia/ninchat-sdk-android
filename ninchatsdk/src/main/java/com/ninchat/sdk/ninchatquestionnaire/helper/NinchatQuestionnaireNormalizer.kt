package com.ninchat.sdk.ninchatquestionnaire.helper

import org.json.JSONArray
import org.json.JSONObject

class NinchatQuestionnaireNormalizer {
    companion object {
        internal fun simpleFormToGroupQuestionnaire(questionnaireArr: JSONArray?): JSONArray {
            return JSONArray("""[
                {
                    "name": "SimpleForm",
                    "type": "group",
                    "buttons": {
                        "back": false,
                        "next": true
                    },
                    "elements": ${questionnaireArr?.toString(2)}
                },
                {
                    "name": "SimpleForm-Logic1",
                    "logic": {
                        "target": "_complete"
                    }
                }
            ]""".trimIndent())
        }

        internal fun makeGroupElement(nonGroupElement: JSONObject): JSONObject {
            return JSONObject(nonGroupElement.toString())
                    .apply {
                        putOpt("elements", JSONArray("""[
                            ${nonGroupElement.toString(2)}
                        ]""".trimIndent()))
                        putOpt("type", "group")
                    }
        }

        internal fun makeLogicElement(redirectElement: JSONObject, elementName: String, logicIndex: Int): JSONObject {
            val logic = JSONObject("""{
                        "target": ${redirectElement.optString("target")}   
                    }""".trimIndent())
                    .apply {
                        // only add logic if it has pattern
                        if (redirectElement.has("pattern")) {
                            putOpt("and", """[{
                                "$elementName": ${redirectElement.optString("pattern")}
                            }]""".trimMargin())
                        }
                    }
            return JSONObject("""{
                "name": "${elementName}-logic:${logicIndex}",
                "logic": ${logic.toString(2)}
            }""".trimIndent())
        }

        internal fun redirectToLogicElement(elements: JSONObject, logicIndex: Int): List<JSONObject> {
            val name = elements.optString("name")
            val redirectList = elements.optJSONArray("redirects")
            return fromJSONArray<JSONObject>(redirectList)
                    .map { makeLogicElement(redirectElement = it as JSONObject, elementName = name, logicIndex = logicIndex) }
        }

        internal fun updateActions(questionnaireList: List<JSONObject>): List<JSONObject> {
            return questionnaireList.mapIndexed { index, currentElement ->
                val elementList = currentElement.optJSONArray("elements")
                if (NinchatQuestionnaireType.isLogic(currentElement) || elementList == null) {
                    currentElement
                } else {
                    val hasBackButton = NinchatQuestionnaireType.isButton(json = currentElement, isBack = true)
                    val hasNextButton = NinchatQuestionnaireType.isButton(json = currentElement, isBack = false)
                    if (hasBackButton || hasNextButton) {
                        // next button always true hard coded
                        val tempElement = NinchatQuestionnaireJsonUtil.getButtonElement(json = currentElement, hideBack = index == 0)
                        elementList.put(tempElement)
                    } else {
                        // add event fire capability to last element if it is not an text, input, or
                        val tempElement = elementList.optJSONObject(elementList.length() - 1)
                        if (NinchatQuestionnaireType.isText(tempElement) ||
                                NinchatQuestionnaireType.isInput(tempElement) ||
                                NinchatQuestionnaireType.isTextArea(tempElement)) {
                            val tempBtnElement = NinchatQuestionnaireJsonUtil.getButtonElement(json = currentElement, hideBack = index == 0)
                            elementList.put(tempBtnElement)
                        } else {
                            tempElement.putOpt("fireEvent", true)
                        }
                    }
                    currentElement
                }
            }
        }

        internal fun updateLikeRTElements(questionnaireList: List<JSONObject>): List<JSONObject> {
            return questionnaireList.map { currentElement ->
                val elementList = currentElement.optJSONArray("elements")
                if (NinchatQuestionnaireType.isLogic(currentElement) || elementList == null) currentElement
                else {
                    fromJSONArray<JSONObject>(elementList).forEach {
                        val likeRTElement = it as JSONObject
                        if (NinchatQuestionnaireType.isLikeRT(likeRTElement)) {
                            likeRTElement.putOpt("options", NinchatQuestionnaireJsonUtil.getLikeRTOptions())
                        }
                    }
                    currentElement
                }
            }
        }

        fun unifyQuestionnaireList(questionnaireArr: JSONArray?): List<JSONObject> {
            val questionnaireList = if (NinchatQuestionnaireType.isSimpleFormLikeQuestionnaire(questionnaireArr)) {
                simpleFormToGroupQuestionnaire(questionnaireArr = questionnaireArr)
            } else
                questionnaireArr
            // convert everything to group element
            val retval = fromJSONArray<JSONObject>(questionnaireList = questionnaireList)
                    .map {
                        val currentElement = it as JSONObject
                        when {
                            NinchatQuestionnaireType.isGroupElement(currentElement) -> currentElement
                            NinchatQuestionnaireType.isLogic(currentElement) -> currentElement
                            else ->
                                makeGroupElement(currentElement)
                        }
                    }
            // convert any redirect to logic element
            val logicElement = retval
                    .mapIndexedNotNull { index, currentElement ->
                        val redirectList = redirectToLogicElement(elements = currentElement, logicIndex = index)
                        if (redirectList.isNullOrEmpty()) {
                            null
                        } else {
                            redirectList
                        }
                    }.flatten()

            return retval.plus(logicElement)
                    .also { updateActions(it) }
                    .also { updateLikeRTElements(it) }
        }
    }
}