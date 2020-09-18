package com.ninchat.sdk.utils.misc

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.webkit.MimeTypeMap

class Misc {
    companion object {
        @JvmStatic
        fun center(text: String?): String {
            return text?.let {
                if (it.contains("<center>") && it.contains("</center>")) {
                    it
                } else {
                    "<center> $it </center>"
                }
            } ?: "<center> </center>"
        }

        @JvmStatic
        fun toSpanned(text: String?): Spanned {
            val centeredText = center(text)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(centeredText, Html.FROM_HTML_MODE_LEGACY) else Html.fromHtml(centeredText)
        }

        @JvmStatic
        fun guessMimeTypeFromFileName(name: String?): String {
            val extension = name?.replace(".*\\.".toRegex(), "")
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                    ?: return "application/octet-stream"
        }
    }
}