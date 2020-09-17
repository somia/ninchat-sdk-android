package com.ninchat.sdk.helper.siteconfigparser

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
                siteConfig = JSONObject(value)
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
            for (i in 0 until array.length()) {
                queues.add(array.optString(i))
            }
        }
        return queues
    }

    fun getRealmId(preferredEnvironments: ArrayList<String>?): String? =
            getString("audienceRealmId", preferredEnvironments)

    fun getWelcomeText(preferredEnvironments: ArrayList<String>?): String =
            getString("welcome", preferredEnvironments) ?: "welcome"

    fun getNoQueuesText(preferredEnvironments: ArrayList<String>?): String =
            getString("noQueuesText", preferredEnvironments) ?: "noQueuesText"

    fun getCloseWindowText(preferredEnvironments: ArrayList<String>?): String =
            getString("Close window", preferredEnvironments) ?: "Close window"

    fun getUserName(preferredEnvironments: ArrayList<String>?): String? =
            getString("userName", preferredEnvironments)

    fun getAgentName(preferredEnvironments: ArrayList<String>?): String? =
            getString("agentName", preferredEnvironments)

    fun getSendButtonText(preferredEnvironments: ArrayList<String>?): String? =
            getString("sendButtonText", preferredEnvironments)

    fun getSubmitButtonText(preferredEnvironments: ArrayList<String>?): String? =
            getString("Submit", preferredEnvironments)

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

    fun getConversationEndedText(preferredEnvironments: ArrayList<String>?): String =
            getString("Conversation ended", preferredEnvironments) ?: "Conversation ended"

    fun getChatCloseText(preferredEnvironments: ArrayList<String>?): String =
            getString("Close chat", preferredEnvironments) ?: "Close chat"

    fun getChatCloseDescriptionText(preferredEnvironments: ArrayList<String>?): String =
            getString("closeConfirmText", preferredEnvironments) ?: "closeConfirmText"

    fun getContinueChatText(preferredEnvironments: ArrayList<String>?): String =
            getString("Continue chat", preferredEnvironments) ?: "Continue chat"

    fun getEnterMessageText(preferredEnvironments: ArrayList<String>?): String =
            getString("Enter your message", preferredEnvironments) ?: "Enter your message"

    fun getVideoChatTitleText(preferredEnvironments: ArrayList<String>?): String =
            getString("You are invited to a video chat", preferredEnvironments)
                    ?: "You are invited to a video chat"

    fun getVideoChatDescriptionText(preferredEnvironments: ArrayList<String>?): String =
            getString("wants to video chat with you", preferredEnvironments)
                    ?: "wants to video chat with you"

    fun getVideoCallAcceptText(preferredEnvironments: ArrayList<String>?): String =
            getString("Accept", preferredEnvironments) ?: "Accept"

    fun getVideoCallDeclineText(preferredEnvironments: ArrayList<String>?): String =
            getString("Decline", preferredEnvironments) ?: "Decline"

    fun getVideoCallMetaMessageText(preferredEnvironments: ArrayList<String>?): String =
            getString("You are invited to a video chat", preferredEnvironments)
                    ?: "You are invited to a video chat"

    fun getVideoCallAcceptedText(preferredEnvironments: ArrayList<String>?): String =
            getString("Video chat answered", preferredEnvironments) ?: "Video chat answered"

    fun getVideoCallRejectedText(preferredEnvironments: ArrayList<String>?): String =
            getString("Video chat declined", preferredEnvironments) ?: "Video chat declined"

    fun getMOTDText(preferredEnvironments: ArrayList<String>?): String =
            getString("motd", preferredEnvironments) ?: "motd"

    fun getQueueMessageText(preferredEnvironments: ArrayList<String>?): String =
            getString("inQueueText", preferredEnvironments) ?: "inQueueText"


    fun showRating(preferredEnvironments: ArrayList<String>?): Boolean =
            getBoolean("audienceRating", preferredEnvironments) ?: false

    fun getFeedbackTitleText(preferredEnvironments: ArrayList<String>?): String =
            getString("How was our customer service?", preferredEnvironments)
                    ?: "How was our customer service?"

    fun getThankYouTextText(preferredEnvironments: ArrayList<String>?): String =
            getString("Thank you for the conversation!", preferredEnvironments)
                    ?: "Thank you for the conversation!"

    fun getFeedbackPositiveText(preferredEnvironments: ArrayList<String>?): String =
            getString("Good", preferredEnvironments) ?: "Good"

    fun getFeedbackNeutralText(preferredEnvironments: ArrayList<String>?): String =
            getString("Okay", preferredEnvironments) ?: "Okay"

    fun getFeedbackNegativeText(preferredEnvironments: ArrayList<String>?): String =
            getString("Poor", preferredEnvironments) ?: "Poor"

    fun getFeedbackSkipText(preferredEnvironments: ArrayList<String>?): String =
            getString("Skip", preferredEnvironments) ?: "Skip"
}