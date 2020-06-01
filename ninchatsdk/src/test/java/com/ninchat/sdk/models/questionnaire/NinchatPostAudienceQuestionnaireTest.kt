package com.ninchat.sdk.models.questionnaire

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class NinchatPostAudienceQuestionnaireTest{
    lateinit var configuration: JSONObject
    lateinit var postAudienceQuestionnaireJson: JSONArray
    @Before
    fun setUp() {
        configuration = Mockito.mock(JSONObject::class.java)
        postAudienceQuestionnaireJson = Mockito.mock(JSONArray::class.java)
    }

    @Test
    fun `should parse preAudienceQuestionnaire from configuration json with given type`() {
        Mockito.doReturn(postAudienceQuestionnaireJson).`when`(configuration).optJSONArray(
                NinchatQuestionnaireBase.QuestionnaireType.POST_AUDIENCE_QUESTIONNAIRE.toString()
        )
        val ninchatPostAudienceQuestionnairesTest = NinchatPostAudienceQuestionnaire(configuration)
        Assert.assertEquals(postAudienceQuestionnaireJson, ninchatPostAudienceQuestionnairesTest.questionnaireList)
    }
}
