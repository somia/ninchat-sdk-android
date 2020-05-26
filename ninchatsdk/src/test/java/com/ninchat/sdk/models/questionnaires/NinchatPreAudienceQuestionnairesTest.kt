package com.ninchat.sdk.models.questionnaires

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

class NinchatPreAudienceQuestionnairesTest {
    @Test
    fun `should parse preAudienceQuestionnaire from configuration json with given type`() {
        val configuration = Mockito.mock(JSONObject::class.java)
        val preAudienceQuestionnairesJson = Mockito.mock(JSONArray::class.java)
        Mockito.doReturn(preAudienceQuestionnairesJson).`when`(configuration).getJSONArray(
                NinchatQuestionnairesBase.QuestionnairesType.PRE_AUDIENCE_QUESTIONNAIRES.toString()
        )
        val ninchatPreAudienceQuestionnaires = NinchatPreAudienceQuestionnaires(configuration)
        Assert.assertEquals(preAudienceQuestionnairesJson, ninchatPreAudienceQuestionnaires.questionnaires)
    }
}
