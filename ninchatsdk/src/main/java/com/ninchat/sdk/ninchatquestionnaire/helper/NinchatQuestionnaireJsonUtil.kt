package com.ninchat.sdk.ninchatquestionnaire.helper

import android.text.InputType
import org.json.JSONArray
import org.json.JSONObject

inline fun <reified T> fromJSONArray(questionnaireList: JSONArray?): ArrayList<Any> {
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
                    NinchatQuestionnaireConstants.elements to NinchatQuestionnaireConstants.buttons,
                    NinchatQuestionnaireConstants.fireEvent to true,
                    NinchatQuestionnaireConstants.back to if(hasBackButton) json?.optJSONObject(NinchatQuestionnaireConstants.buttons)?.optString("back") else false,
                    NinchatQuestionnaireConstants.next to if(hasNextButton) json?.optJSONObject(NinchatQuestionnaireConstants.buttons)?.optString("next") else true,
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

        fun getLogicByName(questionnaireList: JSONArray?, logicName: String): List<JSONObject> {
            return fromJSONArray<JSONObject>(questionnaireList = questionnaireList)
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
    }
}