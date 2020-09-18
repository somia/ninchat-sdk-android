package com.ninchat.sdk.helper.siteconfigparser

import com.ninchat.sdk.utils.misc.Misc
import org.json.JSONArray
import org.json.JSONObject

/**
 * Ninchat site configuration data class and parser
 */
class NinchatSiteConfig {
    var siteConfig: JSONObject? = null
    fun setConfigString(value: String?) {
        try {
            value?.let {
                siteConfig = JSONObject(it)
            }
        } catch (_: Exception) {
        }
    }

    internal fun sanitizePreferredEnvironments(preferredEnvironments: ArrayList<String>?): ArrayList<String> {
        if (preferredEnvironments == null) {
            return arrayListOf("default")
        }
        if (!preferredEnvironments.contains("default")) {
            preferredEnvironments.add(0, "default")
        }
        return preferredEnvironments
    }

    fun getDefault(): JSONObject? {
        return siteConfig?.optJSONObject("default")
    }

    fun getJsonObject(key: String, preferredEnvironments: ArrayList<String>?): JSONObject? {
        if (siteConfig == null) return null
        val environments = sanitizePreferredEnvironments(preferredEnvironments)

        var value: JSONObject? = null
        for (currentEnvironment in environments) {
            if (siteConfig?.optJSONObject(currentEnvironment)?.has(key) == true) {
                value = siteConfig?.optJSONObject(currentEnvironment)?.optJSONObject(key)
            }
        }
        return value
    }

    fun getArray(key: String, preferredEnvironments: ArrayList<String>?): JSONArray? {
        if (siteConfig == null) return null
        val environments = sanitizePreferredEnvironments(preferredEnvironments)

        var value: JSONArray? = null
        for (currentEnvironment in environments) {
            if (siteConfig?.optJSONObject(currentEnvironment)?.has(key) == true) {
                value = siteConfig?.optJSONObject(currentEnvironment)?.optJSONArray(key)
            }
        }
        return value
    }

    fun getBoolean(key: String, preferredEnvironments: ArrayList<String>?): Boolean? {
        if (siteConfig == null) return false
        val environments = sanitizePreferredEnvironments(preferredEnvironments)

        var value = false
        for (currentEnvironment in environments) {
            if (siteConfig?.optJSONObject(currentEnvironment)?.has(key) == true) {
                value = siteConfig?.optJSONObject(currentEnvironment)?.optBoolean(key, false)
                        ?: false
            }
        }
        return value
    }

    fun getString(key: String, preferredEnvironments: ArrayList<String>?): String? {
        if (siteConfig == null) return null
        val environments = sanitizePreferredEnvironments(preferredEnvironments)

        var value: String? = null
        for (currentEnvironment in environments) {
            if (siteConfig?.optJSONObject(currentEnvironment)?.has(key) == true) {
                value = siteConfig?.optJSONObject(currentEnvironment)?.optString(key)
                // workaround value can be null which when parsing will be interpreted as "null" string in Java with quote
                if ("null" == value) {
                    value = null
                }
            }
        }
        return value
    }

    fun getAudienceQueues(preferredEnvironments: ArrayList<String>?): MutableList<String?> {
        val queues = mutableListOf<String?>()
        val array: JSONArray? = getArray("audienceQueues", preferredEnvironments)
        array?.let {
            for (i in 0 until it.length()) {
                queues.add(it.optString(i))
            }
        }
        return queues
    }

    fun getAudienceAutoQueue(preferredEnvironments: ArrayList<String>?): String? =
            getString("audienceAutoQueue", preferredEnvironments)

    fun getRealmId(preferredEnvironments: ArrayList<String>?): String? =
            getString("audienceRealmId", preferredEnvironments)

    fun getWelcomeText(preferredEnvironments: ArrayList<String>?): String =
            getString("welcome", preferredEnvironments) ?: "welcome"

    fun getNoQueuesText(preferredEnvironments: ArrayList<String>?): String =
            getString("noQueuesText", preferredEnvironments) ?: "noQueuesText"

    fun getCloseWindowText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("Close window", preferredEnvironments) ?: "Close window"

    fun getUserName(preferredEnvironments: ArrayList<String>?): String? =
            getString("userName", preferredEnvironments)

    fun getAgentName(preferredEnvironments: ArrayList<String>?): String? =
            getString("agentName", preferredEnvironments)

    fun getSendButtonText(preferredEnvironments: ArrayList<String>?): String? =
            getString("sendButtonText", preferredEnvironments)

    fun getSubmitButtonText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("Submit", preferredEnvironments) ?: "Submit"

    fun isAttachmentsEnabled(preferredEnvironments: ArrayList<String>?): Boolean =
            getBoolean("supportFiles", preferredEnvironments) ?: false

    fun isVideoEnabled(preferredEnvironments: ArrayList<String>?): Boolean =
            getBoolean("supportVideo", preferredEnvironments) ?: false

    fun showUserAvatar(preferredEnvironments: ArrayList<String>?): Boolean =
            getBoolean("userAvatar", preferredEnvironments) ?: false

    fun showAgentAvatar(preferredEnvironments: ArrayList<String>?): Boolean =
            getBoolean("agentAvatar", preferredEnvironments) ?: false

    fun getAgentAvatar(preferredEnvironments: ArrayList<String>?): String? =
            getString("agentAvatar", preferredEnvironments)

    fun getUserAvatar(preferredEnvironments: ArrayList<String>?): String? =
            getString("userAvatar", preferredEnvironments)

    fun getConversationEndedText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("Conversation ended", preferredEnvironments) ?: "Conversation ended"

    fun getChatCloseText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("Close chat", preferredEnvironments) ?: "Close chat"

    fun getChatCloseConfirmationText(preferredEnvironments: ArrayList<String>?): String =
            getString("closeConfirmText", preferredEnvironments) ?: "closeConfirmText"

    fun getContinueChatText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("Continue chat", preferredEnvironments) ?: "Continue chat"

    fun getEnterMessageText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("Enter your message", preferredEnvironments) ?: "Enter your message"

    fun getVideoChatTitleText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("You are invited to a video chat", preferredEnvironments)
                    ?: "You are invited to a video chat"

    fun getVideoChatDescriptionText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("wants to video chat with you", preferredEnvironments)
                    ?: "wants to video chat with you"

    fun getVideoCallAcceptText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("Accept", preferredEnvironments) ?: "Accept"

    fun getVideoCallDeclineText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("Decline", preferredEnvironments) ?: "Decline"

    fun getVideoCallMetaMessageText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("You are invited to a video chat", preferredEnvironments)
                    ?: "You are invited to a video chat"

    fun getVideoCallAcceptedText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("Video chat answered", preferredEnvironments) ?: "Video chat answered"

    fun getVideoCallRejectedText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("Video chat declined", preferredEnvironments) ?: "Video chat declined"

    fun getMOTDText(preferredEnvironments: ArrayList<String>?): String =
            getString("motd", preferredEnvironments) ?: "motd"

    fun getInQueueMessageText(preferredEnvironments: ArrayList<String>?): String? =
            getString("inQueueText", preferredEnvironments)


    fun showRating(preferredEnvironments: ArrayList<String>?): Boolean =
            getBoolean("audienceRating", preferredEnvironments) ?: false

    fun getFeedbackTitleText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("How was our customer service?", preferredEnvironments)
                    ?: "How was our customer service?"

    fun getThankYouTextText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("Thank you for the conversation!", preferredEnvironments)
                    ?: "Thank you for the conversation!"

    fun getFeedbackPositiveText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("Good", preferredEnvironments) ?: "Good"

    fun getFeedbackNeutralText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("Okay", preferredEnvironments) ?: "Okay"

    fun getFeedbackNegativeText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("Poor", preferredEnvironments) ?: "Poor"

    fun getFeedbackSkipText(preferredEnvironments: ArrayList<String>?): String =
            getTranslation("Skip", preferredEnvironments) ?: "Skip"

    fun getQueueName(name: String, closed: Boolean = false, preferredEnvironments: ArrayList<String>?): String {
        return if (closed) {
            replacePlaceholder(getTranslation("Join audience queue {{audienceQueue.queue_attrs.name}} (closed)", preferredEnvironments), name);
        } else {
            replacePlaceholder(getTranslation("Join audience queue {{audienceQueue.queue_attrs.name}}", preferredEnvironments), name);
        }
    }

    fun getChatStarted(name: String?, preferredEnvironments: ArrayList<String>?): String =
            replacePlaceholder(getTranslation("Audience in queue {{queue}} accepted.", preferredEnvironments), name
                    ?: "");

    fun getQueueStatus(name: String?, position: Long = 0, preferredEnvironments: ArrayList<String>?): String {
        val key = if (position == 1L) {
            "Joined audience queue {{audienceQueue.queue_attrs.name}}, you are next."
        } else {
            "Joined audience queue {{audienceQueue.queue_attrs.name}}, you are at position {{audienceQueue.queue_position}}."
        }
        var queueStatus = getTranslation(key, preferredEnvironments)
        if (queueStatus?.contains("audienceQueue.queue_attrs.name") == true)
            queueStatus = replacePlaceholder(queueStatus, name ?: "")
        if (queueStatus?.contains("audienceQueue.queue_position") == true)
            queueStatus = replacePlaceholder(queueStatus, "$position")

        return queueStatus ?: ""
    }


    fun getQuestionnaireName(preferredEnvironments: ArrayList<String>?): String? =
            getString("questionnaireName", preferredEnvironments)

    fun getQuestionnaireAvatar(preferredEnvironments: ArrayList<String>?): String? =
            getString("questionnaireAvatar", preferredEnvironments)

    fun getAudienceRegisteredText(preferredEnvironments: ArrayList<String>?): String? =
            getString("audienceRegisteredText", preferredEnvironments)

    fun getAudienceRegisteredClosedText(preferredEnvironments: ArrayList<String>?): String? =
            getString("audienceRegisteredClosedText", preferredEnvironments)

    fun getPreAudienceQuestionnaire(preferredEnvironments: ArrayList<String>?): JSONArray? =
            getArray("preAudienceQuestionnaire", preferredEnvironments)

    fun getPostAudienceQuestionnaire(preferredEnvironments: ArrayList<String>?): JSONArray? =
            getArray("postAudienceQuestionnaire", preferredEnvironments)

    fun getPreAudienceQuestionnaireStyle(preferredEnvironments: ArrayList<String>?): String =
            getString("preAudienceQuestionnaireStyle", preferredEnvironments)
                    ?: "form" // default style is form

    fun getPostAudienceQuestionnaireStyle(preferredEnvironments: ArrayList<String>?): String =
            getString("postAudienceQuestionnaireStyle", preferredEnvironments)
                    ?: "form" // default style is form


    fun getTranslation(key: String?, preferredEnvironments: ArrayList<String>?): String? =
            getJsonObject("translations", preferredEnvironments)?.optString(key)

    fun replacePlaceholder(origin: String?, replacement: String): String {
        return origin?.replaceFirst("\\{\\{([^}]*?)}}".toRegex(), replacement) ?: replacement
    }


}