package com.ninchat.sdk.ninchatquestionnaire.common.interfaces

import org.json.JSONArray
import org.json.JSONObject

interface INinchatQuestionnairePresenter {
    fun addContent(questionnaireList: JSONObject?)
    fun updateContent(questionnaireList: JSONArray?)
    fun lastElement(): JSONObject?
    fun secondLastElement(): JSONObject?
    fun removeLast(): JSONObject?
    fun getQuestionnaireList(): JSONArray?
}