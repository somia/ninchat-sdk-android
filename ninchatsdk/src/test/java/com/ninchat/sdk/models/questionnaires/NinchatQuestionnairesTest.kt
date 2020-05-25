package com.ninchat.sdk.models.questionnaires

import org.json.JSONObject
import org.junit.Test
import org.mockito.Mockito

class NinchatQuestionnairesTest {
    @Test
    fun `should parse preAudienceQuestionnaire and postAudienceQuestionnaire from configuration json`() {
        val configuration = Mockito.mock(JSONObject::class.java)
        val ninchatQuestionnairesTest = NinchatQuestionnaires(configuration)
    }
}
