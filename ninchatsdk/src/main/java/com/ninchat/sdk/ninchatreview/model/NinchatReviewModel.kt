package com.ninchat.sdk.ninchatreview.model

import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.NinchatSessionManager
import org.json.JSONObject


class NinchatReviewModel {
    /*
     * Set default rating as no rating
     */
    var currentRating: Int = NinchatSession.Analytics.Rating.NO_ANSWER

    fun getBotName(): String? {
        return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getQuestionnaireName()?.let {
            if (it in listOf("null", "false", "")) null
            else it
        }
    }

    fun getBotAvatar(): String? {
        return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getQuestionnaireAvatar()?.let {
            if (it in listOf("null", "false", "")) null
            else it
        }
    }

    /**
     * Return whether this is a conversation like or questionnaire like view
     */
    fun isConversationLikeQuestionnaire(): Boolean {
        return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getPostAudienceQuestionnaireStyle() == "conversation"
    }

    fun getRatingPayload(): JSONObject {
        val value = JSONObject()
        value.put("rating", currentRating)
        val data = JSONObject()
        data.put("data", value)
        return data
    }

    fun getFeedbackTitleText(): String? {
        return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getFeedbackTitleText()
    }

    fun getFeedbackPositiveText(): String? {
        return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getFeedbackPositiveText()
    }

    fun getFeedbackNeutralText(): String? {
        return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getFeedbackNeutralText()
    }

    fun getFeedbackNegativeText(): String? {
        return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getFeedbackNegativeText()
    }

    fun getFeedbackSkipText(): String? {
        return NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getFeedbackSkipText()
    }

    companion object {
        @JvmField
        val REQUEST_CODE = NinchatReviewModel::class.java.hashCode() and 0xffff
    }

}