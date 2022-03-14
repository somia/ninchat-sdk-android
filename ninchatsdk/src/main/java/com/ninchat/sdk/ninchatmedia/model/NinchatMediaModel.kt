package com.ninchat.sdk.ninchatmedia.model

import com.ninchat.sdk.NinchatSessionManager

class NinchatMediaModel {
    var fileId: String? = null

    fun isDownloaded(): Boolean {
        return getFile()?.isDownloaded ?: false
    }

    fun getFile(): NinchatFile? {
        return NinchatSessionManager.getInstance()?.ninchatState?.getFile(fileId)
    }

    companion object {
        const val FILE_ID = "fileId"
    }

    fun translate() = NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getTranslation("Loading image")
            ?: "Loading image"
}