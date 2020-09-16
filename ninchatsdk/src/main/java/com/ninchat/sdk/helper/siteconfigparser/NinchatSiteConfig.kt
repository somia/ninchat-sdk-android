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

    fun getDefault(): JSONObject? {
        return siteConfig?.optJSONObject("default")
    }

    fun getArray(key: String, preferredEnvironments: ArrayList<String>?): JSONArray? {
        if (siteConfig == null) return null
        if (preferredEnvironments == null) getDefault()?.optJSONArray(key)

        for (configuration in preferredEnvironments!!) {
            val array = siteConfig?.optJSONObject(configuration)?.optJSONArray(key)
            if (array != null) {
                return array
            }
        }
        return null
    }

    fun getBoolean(key: String, preferredEnvironments: ArrayList<String>?): Boolean? {
        if (siteConfig == null) return false
        if (preferredEnvironments == null) getDefault()?.optBoolean(key, false)

        for (currentEnvironment in preferredEnvironments!!) {
            val value = siteConfig?.optJSONObject(currentEnvironment)?.optBoolean(key, false)
            if (value != null) {
                return value
            }
        }
        return false
    }

    fun getString(key: String, preferredEnvironments: ArrayList<String>?): String? {
        if (siteConfig == null) return null
        if (preferredEnvironments == null) getDefault()?.optString(key, null)

        for (currentEnvironment in preferredEnvironments!!) {
            val value = siteConfig?.optJSONObject(currentEnvironment)?.optString(key, null)
            if (value != null) {
                return value
            }
        }
        return null
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
}