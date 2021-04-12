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
                    if (hasBackButton || hasNextButton || !currentElement.has("buttons") /* or does not have "buttons" element */) {
                        // next button hard coded for mentioned scenarios
                        val tempElement = NinchatQuestionnaireJsonUtil.getButtonElement(json = currentElement, hideBack = index == 0)
                        elementList.put(tempElement)
                    } else {
                        //otherwise add event fire capability to each element if it is an navigation like element
                        fromJSONArray<JSONObject>(elementList).map {
                            if (
                                NinchatQuestionnaireType.isButton(it as JSONObject) ||
                                NinchatQuestionnaireType.isCheckBox(it) ||
                                NinchatQuestionnaireType.isRadio(it) ||
                                NinchatQuestionnaireType.isSelect(it) ||
                                NinchatQuestionnaireType.isLikeRT(it) )
                                {
                                    it.putOpt("fireEvent", true)
                            }
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

        internal fun getCheckboxGroupElement(elementList: List<JSONObject>): List<JSONObject> {
            // get first checkbox element index
            val index = elementList.indexOfFirst { NinchatQuestionnaireType.isCheckBox(jsonObject = it) }
            if (index == -1) return elementList

            // Pick all consecutive checkbox element
            val checkBoxElementList = elementList
                    .subList(fromIndex = index, toIndex = elementList.size)
                    .takeWhile { NinchatQuestionnaireType.isCheckBox(jsonObject = it) }
                    .map {
                        // default value as false
                        it.putOpt("result", false)
                    }
            // Make a group element from checkbox consecutive checkbox element
            val checkboxGroupElement = NinchatQuestionnaireJsonUtil.getCheckboxElements(elementList = checkBoxElementList, index = index)
            return elementList.mapIndexedNotNull { currentIndex, jsonObject ->
                when {
                    // replace matched checkbox element with group checkbox element
                    currentIndex == index -> checkboxGroupElement
                    currentIndex > index && currentIndex < index + checkBoxElementList.size -> null
                    else ->
                        jsonObject
                }
            }
        }

        internal fun updateCheckBoxElements(questionnaireList: List<JSONObject>): List<JSONObject> {
            return questionnaireList.map { currentElement ->
                val elementList = currentElement.optJSONArray("elements")
                if (NinchatQuestionnaireType.isLogic(currentElement) || elementList == null) currentElement
                else {
                    val elementList = getCheckboxGroupElement(elementList = fromJSONArray<JSONObject>(elementList).map { it as JSONObject })
                    currentElement.putOpt("elements", toJSONArray(questionnaireList = elementList))
                }
                currentElement
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