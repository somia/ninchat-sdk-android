package com.ninchat.sdk.ninchatmedia.model

import android.os.Build
import android.text.Html
import android.text.Spanned
import java.util.*

data class NinchatFile(val messageId: String?, val id: String, val name: String?, private val size: Int, private val type: String, val timestamp: Long, val sender: String?, val senderName: String?, val isRemote: Boolean) {
    var url: String? = null
    var thumbnailUrl: String? = null
    var urlExpiry: Date? = null
    var aspectRatio = 0f
    var fileWidth: Long = 0
    var fileHeight: Long = 0
    var isDownloadableFile = false
    var isDownloaded = false

    val isVideo: Boolean
        get() = type.startsWith("video/")

    fun getWidth(): Int {
        return fileWidth.toInt()
    }

    fun getHeight(): Int {
        return fileHeight.toInt()
    }

    // TODO: Should we support gigabytes and terabytes too?
    val fileSize: String
        get() {
            if (size / 1024 == 0) {
                return size.toString() + "B"
            }
            val kiloBytes = size / 1024
            if (kiloBytes / 1024 == 0) {
                return kiloBytes.toString() + "kB"
            }
            val megaBytes = kiloBytes / 1024
            // TODO: Should we support gigabytes and terabytes too?
            return megaBytes.toString() + "MB"
        }
    val fileLink: Spanned
        get() {
            val link = "<a href='$url'>$name</a> ($fileSize)"
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(link, Html.FROM_HTML_MODE_LEGACY) else Html.fromHtml(link)
        }
}