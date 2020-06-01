package com.ninchat.sdk.models.questionnaire

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class NinchatPreAudienceQuestionnaireTest {
    lateinit var configuration: JSONObject
    lateinit var preAudienceQuestionnaireJson: JSONArray
    @Before
    fun setUp() {
        configuration = Mockito.mock(JSONObject::class.java)
        preAudienceQuestionnaireJson = Mockito.mock(JSONArray::class.java)
    }

    @Test
    fun `should parse preAudienceQuestionnaire from configuration json with given type`() {
        Mockito.doReturn(preAudienceQuestionnaireJson).`when`(configuration).optJSONArray(
                NinchatQuestionnaireBase.QuestionnaireType.PRE_AUDIENCE_QUESTIONNAIRE.toString()
        )
        val ninchatPreAudienceQuestionnaires = NinchatPreAudienceQuestionnaire(configuration)
        Assert.assertEquals(preAudienceQuestionnaireJson, ninchatPreAudienceQuestionnaires.questionnaireList)
    }
}
