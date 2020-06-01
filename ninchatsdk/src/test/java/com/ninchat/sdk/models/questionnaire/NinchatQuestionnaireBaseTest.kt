package com.ninchat.sdk.models.questionnaire

import com.ninchat.sdk.models.questionnaire.data.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

class NinchatQuestionnaireBaseTest {
    lateinit var configuration: JSONObject
    lateinit var preAudienceQuestionnaireJson: JSONArray
    lateinit var postAudienceQuestionnaireJson: JSONArray

    @Before
    fun setUp() {
        configuration = mock(JSONObject::class.java)
        preAudienceQuestionnaireJson = mock(JSONArray::class.java)
        postAudienceQuestionnaireJson = mock(JSONArray::class.java)
    }

    @Test
    fun `should parse preAudienceQuestionnaire from configuration json with given type`() {
        doReturn(preAudienceQuestionnaireJson).`when`(configuration).optJSONArray(
                NinchatQuestionnaireBase.QuestionnaireType.PRE_AUDIENCE_QUESTIONNAIRE.toString()
        )
        val ninchatQuestionnaireBase = NinchatQuestionnaireBase()
        val retval = ninchatQuestionnaireBase.parse(configuration, NinchatQuestionnaireBase.QuestionnaireType.PRE_AUDIENCE_QUESTIONNAIRE)
        Assert.assertEquals(preAudienceQuestionnaireJson, retval)
        Assert.assertNotEquals(postAudienceQuestionnaireJson, retval)
    }

    @Test
    fun `should parse postAudienceQuestionnaire from configuration json with given type`() {
        doReturn(postAudienceQuestionnaireJson).`when`(configuration).optJSONArray(
                NinchatQuestionnaireBase.QuestionnaireType.POST_AUDIENCE_QUESTIONNAIRE.toString()
        )
        val ninchatQuestionnaireBase = NinchatQuestionnaireBase()
        val retval = ninchatQuestionnaireBase.parse(configuration, NinchatQuestionnaireBase.QuestionnaireType.POST_AUDIENCE_QUESTIONNAIRE)
        Assert.assertEquals(postAudienceQuestionnaireJson, retval)
        Assert.assertNotEquals(preAudienceQuestionnaireJson, retval)
    }

    @Test
    fun `should return null in case malformed json object provided`() {
        val ninchatQuestionnairesBase = NinchatQuestionnaireBase()
        val retval = ninchatQuestionnairesBase.parse(null, NinchatQuestionnaireBase.QuestionnaireType.POST_AUDIENCE_QUESTIONNAIRE)
        Assert.assertNull(retval)
    }

    @Test
    fun `should not be a simple form if questionnaires is null`() {
        val ninchatQuestionnairesBase = NinchatQuestionnaireBase()
        val isSimpleForm = ninchatQuestionnairesBase.simpleForm(null)
        Assert.assertEquals(false, isSimpleForm)
    }

    @Test
    fun `should not be a simple form if questionnaires has redirects`() {
        val ninchatQuestionnairesBase = NinchatQuestionnaireBase()
        val questionnaires = QuestionnariesWithRedirects.getQuestionnaires()
        val isSimpleForm = ninchatQuestionnairesBase.simpleForm(questionnaires)
        Assert.assertEquals(false, isSimpleForm)
    }

    @Test
    fun `should not be a simple form if questionnaires has logic`() {
        val ninchatQuestionnairesBase = NinchatQuestionnaireBase()
        val questionnaires = QuestionnariesWithLogic.getQuestionnaires()
        val isSimpleForm = ninchatQuestionnairesBase.simpleForm(questionnaires)
        Assert.assertEquals(false, isSimpleForm)
    }

    @Test
    fun `should not be a simple form if questionnaires has buttons`() {
        val ninchatQuestionnairesBase = NinchatQuestionnaireBase()
        val questionnaires = QuestionnariesWithButtons.getQuestionnaires()
        val isSimpleForm = ninchatQuestionnairesBase.simpleForm(questionnaires)
        Assert.assertEquals(false, isSimpleForm)
    }

    @Test
    fun `should not be a simple form if questionnaires is a group element`() {
        val ninchatQuestionnairesBase = NinchatQuestionnaireBase()
        val questionnaires = QuestionnariesWithGroupElements.getQuestionnaires()
        val isSimpleForm = ninchatQuestionnairesBase.simpleForm(questionnaires)
        Assert.assertEquals(false, isSimpleForm)
    }

    @Test
    fun `should not be a simple form with no redirects, logic, buttons or group type`() {
        val ninchatQuestionnairesBase = NinchatQuestionnaireBase()
        val questionnaires = QuestionnariesSimpleFormLike.getQuestionnaires()
        val isSimpleForm = ninchatQuestionnairesBase.simpleForm(questionnaires)
        Assert.assertEquals(true, isSimpleForm)
    }

    @Test
    fun `should return true for a group element that has more than one element`() {
        val ninchatQuestionnairesBase = NinchatQuestionnaireBase()
        val questionnaires = QuestionnariesWithGroupElements.groupElementWithElements()
        val groupElement = ninchatQuestionnairesBase.isGroupElement(questionnaires)
        Assert.assertEquals(true, groupElement)
    }

    @Test
    fun `should return false for a group element that has only one element`() {
        val ninchatQuestionnairesBase = NinchatQuestionnaireBase()
        val questionnaires = QuestionnariesWithGroupElements.groupElementWithElement()
        val groupElement = ninchatQuestionnairesBase.isGroupElement(questionnaires)
        Assert.assertEquals(false, groupElement)
    }

    @Test
    fun `should return false for a non-group element`() {
        val ninchatQuestionnairesBase = NinchatQuestionnaireBase()
        val questionnaires = QuestionnariesWithGroupElements.nonGroupElement()
        val groupElement = ninchatQuestionnairesBase.isGroupElement(questionnaires)
        Assert.assertEquals(false, groupElement)
    }

    @Test
    fun `should return false for a empty element`() {
        val ninchatQuestionnairesBase = NinchatQuestionnaireBase()
        val questionnaires = QuestionnariesWithGroupElements.nonGroupElement()
        val groupElement = ninchatQuestionnairesBase.isGroupElement(null)
        Assert.assertEquals(false, groupElement)
    }

    @Test
    fun `should find a element from questionnaires by name`() {
        val ninchatQuestionnairesBase = NinchatQuestionnaireBase()
        val questionnaires = QuestionnariesWithGroupElements.getQuestionnaires()
        val element1 = ninchatQuestionnairesBase.getQuestionnaireElementByName(questionnaires, "Suojautuminen")
        val element2 = ninchatQuestionnairesBase.getQuestionnaireElementByName(questionnaires, "Aiheet")
        Assert.assertNotNull(element1)
        Assert.assertNotNull(element2)
    }

    @Test
    fun `should not able to find a element from questionnaires for unknown name`() {
        val ninchatQuestionnairesBase = NinchatQuestionnaireBase()
        val questionnaires = QuestionnariesWithGroupElements.getQuestionnaires()
        val element1 = ninchatQuestionnairesBase.getQuestionnaireElementByName(questionnaires, "SomethingElse")
        val element2 = ninchatQuestionnairesBase.getQuestionnaireElementByName(questionnaires, null)
        Assert.assertNull(element1)
        Assert.assertNull(element2)
    }

    @Test
    fun `should not able to find a element from null questionnaries`() {
        val ninchatQuestionnairesBase = NinchatQuestionnaireBase()
        val element1 = ninchatQuestionnairesBase.getQuestionnaireElementByName(null, "SomethingElse")
        Assert.assertNull(element1)
    }
}
