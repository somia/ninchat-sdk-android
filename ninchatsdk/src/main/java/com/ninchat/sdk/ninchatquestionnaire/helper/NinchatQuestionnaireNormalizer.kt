package com.ninchat.sdk.ninchatquestionnaire.helper

import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter
import org.json.JSONArray
import org.json.JSONObject

class NinchatQuestionnaireNormalizer {
    companion object {
        internal fun simpleFormToGroupQuestionnaire(questionnaireArr: JSONArray?): JSONArray {
            val retval = JSONArray()
            val simpleForm = JSONObject()
            simpleForm.putOpt("name", "SimpleForm")
            simpleForm.putOpt("type", "group")
            simpleForm.putOpt("buttons", JSONObject("""{
                "back": false,
                "next": true
            }""".trimIndent()))
            simpleForm.putOpt("elements", questionnaireArr)
            retval.put(simpleForm)
            retval.put(JSONObject("""{
                "name": "SimpleForm-Logic1",
                "logic": {
                    "target": "_complete"
                }
            }""".trimIndent()))
            return retval
        }

        internal fun makeGroupElement(nonGroupElement: JSONObject): JSONObject {
            val elements = JSONArray()
            elements.put(JSONObject(nonGroupElement.toString()))

            val retval = JSONObject(nonGroupElement.toString())
            retval.putOpt("elements", elements)
            retval.putOpt("type", "group")
            return retval
        }

        internal fun makeLogicElement(redirectElement: JSONObject, elementName: String, logicIndex: Int): JSONObject {
            val andLogic = JSONObject()
            andLogic.put(elementName, redirectElement.optString("pattern"))

            val andLogicList = JSONArray()
            andLogicList.put(andLogic)
            val logic = JSONObject()
            logic.putOpt("target", redirectElement.optString("target"))

            // only add logic if it has pattern
            if (redirectElement.has("pattern")) {
                logic.putOpt("and", andLogicList)
            }
            val retval = JSONObject()
            retval.putOpt("name", "$elementName-Logic$logicIndex")
            retval.putOpt("logic", logic)
            return retval
        }

        internal fun redirectToLogicElement(elements: JSONObject, logicIndex: Int): List<JSONObject> {
            val name = elements.optString("name")
            val redirectList = elements.optJSONArray("redirects")
            return fromJSONArray<JSONObject>(redirectList)
                    .map { makeLogicElement(redirectElement = it as JSONObject, elementName = name, logicIndex = logicIndex) }
        }

        internal fun updateActions(questionnaireList: List<JSONObject>): List<JSONObject> {
            return questionnaireList.mapIndexedNotNull { index, currentElement ->
                val elementList = currentElement.optJSONArray("elements")
                if (NinchatQuestionnaireType.isLogic(currentElement) || elementList == null) {
                    null
                } else {
                    val hasBackButton = NinchatQuestionnaireType.isButton(json = currentElement, isBack = true)
                    val hasNextButton = NinchatQuestionnaireType.isButton(json = currentElement, isBack = false)
                    if (hasBackButton || hasNextButton) {
                        // next button always true hard coded
                        val tempElement = NinchatQuestionnaireJsonUtil.getButtonElement(json = currentElement, hideBack = index == 0, forceNext = true)
                        elementList.put(tempElement)
                    } else {
                        // add event fire capability to last element if it is not an text, input, or
                        val tempElement = elementList.optJSONObject(elementList.length() - 1)
                        if (NinchatQuestionnaireType.isText(tempElement) ||
                                NinchatQuestionnaireType.isInput(tempElement) ||
                                NinchatQuestionnaireType.isTextArea(tempElement)) {
                            val tempBtnElement = NinchatQuestionnaireItemGetter.getButtonElement(true)
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
            return questionnaireList.mapNotNull { currentElement ->
                val elementList = currentElement.optJSONArray("elements")
                if (NinchatQuestionnaireType.isLogic(currentElement) || elementList == null) null
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
            return retval
                    .plus(logicElement)
                    .also {
                        updateActions(questionnaireList = it)
                    }
                    .also {
                        updateLikeRTElements(questionnaireList = it)
                    }
        }
    }
}