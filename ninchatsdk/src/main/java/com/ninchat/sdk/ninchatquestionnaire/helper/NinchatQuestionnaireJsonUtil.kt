package com.ninchat.sdk.ninchatquestionnaire.helper

import android.text.InputType
import org.json.JSONArray
import org.json.JSONObject

inline fun <reified T> fromJSONArray(questionnaireList: JSONArray?): List<Any> {
    val klass = T::class
    val retval = arrayListOf<Any>()
    return questionnaireList?.let { currentList ->
        for (i in 0 until currentList.length()) {
            when (klass) {
                Int::class -> {
                    val currentElement = currentList.optJSONObject(i)
                    retval.add(currentElement)
                }
                Boolean::class -> {
                    val currentElement = currentList.optBoolean(i)
                    retval.add(currentElement)
                }
                String::class -> {
                    val currentElement = currentList.optString(i)
                    retval.add(currentElement)
                }
                JSONObject::class -> {
                    val currentElement = currentList.optJSONObject(i)
                    retval.add(currentElement)
                }
                JSONArray::class -> {
                    val currentElement = currentList.optJSONArray(i)
                    retval.add(currentElement)
                }
                Double::class -> {
                    val currentElement = currentList.optDouble(i)
                    retval.add(currentElement)
                }
                Long::class -> {
                    val currentElement = currentList.optLong(i)
                    retval.add(currentElement)
                }
            }
        }
        retval
    } ?: retval
}

fun toJSONArray(questionnaireList: List<Any>?): JSONArray {
    val retval = JSONArray()
    questionnaireList?.forEach {
        retval.put(it)
    }
    return retval
}


class NinchatQuestionnaireJsonUtil {

    companion object {

        fun getThankYouElement(thankYouString: String): JSONObject {
            return JSONObject("""{
                    "name": "ThankYouForm",
                    "type": "group",
                    "buttons": {
                      "element": "buttons",
                      "fireEvent": true,
                      "back": false,
                      "next": "Close chat",
                      "type": "thankYouText"
                    },
                    "elements": [
                      {
                        "element": "text",
                        "name": "ThankYouText",
                        "label": "$thankYouString"
                      },
                      {
                        "element": "buttons",
                        "fireEvent": true,
                        "back": false,
                        "next": "Close chat",
                        "type": "thankYouText"
                      }
                    ]
            }""".trimIndent())
        }

        fun getInputType(json: JSONObject?): Int {
            return json?.optString(NinchatQuestionnaireConstants.inputMode)?.let {
                when {
                    it.contains("text") -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                    it.contains("tel") -> InputType.TYPE_CLASS_PHONE
                    it.contains("email") -> InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
                    it.contains("numeric") -> InputType.TYPE_CLASS_NUMBER
                    it.contains("decimal") -> InputType.TYPE_NUMBER_FLAG_DECIMAL
                    it.contains("url") -> InputType.TYPE_TEXT_VARIATION_URI
                    else -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                }
            } ?: InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }


        fun getButtonElement(json: JSONObject?, hideBack: Boolean): JSONObject {
            val hasBackButton = if (hideBack) false else NinchatQuestionnaireType.isButton(json = json?.optJSONObject(NinchatQuestionnaireConstants.buttons), isBack = true)
            val hasNextButton = NinchatQuestionnaireType.isButton(json = json?.optJSONObject(NinchatQuestionnaireConstants.buttons), isBack = false)

            val buttonMap = mapOf(
                    NinchatQuestionnaireConstants.element to NinchatQuestionnaireConstants.buttons,
                    NinchatQuestionnaireConstants.fireEvent to true,
                    NinchatQuestionnaireConstants.back to if (hasBackButton) json?.optJSONObject(NinchatQuestionnaireConstants.buttons)?.optString("back") else false,
                    NinchatQuestionnaireConstants.next to if (hasNextButton) json?.optJSONObject(NinchatQuestionnaireConstants.buttons)?.optString("next") else true,
            )
            return JSONObject(buttonMap)
        }


        fun getLikeRTOptions(): JSONArray {
            val optionList = """[
                {"label":"Strongly disagree","value":"strongly_disagree"},
                {"label":"Disagree","value":"disagree"},
                {"label":"Neither agree nor disagree","value":"neither_agree_nor_disagree"},
                {"label":"Agree","value":"agree"},
                {"label":"Strongly agree","value":"strongly_agree"}
            ]""".trimIndent()
            return JSONArray(optionList)
        }

        fun hasLogic(logicElement: JSONObject?, isAnd: Boolean): Boolean {
            // get all and or or logic list
            fromJSONArray<JSONObject>(logicElement?.optJSONArray(if (isAnd) "and" else "or")).forEach {
                val currentLogic = it as JSONObject
                // iterate throw the keys and see any key is no empty
                currentLogic.keys().forEach { currentKey: String ->
                    if (currentKey.isEmpty().not()) {
                        return true
                    }
                }
            }
            return false
        }

        fun matchPattern(currentInput: String?, pattern: String?): Boolean {
            return when {
                pattern.isNullOrEmpty() -> true
                currentInput ?: "" == pattern ?: "" -> true
                (currentInput ?: "").matches((pattern ?: "").toRegex()) -> true
                else ->
                    false
            }
        }

        fun matchedLogicAll(logicList: JSONArray?, answerList: List<JSONObject>): Boolean {
            return fromJSONArray<JSONObject>(logicList).all {
                val currentLogic = it as JSONObject
                var found = false
                currentLogic.keys().forEach { currentKey: String ->
                    val pattern = currentLogic.optString(currentKey)
                    found = found or answerList.any { currentAnswer: JSONObject ->
                        val result = currentAnswer.optString("result")
                        matchPattern(currentInput = result, pattern = pattern)
                    }
                }
                found
            }
        }

        fun matchedLogicAny(logicList: JSONArray?, answerList: List<JSONObject>): Boolean {
            return fromJSONArray<JSONObject>(logicList).any {
                val currentLogic = it as JSONObject
                var found = false
                currentLogic.keys().forEach { currentKey: String ->
                    val pattern = currentLogic.optString(currentKey)
                    found = found or answerList.any { currentAnswer: JSONObject ->
                        val result = currentAnswer.optString("result")
                        matchPattern(currentInput = result, pattern = pattern)
                    }
                }
                found
            }
        }

        fun getLogicByName(questionnaireList: List<JSONObject>, logicName: String): List<JSONObject> {
            return questionnaireList
                    // is a logic element
                    .filter { (it as JSONObject).has(NinchatQuestionnaireConstants.logic) }
                    // if the name of the element start with given logic name or Logic-name prefix
                    .filter {
                        val currentElement = it as JSONObject
                        val name = currentElement.optString(NinchatQuestionnaireConstants.name, "")
                        name.startsWith(logicName) || name.startsWith("logic$logicName") || name.startsWith("Logic-$logicName")
                    }
                    // convert it to json list
                    .map {
                        it as JSONObject
                    }
        }

        fun getMatchingLogic(questionnaireList: List<JSONObject>, elementName: String, answerList: List<JSONObject>): JSONObject? {
            val logicList = getLogicByName(questionnaireList = questionnaireList, logicName = elementName)
            return logicList.firstOrNull {
                val currentLogic = it.optJSONObject("logic")
                val hasAndLogic = hasLogic(logicElement = currentLogic, isAnd = true)
                val hasOrLogic = hasLogic(logicElement = currentLogic, isAnd = false)
                when {
                    // if does not have both and or or logic then it a direct match
                    !hasAndLogic && !hasOrLogic -> {
                        true
                    }
                    matchedLogicAll(logicList = currentLogic?.optJSONArray("and"), answerList = answerList) ->
                        true
                    matchedLogicAny(logicList = currentLogic?.optJSONArray("or"), answerList = answerList) ->
                        true
                    else ->
                        false
                }
            }
        }

        fun requiredOk(json: JSONObject): Boolean {
            val isRequired = json.optBoolean("required", false)
            if (!isRequired) return true
            return when {
                NinchatQuestionnaireType.isInput(json) ||
                        NinchatQuestionnaireType.isTextArea(json) ||
                        NinchatQuestionnaireType.isSelect(json) ||
                        NinchatQuestionnaireType.isLikeRT(json) ||
                        NinchatQuestionnaireType.isRadio(json) -> {
                    !json.optString("result").isNullOrBlank()
                }
                NinchatQuestionnaireType.isCheckBox(json) -> {
                    json.optBoolean("result", false)
                }
                else -> {
                    true
                }
            }
        }

        fun matchPattern(json: JSONObject): Boolean {
            val hasPattern = json.has("pattern")
            if (!hasPattern) return true
            return when {
                NinchatQuestionnaireType.isInput(json) ||
                        NinchatQuestionnaireType.isTextArea(json) ||
                        NinchatQuestionnaireType.isSelect(json) ||
                        NinchatQuestionnaireType.isLikeRT(json) ||
                        NinchatQuestionnaireType.isRadio(json) -> {
                    matchPattern(currentInput = json.optString("result"),
                            pattern = json.optString("pattern"))
                }
                else -> {
                    true
                }
            }
        }

        fun hasError(answerList: List<JSONObject>, selectedElement: Pair<String, Int>): Boolean {
            return answerList.takeLast(selectedElement.second).any {
                // is required but there is no result
                !requiredOk(json = it) || !matchPattern(json = it)
            }
        }

        fun updateError(answerList: List<JSONObject>, selectedElement: Pair<String, Int>): List<JSONObject> {
            answerList.takeLast(selectedElement.second).forEach {
                // is required but there is no result
                if (!requiredOk(json = it) || !matchPattern(json = it)) {
                    it.putOpt("hasError", true)
                }
            }
            return answerList
        }

        fun slowCopy(json: JSONObject): JSONObject =
                JSONObject(json.toString(2))
    }
}