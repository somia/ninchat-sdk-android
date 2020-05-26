package com.ninchat.sdk.models.questionnaires.data

import org.json.JSONArray

class QuestionnariesWithRedirects {
    companion object Factory {
        fun getQuestionnaires(): JSONArray {
            val questionnaires = """[
            {
                "element":"radio",
                "name":"Aiheet",
                "label":"Hei. Voin auttaa sinua koronavirusta (COVID-19) koskevissa kysymyksissä. Mitä tietoa etsit?",
                "required":true,
                "redirects":[
                    {
                        "pattern":"Mikä on koronavirus",
                        "target":"Koronavirus"
                    },
                    {
                        "pattern":"Mitä teen jos epäilen koronavirusta",
                        "target":"Epäilys"
                    },
                    {
                        "pattern":"Minulla on sovittuja vastaanottoja miten toimin",
                        "target":"Sovitut"
                    }
                ]
            }
        ]"""

            return JSONArray(questionnaires)
        }
    }
}