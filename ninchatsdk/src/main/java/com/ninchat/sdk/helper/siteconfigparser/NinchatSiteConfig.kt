package com.ninchat.sdk.helper.siteconfigparser

import org.json.JSONArray
import org.json.JSONObject

/**
 * Ninchat site configuration data class and parser
 */
class NinchatSiteConfig {
    var siteConfig: JSONObject? = null
    private var preferredEnvironments: ArrayList<String>? = null
    fun setConfigString(value: String?, preferredEnvironments: ArrayList<String>? = null) {
        try {
            this.preferredEnvironments = sanitizePreferredEnvironments(preferredEnvironments)
            value?.let {
                siteConfig = JSONObject(it)
            }
        } catch (_: Exception) {
        }
    }

    internal fun sanitizePreferredEnvironments(preferredEnvironments: ArrayList<String>? = null): ArrayList<String> {
        preferredEnvironments?.let {
            if (!it.contains("default")) {
                preferredEnvironments.add(0, "default")
            }
        }
        return preferredEnvironments ?: arrayListOf("default")
    }

    fun getDefault(): JSONObject? {
        return siteConfig?.optJSONObject("default")
    }

    fun getArray(key: String): JSONArray? {
        var value: JSONArray? = null
        siteConfig?.let {
            preferredEnvironments?.let {
                for (currentEnvironment in it) {
                    if (siteConfig?.optJSONObject(currentEnvironment)?.has(key) == true) {
                        value = siteConfig?.optJSONObject(currentEnvironment)?.optJSONArray(key)
                    }
                }
            }
        }
        return value
    }

    fun getBoolean(key: String): Boolean? {
        var value = false
        var found = false;
        siteConfig?.let {
            preferredEnvironments?.let {
                for (currentEnvironment in it) {
                    if (siteConfig?.optJSONObject(currentEnvironment)?.has(key) == true) {
                        found = true
                        value = siteConfig?.optJSONObject(currentEnvironment)?.optBoolean(key, true)
                                ?: false
                    }
                }
            }
        }
        return if (found) value else null
    }

    fun getString(key: String): String? {
        var value: String? = null
        siteConfig?.let {
            preferredEnvironments?.let {
                for (currentEnvironment in it) {
                    if (siteConfig?.optJSONObject(currentEnvironment)?.has(key) == true) {
                        value = siteConfig?.optJSONObject(currentEnvironment)?.optString(key, "null")
                        // workaround value can be null which when parsing will be interpreted as "null" string in Java with quote
                        if ("null" == value || "false" == value) {
                            value = null
                        }
                    }
                }
            }
        }
        return value
    }

    fun getAudienceQueues(): Collection<String>? {
        val queues = mutableListOf<String>()
        val array: JSONArray? = getArray("audienceQueues")
        array?.let {
            for (i in 0 until it.length()) {
                queues.add(it.optString(i))
            }
        }
        return queues
    }

    fun getAudienceAutoQueue(): String? =
            getString("audienceAutoQueue")

    fun getRealmId(): String? =
            getString("audienceRealmId")

    fun getWelcomeText(): String =
            getString("welcome") ?: "welcome"

    fun getNoQueuesText(): String =
            getString("noQueuesText") ?: ""

    fun getCloseWindowText(): String =
            getTranslation("Close window")

    fun getUserName(): String? =
            getString("userName")

    fun getAgentName(): String? =
            getString("agentName")

    fun getSendButtonText(): String? =
            getString("sendButtonText")

    fun getSubmitButtonText(): String =
            getTranslation("Submit")

    fun isAttachmentsEnabled(): Boolean =
            getBoolean("supportFiles") ?: false

    fun isVideoEnabled(): Boolean =
            getBoolean("supportVideo") ?: false

    fun showUserAvatar(): Boolean =
            getBoolean("userAvatar") ?: false

    fun showAgentAvatar(): Boolean =
            getBoolean("agentAvatar") ?: false

    fun getAgentAvatar(): String? =
            getString("agentAvatar")

    fun getUserAvatar(): String? =
            getString("userAvatar")

    fun getConversationEndedText(): String =
            getTranslation("Conversation ended")

    fun getChatCloseText(): String =
            getTranslation("Close chat")

    fun getChatCloseConfirmationText(): String =
            getString("closeConfirmText") ?: ""

    fun getContinueChatText(): String =
            getTranslation("Continue chat")

    fun getEnterMessageText(): String =
            getTranslation("Enter your message")

    fun getVideoChatTitleText(): String =
            getTranslation("You are invited to a video chat")

    fun getVideoChatDescriptionText(): String =
            getTranslation("wants to video chat with you")

    fun getVideoCallAcceptText(): String =
            getTranslation("Accept")

    fun getVideoCallDeclineText(): String =
            getTranslation("Decline")

    fun getVideoCallMetaMessageText(): String =
            getTranslation("You are invited to a video chat")

    fun getVideoCallAcceptedText(): String =
            getTranslation("Video chat answered")

    fun getVideoCallRejectedText(): String =
            getTranslation("Video chat declined")

    fun getMOTDText(): String =
            getString("motd") ?: ""

    fun getInQueueMessageText(): String? =
            getString("inQueueText")

    fun showRating(): Boolean =
            getBoolean("audienceRating") ?: false

    fun getFeedbackTitleText(): String =
            getTranslation("How was our customer service?")

    fun getThankYouTextText(): String =
            getTranslation("Thank you for the conversation!")

    fun getFeedbackPositiveText(): String =
            getTranslation("Good")

    fun getFeedbackNeutralText(): String =
            getTranslation("Okay")

    fun getFeedbackNegativeText(): String =
            getTranslation("Poor")

    fun getFeedbackSkipText(): String =
            getTranslation("Skip")

    fun getQueueName(name: String, closed: Boolean = false): String {
        return if (closed) {
            replacePlaceholder(getTranslation("Join audience queue {{audienceQueue.queue_attrs.name}} (closed)"), name);
        } else {
            replacePlaceholder(getTranslation("Join audience queue {{audienceQueue.queue_attrs.name}}"), name);
        }
    }

    fun getChatStarted(name: String?): String =
            replacePlaceholder(getTranslation("Audience in queue {{queue}} accepted."), name
                    ?: "");

    fun getQueueStatus(name: String?, position: Long = 0): String {
        val key = if (position == 1L) {
            "Joined audience queue {{audienceQueue.queue_attrs.name}}, you are next."
        } else {
            "Joined audience queue {{audienceQueue.queue_attrs.name}}, you are at position {{audienceQueue.queue_position}}."
        }
        var queueStatus = getTranslation(key) ?: key
        if (queueStatus.contains("audienceQueue.queue_attrs.name"))
            queueStatus = replacePlaceholder(queueStatus, name ?: "")
        if (queueStatus.contains("audienceQueue.queue_position"))
            queueStatus = replacePlaceholder(queueStatus, "$position")

        return queueStatus
    }


    fun getQuestionnaireName(): String? =
            getString("questionnaireName")

    fun getQuestionnaireAvatar(): String? =
            getString("questionnaireAvatar")

    fun getAudienceRegisteredText(): String? =
            getString("audienceRegisteredText")

    fun getAudienceRegisteredClosedText(): String? =
            getString("audienceRegisteredClosedText")

    fun getPreAudienceQuestionnaire(): JSONArray? =
            getArray("preAudienceQuestionnaire")

    fun getPostAudienceQuestionnaire(): JSONArray? =
            getArray("postAudienceQuestionnaire")

    fun getPreAudienceQuestionnaireStyle(): String =
            getString("preAudienceQuestionnaireStyle")
                    ?: "form" // default style is form

    fun getPostAudienceQuestionnaireStyle(): String =
            getString("postAudienceQuestionnaireStyle")
                    ?: "form" // default style is form


    internal fun getTranslation(translationKey: String = "translations", key: String): String? {
        var value: String? = null
        siteConfig?.let {
            preferredEnvironments?.let {
                for (currentEnvironment in it) {
                    if (siteConfig?.optJSONObject(currentEnvironment)?.optJSONObject(translationKey)?.has(key) == true) {
                        value = siteConfig?.optJSONObject(currentEnvironment)?.optJSONObject(translationKey)?.optString(key)
                    }
                }
            }
        }
        return value
    }

    // todo (pallab) move to translator package
    fun getTranslation(key: String = ""): String =
            getTranslation("translations", key) ?: key

    // todo (pallab) move to translator or general util/helper package
    fun replacePlaceholder(origin: String?, replacement: String): String {
        return origin?.replaceFirst("\\{\\{([^}]*?)\\}\\}".toRegex(), replacement) ?: replacement
    }


}