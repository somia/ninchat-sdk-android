package com.ninchat.sdk.espresso.ninchatreview.model


import com.ninchat.sdk.NinchatSessionManager
import org.junit.Test

class NinchatReviewModelTest {

    @Test
    fun `should_parse_bot_name_and_bot_avater`() {
        NinchatSessionManager.getInstance()?.ninchatState?.ninchatQuestionnaire?.botQuestionnaireName
    }

    @Test
    fun `conversation_like_questionnaire_should_return_false_as_default`() {

    }

    @Test
    fun `conversation_like_questionnaire_should_return_true`() {

    }

    @Test
    fun `should_be_able_to_parse_review_related_text_from_site_config`() {

    }
}