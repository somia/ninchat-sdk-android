package com.ninchat.sdk.models.questionnaires

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

class NinchatQuestionnairesBaseTest {
    lateinit var configuration: JSONObject
    lateinit var preAudienceQuestionnairesJson: JSONArray
    lateinit var postAudienceQuestionnairesJson: JSONArray
    @Before
    fun setUp() {
        configuration = mock(JSONObject::class.java)
        preAudienceQuestionnairesJson = mock(JSONArray::class.java)
        postAudienceQuestionnairesJson = mock(JSONArray::class.java)
    }

    @Test
    fun `should parse preAudienceQuestionnaire from configuration json with given type`() {
        doReturn(preAudienceQuestionnairesJson).`when`(configuration).optJSONArray(
                NinchatQuestionnairesBase.QuestionnairesType.PRE_AUDIENCE_QUESTIONNAIRES.toString()
        )
        val ninchatQuestionnairesBase = NinchatQuestionnairesBase()
        val retval = ninchatQuestionnairesBase.parse(configuration, NinchatQuestionnairesBase.QuestionnairesType.PRE_AUDIENCE_QUESTIONNAIRES)
        Assert.assertEquals(preAudienceQuestionnairesJson, retval)
        Assert.assertNotEquals(postAudienceQuestionnairesJson, retval)
    }

    @Test
    fun `should parse postAudienceQuestionnaire from configuration json with given type`() {
        doReturn(postAudienceQuestionnairesJson).`when`(configuration).optJSONArray(
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

    @Test
    fun `should not be a simple form if questionnaires is null`() {
        val ninchatQuestionnairesBase = NinchatQuestionnairesBase()
        val isSimpleForm = ninchatQuestionnairesBase.simpleForm(null)
        Assert.assertEquals(false, isSimpleForm)
    }

    @Test
    fun `should not be a simple form if questionnaires has redirects`() {
        val ninchatQuestionnairesBase = NinchatQuestionnairesBase()
        val sampleQuestionnairesWithRedirects = JSONArray("""{}""")
        doReturn(sampleQuestionnairesWithRedirects).`when`(configuration).optJSONArray(
                NinchatQuestionnairesBase.QuestionnairesType.POST_AUDIENCE_QUESTIONNAIRES.toString()
        )
        val isSimpleForm = ninchatQuestionnairesBase.simpleForm(null)
        Assert.assertEquals(false, isSimpleForm)
    }
}
