package com.ninchat.sdk.models.questionnaires

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

class NinchatPostAudienceQuestionnairesTest{
    @Test
    fun `should parse preAudienceQuestionnaire from configuration json with given type`() {
        val configuration = Mockito.mock(JSONObject::class.java)
        val postAudienceQuestionnairesJson = Mockito.mock(JSONArray::class.java)
        Mockito.doReturn(postAudienceQuestionnairesJson).`when`(configuration).getJSONObject(
                NinchatQuestionnairesBase.QuestionnairesType.POST_AUDIENCE_QUESTIONNAIRES.toString()
        )
        val ninchatPostAudienceQuestionnairesTest = NinchatPostAudienceQuestionnaires(configuration)
        Assert.assertEquals(postAudienceQuestionnairesJson, ninchatPostAudienceQuestionnairesTest.questionnaires)
    }
}
