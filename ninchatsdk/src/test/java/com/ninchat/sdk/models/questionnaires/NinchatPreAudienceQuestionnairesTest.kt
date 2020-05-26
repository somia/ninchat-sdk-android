package com.ninchat.sdk.models.questionnaires

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class NinchatPreAudienceQuestionnairesTest {
    lateinit var configuration: JSONObject
    lateinit var preAudienceQuestionnairesJson: JSONArray
    @Before
    fun setUp() {
        configuration = Mockito.mock(JSONObject::class.java)
        preAudienceQuestionnairesJson = Mockito.mock(JSONArray::class.java)
    }

    @Test
    fun `should parse preAudienceQuestionnaire from configuration json with given type`() {
        Mockito.doReturn(preAudienceQuestionnairesJson).`when`(configuration).optJSONArray(
                NinchatQuestionnairesBase.QuestionnairesType.PRE_AUDIENCE_QUESTIONNAIRES.toString()
        )
        val ninchatPreAudienceQuestionnaires = NinchatPreAudienceQuestionnaires(configuration)
        Assert.assertEquals(preAudienceQuestionnairesJson, ninchatPreAudienceQuestionnaires.questionnaires)
    }
}
