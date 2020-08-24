package com.ninchat.sdk.models.questionnaire.data

import org.json.JSONArray

class QuestionnariesSimpleFormLike {
    companion object Factory {
        fun getQuestionnaires(): JSONArray {
            val questionnaires = """[
            {
               "element":"text",
               "label":"<h3>Welcome!</h3><p>Tell some info about yourself before entering chat</p>",
               "name":"intro"
            },
            {
               "element":"text",
               "label":"Hello! How can we help?",
               "name":"intro"
            },
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
            }
        ]"""

            return JSONArray(questionnaires)
        }
    }
}