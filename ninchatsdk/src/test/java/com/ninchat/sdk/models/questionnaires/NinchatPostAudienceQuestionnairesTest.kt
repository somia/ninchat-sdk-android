package com.ninchat.sdk.models.questionnaires

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class NinchatPostAudienceQuestionnairesTest{
    lateinit var configuration: JSONObject
    lateinit var postAudienceQuestionnairesJson: JSONArray
    @Before
    fun setUp() {
        configuration = Mockito.mock(JSONObject::class.java)
        postAudienceQuestionnairesJson = Mockito.mock(JSONArray::class.java)
    }

    @Test
    fun `should parse preAudienceQuestionnaire from configuration json with given type`() {
        Mockito.doReturn(postAudienceQuestionnairesJson).`when`(configuration).optJSONArray(
                NinchatQuestionnairesBase.QuestionnairesType.POST_AUDIENCE_QUESTIONNAIRES.toString()
        )
        val ninchatPostAudienceQuestionnairesTest = NinchatPostAudienceQuestionnaires(configuration)
        Assert.assertEquals(postAudienceQuestionnairesJson, ninchatPostAudienceQuestionnairesTest.questionnaires)
    }
}
