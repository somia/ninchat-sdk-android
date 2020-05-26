package com.ninchat.sdk.models.questionnaires.data

import org.json.JSONArray

class QuestionnariesWithLogic {
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
                        "label":"Huolen tai epävarmuuden sietäminen", 
                        "value":"Huolen tai epävarmuuden sietäminen"
                    }
                ]
            },
            {
                "name":"Koronavirus-Logic2",
                "logic":{
                    "and":[
                        {"Koronavirus-jatko":"Muut aiheet"}
                    ],
                    "target":"Aiheet"
                }
            },
            
        ]"""

            return JSONArray(questionnaires)
        }
    }
}