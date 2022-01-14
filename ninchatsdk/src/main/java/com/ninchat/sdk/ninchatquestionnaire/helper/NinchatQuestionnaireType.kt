package com.ninchat.sdk.ninchatquestionnaire.helper

import org.json.JSONArray
import org.json.JSONObject

class NinchatQuestionnaireType {
    companion object {
        fun isText(jsonObject: JSONObject): Boolean =
                "text" == jsonObject.optString("element")

        fun isInput(jsonObject: JSONObject): Boolean =
                "input" == jsonObject.optString("element")

        fun isTextArea(jsonObject: JSONObject): Boolean =
                "textarea" == jsonObject.optString("element")

        fun isSelect(jsonObject: JSONObject): Boolean =
                "select" == jsonObject.optString("element")

        fun isRadio(jsonObject: JSONObject): Boolean =
                "radio" == jsonObject.optString("element")

        fun isLikeRT(jsonObject: JSONObject): Boolean =
                "likert" == jsonObject.optString("element")

        fun isCheckBox(jsonObject: JSONObject): Boolean =
                "checkbox" == jsonObject.optString("element")

        fun isButton(jsonObject: JSONObject): Boolean =
                "buttons" == jsonObject.optString("element")

        fun isBotElement(jsonObject: JSONObject): Boolean =
                "botElement" == jsonObject.optString("element")

        fun isHyperlinkElement(jsonObject: JSONObject): Boolean =
                "a" == jsonObject.optString("element")

        fun isEoF(jsonObject: JSONObject): Boolean =
                "eof" == jsonObject.optString("element")

        fun isButton(json: JSONObject?, isBack: Boolean = false): Boolean {
            val key = if (isBack) NinchatQuestionnaireConstants.back else NinchatQuestionnaireConstants.next
            return json?.let {
                it.optString(key,"true") !in listOf("false", "")
            } ?: true
        }

        fun isSimpleFormLikeQuestionnaire(questionnaires: JSONArray?): Boolean {
            return fromJSONArray<JSONObject>(questionnaireList = questionnaires).any {
                val currentElement = it as JSONObject
                val redirects = currentElement.optJSONArray(NinchatQuestionnaireConstants.redirects)
                val logic = currentElement.optJSONObject(NinchatQuestionnaireConstants.logic)
                val buttons = currentElement.optJSONObject(NinchatQuestionnaireConstants.buttons)
                val elementType = currentElement.optString(NinchatQuestionnaireConstants.type)
                redirects != null || logic != null || buttons != null || elementType == NinchatQuestionnaireConstants.group
            }.not()
        }

        fun isGroupElement(element: JSONObject?): Boolean =
                element?.optString(NinchatQuestionnaireConstants.type) in listOf("group", "elements")

        fun isElement(element: JSONObject?): Boolean =
                element?.has("elements") == true || element?.has("element") == true
        
        fun isLogic(element: JSONObject?): Boolean =
                element?.has(NinchatQuestionnaireConstants.logic) == true

        fun isRedirect(element: JSONObject?): Boolean =
                element?.has(NinchatQuestionnaireConstants.redirects) == true
    }
}