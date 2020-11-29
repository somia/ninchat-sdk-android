package com.ninchat.sdk.ninchatquestionnaire.helper

import org.json.JSONArray
import org.json.JSONObject

class NinchatQuestionnaireSanitizer {
    companion object {
        fun simpleFormToGroupQuestionnaire(questionnaireArr: JSONArray): JSONArray {
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
    }
}