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


        fun isEoF(jsonObject: JSONObject): Boolean =
                "eof" == jsonObject.optString("element")

        fun isSimpleFormLikeQuestionnaire(questionnaires: JSONArray?): Boolean {
            return fromJSONArray<JSONObject>(questionnaireList = questionnaires).any {
                val currentElement = it as JSONObject
                val redirects = currentElement.optJSONArray(NinchatQuestionnaireConstants.redirects)
                val logic = currentElement.optJSONObject(NinchatQuestionnaireConstants.logic)
                val buttons = currentElement.optJSONObject(NinchatQuestionnaireConstants.buttons)
                val elementType = currentElement.optString(NinchatQuestionnaireConstants.type)
                return redirects != null || logic != null || buttons != null || elementType == NinchatQuestionnaireConstants.group
            }.not()
        }

        fun isGroupElement(element: JSONObject?): Boolean =
                element?.optString(NinchatQuestionnaireConstants.type) in listOf("group", "elements")

        fun isRegister(target: String?): Boolean =
                "_register" == target

        fun isComplete(target: String?): Boolean =
                "_complete" == target

        fun isRequired(element: JSONObject?): Boolean =
                NinchatQuestionnaireConstants.required == NinchatQuestionnaireConstants.elements

        fun isElement(element: JSONObject?): Boolean =
                element?.optString(NinchatQuestionnaireConstants.type) in listOf("elements", "element")

        fun isLogic(element: JSONObject): Boolean =
                element.has(NinchatQuestionnaireConstants.logic)
    }
}