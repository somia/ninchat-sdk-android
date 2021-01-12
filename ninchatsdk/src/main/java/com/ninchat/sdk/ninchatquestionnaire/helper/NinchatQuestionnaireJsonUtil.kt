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

        fun getBotElement(botName: String?, botImgUrl: String?, targetElement: String?, thankYouText: String?): JSONObject {
            val element = JSONObject("""{
                "name": "botViewElement",
                "element": "botElement",
                "label": "$botName",
                "imgUrl": "$botImgUrl",
                "target": "$targetElement",
                "thankYouText": "$thankYouText"    
            }""".trimMargin()).apply {
                putOpt("loaded", false)
            }

            return element.apply {
                putOpt("elements", JSONArray().apply {
                    put(JSONObject(element.toString(2)))
                })
            }
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

        fun getCheckboxElements(elementList: List<JSONObject>, index: Int): JSONObject {
            val name = if (elementList.size == 1) elementList.first().optString("name") else "customCheckbox - $index"
            val jsonObject = JSONObject("""{
                "name": "$name",
                "type": "group",
                "element": "checkbox"
            }""".trimMargin()).apply {
                putOpt("options", toJSONArray(questionnaireList = elementList))
            }
            return jsonObject
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

        internal fun matchedLogicAll(logicList: JSONArray?, answerList: List<JSONObject>): Boolean {
            return fromJSONArray<JSONObject>(logicList).all { logic ->
                val currentLogic = logic as JSONObject
                var found = false
                currentLogic.keys().forEach { currentKey: String ->
                    val pattern = currentLogic.optString(currentKey)
                    found = found or answerList
                            .map { currentElement ->
                                when {
                                    NinchatQuestionnaireType.isCheckBox(currentElement) -> {
                                        fromJSONArray<JSONObject>(currentElement.optJSONArray("options"))
                                                .map { element -> element as JSONObject }
                                    }
                                    else -> listOf(currentElement)
                                }
                            }
                            .flatten()
                            .asReversed()
                            .filter { currentAnswer ->
                                currentKey == currentAnswer.optString("name")
                            }
                            .distinctBy { currentAnswer ->
                                currentAnswer.optString("name")
                            }
                            .any { currentAnswer: JSONObject ->
                                val result = currentAnswer.optString("result")
                                matchPattern(currentInput = result, pattern = pattern)
                            }
                }
                found
            }
        }

        internal fun matchedLogicAny(logicList: JSONArray?, answerList: List<JSONObject>): Boolean {
            return fromJSONArray<JSONObject>(logicList).any { logic ->
                val currentLogic = logic as JSONObject
                var found = false
                currentLogic.keys().forEach { currentKey: String ->
                    val pattern = currentLogic.optString(currentKey)
                    found = found or answerList
                            .map { currentElement ->
                                when {
                                    NinchatQuestionnaireType.isCheckBox(currentElement) -> {
                                        fromJSONArray<JSONObject>(currentElement.optJSONArray("options"))
                                                .map { element -> element as JSONObject }
                                    }
                                    else -> listOf(currentElement)
                                }
                            }
                            .flatten()
                            .asReversed()
                            .filter { currentAnswer ->
                                currentKey == currentAnswer.optString("name")
                            }
                            .distinctBy { currentAnswer ->
                                currentAnswer.optString("name")
                            }.any { currentAnswer: JSONObject ->
                                val result = currentAnswer.optString("result")
                                matchPattern(currentInput = result, pattern = pattern)
                            }
                }
                found
            }
        }

        fun matchAnswerList(logicElement: JSONObject?, answerList: List<JSONObject>, preAnswerList: List<JSONObject>): Boolean {
            val currentLogic = logicElement?.optJSONObject("logic")
            val hasAndLogic = hasLogic(logicElement = currentLogic, isAnd = true)
            val hasOrLogic = hasLogic(logicElement = currentLogic, isAnd = false)
            val notOverridePreAnswers = preAnswerList.filter {
                val name = it.optString("name")
                answerList.any { currentAnswer ->
                    val answeredName = currentAnswer.optString("name")
                    name == answeredName
                }.not()
            }
            return when {
                // if does not have both and or or logic then it a direct match
                !hasAndLogic && !hasOrLogic -> {
                    true
                }
                matchedLogicAll(logicList = currentLogic?.optJSONArray("and"), answerList = answerList) ->
                    true
                matchedLogicAny(logicList = currentLogic?.optJSONArray("or"), answerList = answerList) ->
                    true
                // match pre-answer list
                matchedLogicAll(logicList = currentLogic?.optJSONArray("and"), answerList = notOverridePreAnswers) ->
                    true
                matchedLogicAny(logicList = currentLogic?.optJSONArray("or"), answerList = notOverridePreAnswers) ->
                    true
                else -> {
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

        fun hasError(answerList: List<JSONObject>, selectedElement: Pair<String, Int>?): Boolean {
            return answerList.takeLast(selectedElement?.second ?: 0)
                    .map { currentElement ->
                        when {
                            NinchatQuestionnaireType.isCheckBox(currentElement) -> {
                                fromJSONArray<JSONObject>(currentElement.optJSONArray("options"))
                                        .map { element -> element as JSONObject }
                            }
                            else -> listOf(currentElement)
                        }
                    }
                    .flatten()
                    .any { currentElement ->
                        // is required but there is no result
                        !requiredOk(json = currentElement) || !matchPattern(json = currentElement)
                    }
        }

        fun updateError(answerList: List<JSONObject>, selectedElement: Pair<String, Int>?): List<JSONObject> {
            answerList.takeLast(selectedElement?.second ?: 0).forEach { currentElement ->
                if (NinchatQuestionnaireType.isCheckBox(jsonObject = currentElement)) {
                    fromJSONArray<JSONObject>(questionnaireList = currentElement.optJSONArray("options"))
                            .map { it as JSONObject }
                            .forEach {
                                if (!requiredOk(json = it) || !matchPattern(json = it)) {
                                    it.putOpt("hasError", true)
                                }
                            }
                } else {
                    // is required but there is no result
                    if (!requiredOk(json = currentElement) || !matchPattern(json = currentElement)) {
                        currentElement.putOpt("hasError", true)
                    }
                }

            }
            return answerList
        }

        fun slowCopy(json: JSONObject): JSONObject =
                JSONObject(json.toString(2))

        fun getQuestionnaireAnswers(answerList: List<JSONObject>): List<Pair<String, String>> {
            return answerList
                    .map { currentElement ->
                        when {
                            NinchatQuestionnaireType.isCheckBox(currentElement) -> {
                                fromJSONArray<JSONObject>(currentElement.optJSONArray("options"))
                                        .map { element -> element as JSONObject }
                            }
                            else -> listOf(currentElement)
                        }
                    }
                    .flatten()
                    .asReversed()
                    .distinctBy {
                        it.optString("name")
                    }
                    .mapNotNull {
                        when {
                            NinchatQuestionnaireType.isLogic(it) || NinchatQuestionnaireType.isButton(it) || NinchatQuestionnaireType.isText(it) -> null
                            it.optString("result").isNullOrBlank() -> null
                            // ignore any result that is false
                            it.optString("result", "") == "false" -> null
                            else -> it
                        }
                    }
                    .map {
                        Pair<String, String>(it.optString("name"), it.optString("result"))
                    }
        }

        fun resetElements(answerList: List<JSONObject>, from: Int): List<JSONObject> {
            return answerList.mapIndexed { index, jsonObject ->
                if (index >= from) {
                    jsonObject.remove("position")
                    jsonObject.remove("hasError")
                    jsonObject.remove("tags")
                    jsonObject.remove("queueId")
                    if (NinchatQuestionnaireType.isCheckBox(jsonObject = jsonObject)) {
                        fromJSONArray<JSONObject>(questionnaireList = jsonObject.optJSONArray("options"))
                                .map { it as JSONObject }
                                .forEach {
                                    it.putOpt("result", false)
                                }
                    } else {
                        jsonObject.remove("result")

                    }
                }
                jsonObject
            }
        }

        fun getQuestionnaireTags(answerList: List<JSONObject>): List<String> {
            return answerList
                    .asReversed()
                    .distinctBy {
                        it.optString("name")
                    }
                    .map {
                        fromJSONArray<String>(it.optJSONArray("tags")).map { tag -> tag as String }
                    }.flatten()
        }

        fun getQuestionnaireQueue(answerList: List<JSONObject>): String? {
            return answerList
                    .asReversed()
                    .distinctBy {
                        it.optString("name")
                    }
                    .mapNotNull {
                        it.optString("queue", it.optString("queueId"))
                    }.firstOrNull()
        }
    }
}