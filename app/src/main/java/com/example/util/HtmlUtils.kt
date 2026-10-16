package com.example.util

import android.os.Build
import android.text.Html
import java.util.regex.Pattern

object HtmlUtils {

    private val IMG_PATTERN = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)

    fun cleanHtml(html: String?): String {
        if (html.isNullOrBlank()) return ""
        val withoutCdata = html.replace("<![CDATA[", "").replace("]]>", "")
        val unescaped = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(withoutCdata, Html.FROM_HTML_MODE_COMPACT).toString()
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(withoutCdata).toString()
        }
        return unescaped
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    fun extractImageUrl(html: String?): String? {
        if (html.isNullOrBlank()) return null
        val matcher = IMG_PATTERN.matcher(html)
        if (matcher.find()) {
            val url = matcher.group(1)
            if (!url.isNullOrBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                return url
            }
        }
        return null
    }

    fun estimateReadTime(text: String): Int {
        val wordCount = text.split("\\s+".toRegex()).size
        return (wordCount / 180).coerceAtLeast(1)
    }
}
