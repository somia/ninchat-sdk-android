package com.ninchat.sdk.models.questionnaire.data

import org.json.JSONArray

class QuestionnariesWithButtons {
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
                ],
                "buttons":{
                    "back":false,
                    "next":false
                },
            }
        ]"""

            return JSONArray(questionnaires)
        }
    }
}