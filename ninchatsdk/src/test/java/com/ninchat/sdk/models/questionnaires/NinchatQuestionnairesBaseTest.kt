package com.ninchat.sdk.models.questionnaires

import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

class NinchatQuestionnairesBaseTest {
    @Test
    fun `should parse preAudienceQuestionnaire from configuration json with given type`() {
        val configuration = mock(JSONObject::class.java)
        val preAudienceQuestionnairesJson = mock(JSONObject::class.java)
        val postAudienceQuestionnairesJson = mock(JSONObject::class.java)

        doReturn(preAudienceQuestionnairesJson).`when`(configuration).getJSONObject(
                NinchatQuestionnairesBase.QuestionnairesType.PRE_AUDIENCE_QUESTIONNAIRES.toString()
        )
        val ninchatQuestionnairesBase = NinchatQuestionnairesBase()
        val retval = ninchatQuestionnairesBase.parse(configuration, NinchatQuestionnairesBase.QuestionnairesType.PRE_AUDIENCE_QUESTIONNAIRES)
        Assert.assertEquals(preAudienceQuestionnairesJson, retval)
        Assert.assertNotEquals(postAudienceQuestionnairesJson, retval)
    }

    @Test
    fun `should parse postAudienceQuestionnaire from configuration json with given type`() {
        val configuration = mock(JSONObject::class.java)
        val preAudienceQuestionnairesJson = mock(JSONObject::class.java)
        val postAudienceQuestionnairesJson = mock(JSONObject::class.java)

        doReturn(postAudienceQuestionnairesJson).`when`(configuration).getJSONObject(
                NinchatQuestionnairesBase.QuestionnairesType.POST_AUDIENCE_QUESTIONNAIRES.toString()
        )
        val ninchatQuestionnairesBase = NinchatQuestionnairesBase()
        val retval = ninchatQuestionnairesBase.parse(configuration, NinchatQuestionnairesBase.QuestionnairesType.POST_AUDIENCE_QUESTIONNAIRES)
        Assert.assertEquals(postAudienceQuestionnairesJson, retval)
        Assert.assertNotEquals(preAudienceQuestionnairesJson, retval)
    }

    @Test
    fun `should return null in case malformed json object provided`() {
        val ninchatQuestionnairesBase = NinchatQuestionnairesBase()
        val retval = ninchatQuestionnairesBase.parse(null, NinchatQuestionnairesBase.QuestionnairesType.POST_AUDIENCE_QUESTIONNAIRES)
        Assert.assertNull(retval)
    }
}