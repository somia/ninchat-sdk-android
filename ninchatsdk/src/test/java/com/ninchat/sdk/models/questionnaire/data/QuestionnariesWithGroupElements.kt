package com.ninchat.sdk.models.questionnaire.data

import org.json.JSONArray
import org.json.JSONObject

class QuestionnariesWithGroupElements {
    companion object Factory {
        fun getQuestionnaires(): JSONArray {
            val questionnaires = """[
            {
                "element":"radio",
                "name":"Aiheet",
                "label":"Hei. Voin auttaa sinua koronavirusta (COVID-19) koskevissa kysymyksissä. Mitä tietoa etsit?",
                "required":true,
                "options":[
                    {
                    "label":"Mikä on koronavirus?",
                    "value":"Mikä on koronavirus"
                    },
                ]
            },
            {    
                "name":"Suojautuminen",
                "type":"group",
                "elements":[{
                        "element":"text",
                        "name":"Suojautuminen-tietoa",
                        "label":"<p>Suojautuminen koronavirustaudilta</p><a href='https://thl.fi/fi/web/infektiotaudit-ja-rokotukset/ajankohtaista/ajankohtaista-koronaviruksesta-covid-19/ohjeita-kansalaisille-koronaviruksesta?ref=ninchat' class='btn' target='_blank'>Ohjeita-kansalaisille</a>"
                    }, {
                        "element":"radio",
                        "name":"Suojautuminen-jatko",
                        "label":"Oliko ohjeesta apua?",
                        "required":true,
                        "options":[{
                            "label":"Kiitos, sulje chat",
                            "value":"Sulje"
                    }, {
                        "label":"Näytä muut aiheet",
                        "value":"Muut aiheet"
                    }]
                }]
            }
        ]"""

            return JSONArray(questionnaires)
        }

        fun groupElementWithElements(): JSONObject {
            val element = """
            {
                "type": "group",
                "elements": []
            }
            """.trimIndent()
            return JSONObject(element)
        }

        fun groupElementWithElement(): JSONObject {
            val element = """
            {
                "type": "group",
                "element": {}
            }
            """.trimIndent()
            return JSONObject(element)
        }

        fun nonGroupElement(): JSONObject {
            val element = """
            {
                "type": "button"
            }
            """.trimIndent()
            return JSONObject(element)
        }
    }
}