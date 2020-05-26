package com.ninchat.sdk.models.questionnaires

import com.ninchat.sdk.models.questionnaires.data.*
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
        val questionnaires = QuestionnariesWithRedirects.getQuestionnaires()
        val isSimpleForm = ninchatQuestionnairesBase.simpleForm(questionnaires)
        Assert.assertEquals(false, isSimpleForm)
    }

    @Test
    fun `should not be a simple form if questionnaires has logic`() {
        val ninchatQuestionnairesBase = NinchatQuestionnairesBase()
        val questionnaires = QuestionnariesWithLogic.getQuestionnaires()
        val isSimpleForm = ninchatQuestionnairesBase.simpleForm(questionnaires)
        Assert.assertEquals(false, isSimpleForm)
    }

    @Test
    fun `should not be a simple form if questionnaires has buttons`() {
        val ninchatQuestionnairesBase = NinchatQuestionnairesBase()
        val questionnaires = QuestionnariesWithButtons.getQuestionnaires()
        val isSimpleForm = ninchatQuestionnairesBase.simpleForm(questionnaires)
        Assert.assertEquals(false, isSimpleForm)
    }

    @Test
    fun `should not be a simple form if questionnaires is a group element`() {
        val ninchatQuestionnairesBase = NinchatQuestionnairesBase()
        val questionnaires = QuestionnariesWithGroupElements.getQuestionnaires()
        val isSimpleForm = ninchatQuestionnairesBase.simpleForm(questionnaires)
        Assert.assertEquals(false, isSimpleForm)
    }

    @Test
    fun `should not be a simple form with no redirects, logic, buttons or group type`() {
        val ninchatQuestionnairesBase = NinchatQuestionnairesBase()
        val questionnaires = QuestionnariesSimpleFormLike.getQuestionnaires()
        val isSimpleForm = ninchatQuestionnairesBase.simpleForm(questionnaires)
        Assert.assertEquals(true, isSimpleForm)
    }

    @Test
    fun `should return true for a group element that has more than one element`() {
        val ninchatQuestionnairesBase = NinchatQuestionnairesBase()
        val questionnaires = QuestionnariesWithGroupElements.groupElementWithElements()
        val groupElement = ninchatQuestionnairesBase.isGroupElement(questionnaires)
        Assert.assertEquals(true, groupElement)
    }

    @Test
    fun `should return false for a group element that has only one element`() {
        val ninchatQuestionnairesBase = NinchatQuestionnairesBase()
        val questionnaires = QuestionnariesWithGroupElements.groupElementWithElement()
        val groupElement = ninchatQuestionnairesBase.isGroupElement(questionnaires)
        Assert.assertEquals(false, groupElement)
    }

    @Test
    fun `should return false for a non-group element`() {
        val ninchatQuestionnairesBase = NinchatQuestionnairesBase()
        val questionnaires = QuestionnariesWithGroupElements.nonGroupElement()
        val groupElement = ninchatQuestionnairesBase.isGroupElement(questionnaires)
        Assert.assertEquals(false, groupElement)
    }

    @Test
    fun `should return false for a empty element`() {
        val ninchatQuestionnairesBase = NinchatQuestionnairesBase()
        val questionnaires = QuestionnariesWithGroupElements.nonGroupElement()
        val groupElement = ninchatQuestionnairesBase.isGroupElement(null)
        Assert.assertEquals(false, groupElement)
    }

    @Test
    fun `should find a element from questionnaires by name`() {
        val ninchatQuestionnairesBase = NinchatQuestionnairesBase()
        val questionnaires = QuestionnariesWithGroupElements.getQuestionnaires()
        val element1 = ninchatQuestionnairesBase.getQuestionnairesElementByName(questionnaires, "Suojautuminen")
        val element2 = ninchatQuestionnairesBase.getQuestionnairesElementByName(questionnaires, "Aiheet")
        Assert.assertNotNull(element1)
        Assert.assertNotNull(element2)
    }

    @Test
    fun `should not able to find a element from questionnaires for unknown name`() {
        val ninchatQuestionnairesBase = NinchatQuestionnairesBase()
        val questionnaires = QuestionnariesWithGroupElements.getQuestionnaires()
        val element1 = ninchatQuestionnairesBase.getQuestionnairesElementByName(questionnaires, "SomethingElse")
        val element2 = ninchatQuestionnairesBase.getQuestionnairesElementByName(questionnaires, null)
        Assert.assertNull(element1)
        Assert.assertNull(element2)
    }

    @Test
    fun `should not able to find a element from null questionnaries`() {
        val ninchatQuestionnairesBase = NinchatQuestionnairesBase()
        val element1 = ninchatQuestionnairesBase.getQuestionnairesElementByName(null, "SomethingElse")
        Assert.assertNull(element1)
    }
}
