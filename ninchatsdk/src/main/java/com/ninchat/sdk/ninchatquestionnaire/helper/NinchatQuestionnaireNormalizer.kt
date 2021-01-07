package com.ninchat.sdk.ninchatquestionnaire.helper

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.log

class NinchatQuestionnaireNormalizer {
    companion object {
        internal fun simpleFormToGroupQuestionnaire(questionnaireArr: JSONArray?): JSONArray {
            return JSONArray()
                    .apply {
                        put(JSONObject("""{
                            "name": "SimpleForm",
                            "type": "group",
                            "buttons": {
                                "back": false,
                                "next": true
                            }
                        }""".trimIndent()).apply {
                            putOpt("elements", questionnaireArr)
                        })
                        put(JSONObject("""{
                            "name": "SimpleForm-Logic1",
                            "logic": {
                                "target": "_complete"
                            }
                        }""".trimIndent()))
                    }
        }

        internal fun makeGroupElement(nonGroupElement: JSONObject): JSONObject {
            return JSONObject(nonGroupElement.toString())
                    .apply {
                        putOpt("elements", JSONArray().apply {
                            put(JSONObject(nonGroupElement.toString()))
                        })
                        putOpt("type", "group")
                    }
        }

        internal fun makeLogicElement(redirectElement: JSONObject, elementName: String, logicIndex: Int): JSONObject {
            val andLogic = JSONObject().apply {
                putOpt(elementName, redirectElement.optString("pattern"))
            }
            val andLogicList = JSONArray().apply {
                put(andLogic)
            }
            val logic = JSONObject().apply {
                putOpt("target", redirectElement.optString("target"))
                if (redirectElement.has("pattern")) {
                    putOpt("and", andLogicList)
                }
            }
            return JSONObject().apply {
                putOpt("name", "${elementName}-logic:${logicIndex}")
                putOpt("logic", logic)
            }
        }

        internal fun redirectToLogicElement(elements: JSONObject): List<JSONObject> {
            val name = elements.optString("name")
            val redirectList = elements.optJSONArray("redirects")
            return fromJSONArray<JSONObject>(redirectList)
                    .mapIndexed { index, it -> makeLogicElement(redirectElement = it as JSONObject, elementName = name, logicIndex = index) }
        }

        internal fun updateActions(questionnaireList: List<JSONObject>): List<JSONObject> {
            return questionnaireList.mapIndexed { index, currentElement ->
                val elementList = currentElement.optJSONArray("elements")
                if (NinchatQuestionnaireType.isLogic(currentElement) || elementList == null) {
                    currentElement
                } else {
                    val hasBackButton = NinchatQuestionnaireType.isButton(json = currentElement.optJSONObject("buttons"), isBack = true)
                    val hasNextButton = NinchatQuestionnaireType.isButton(json = currentElement.optJSONObject("buttons"), isBack = false)
                    if (hasBackButton || hasNextButton || index == 0 /* or first element */) {
                        // next button always true hard coded
                        val tempElement = NinchatQuestionnaireJsonUtil.getButtonElement(json = currentElement, hideBack = index == 0)
                        elementList.put(tempElement)
                    } else {
                        // add event fire capability to last element if it is not an text, input, or
                        val tempElement = elementList.optJSONObject(elementList.length() - 1)
                        if (NinchatQuestionnaireType.isText(tempElement) ||
                                NinchatQuestionnaireType.isInput(tempElement) ||
                                NinchatQuestionnaireType.isCheckBox(tempElement) ||
                                NinchatQuestionnaireType.isTextArea(tempElement)) {
                            val tempBtnElement = NinchatQuestionnaireJsonUtil.getButtonElement(json = currentElement, hideBack = index == 0)
                            elementList.put(tempBtnElement)
                        } else {
                            tempElement?.putOpt("fireEvent", true)
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

        internal fun updateCheckBoxElements(questionnaireList: List<JSONObject>): List<JSONObject> {
            return questionnaireList.map { currentElement ->
                val elementList = currentElement.optJSONArray("elements")
                if (NinchatQuestionnaireType.isLogic(currentElement) || elementList == null) currentElement
                else {
                    fromJSONArray<JSONObject>(elementList).forEach {
                        val checkBoxElement = it as JSONObject
                        if (NinchatQuestionnaireType.isCheckBox(checkBoxElement)) {
                            checkBoxElement.putOpt("result", false)
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
                            NinchatQuestionnaireType.isRedirect(currentElement) -> {
                                val logicList = redirectToLogicElement(elements = currentElement)
                                listOf(currentElement).plus(logicList)
                            }
                            else ->
                                listOf(currentElement)
                        }
                    }
                    .flatten()
                    .map {
                        val currentElement = it as JSONObject
                        when {
                            NinchatQuestionnaireType.isGroupElement(currentElement) -> currentElement
                            NinchatQuestionnaireType.isLogic(currentElement) -> currentElement
                            else ->
                                makeGroupElement(currentElement)
                        }
                    }

            return retval
                    .also { updateActions(it) }
                    .also { updateLikeRTElements(it) }
                    .also { updateCheckBoxElements(it) }
        }

        fun sanitizeString(text: String?): String? {
            return text?.let {
                text.replace("^[\\n\\r]".toRegex(), "")
                        .replace("[\n\r]$".toRegex(), "").trim();
            }
        }
    }
}