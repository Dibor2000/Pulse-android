package com.example.data.remote

import android.util.Log
import android.util.Xml
import com.example.data.model.NewsArticle
import com.example.data.model.NewsSource
import com.example.util.HtmlUtils
import com.example.util.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class NewsRssFetcher {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun fetchFeed(source: NewsSource): List<NewsArticle> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(source.feedUrl)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36")
            .header("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml;q=0.9, */*;q=0.8")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w("NewsRssFetcher", "Failed to fetch ${source.name}: ${response.code}")
                    return@withContext emptyList()
                }

                val xmlBody = response.body?.string() ?: return@withContext emptyList()
                parseXmlFeed(xmlBody, source)
            }
        } catch (e: Exception) {
            Log.e("NewsRssFetcher", "Error fetching ${source.name}", e)
            emptyList()
        }
    }

    private fun parseXmlFeed(xml: String, source: NewsSource): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()
        try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var insideItem = false

            var title = ""
            var link = ""
            var description = ""
            var content = ""
            var pubDateStr = ""
            var imageUrl: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name?.lowercase() ?: ""

                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName == "item" || tagName == "entry") {
                            insideItem = true
                            title = ""
                            link = ""
                            description = ""
                            content = ""
                            pubDateStr = ""
                            imageUrl = null
                        } else if (insideItem) {
                            when (tagName) {
                                "title" -> {
                                    title = readElementContent(parser)
                                }
                                "link" -> {
                                    val href = parser.getAttributeValue(null, "href")
                                    val text = readElementContent(parser)
                                    link = if (!href.isNullOrBlank()) href else text
                                }
                                "description", "summary", "atom:summary" -> {
                                    val desc = readElementContent(parser)
                                    description = desc
                                    if (imageUrl == null) {
                                        imageUrl = HtmlUtils.extractImageUrl(desc)
                                    }
                                }
                                "content", "content:encoded", "atom:content", "turbo:content", "yandex:full-text" -> {
                                    val cnt = readElementContent(parser)
                                    content = cnt
                                    if (imageUrl == null) {
                                        imageUrl = HtmlUtils.extractImageUrl(cnt)
                                    }
                                }
                                "pubdate", "published", "updated", "dc:date" -> {
                                    pubDateStr = readElementContent(parser)
                                }
                                "enclosure", "media:content", "media:thumbnail" -> {
                                    val url = parser.getAttributeValue(null, "url")
                                    val type = parser.getAttributeValue(null, "type")
                                    if (!url.isNullOrBlank() && (type == null || type.startsWith("image") || url.contains(".jpg") || url.contains(".png") || url.contains(".webp") || url.contains(".jpeg"))) {
                                        if (imageUrl == null) {
                                            imageUrl = url
                                        }
                                    }
                                }
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        if ((tagName == "item" || tagName == "entry") && insideItem) {
                            insideItem = false

                            val cleanTitle = HtmlUtils.cleanHtml(title)
                            val cleanRawDesc = HtmlUtils.cleanHtml(description)
                            val cleanRawContent = HtmlUtils.cleanHtml(content)

                            // Pick best source for description and content
                            val rawBody = cleanRawContent.ifBlank { cleanRawDesc }
                            val cleanDesc = HtmlUtils.extractShortSummary(rawBody.ifBlank { cleanTitle })
                            val richContent = HtmlUtils.ensureRichContent(
                                existingContent = rawBody,
                                title = cleanTitle,
                                description = cleanDesc,
                                sourceName = source.name,
                                categoryName = source.category.displayName
                            )

                            if (cleanTitle.isNotBlank() && (link.isNotBlank() || richContent.isNotBlank())) {
                                val publishedAt = TimeUtils.parseDate(pubDateStr)
                                val articleId = generateId(link.ifBlank { cleanTitle })
                                val readTime = HtmlUtils.estimateReadTime(richContent)

                                val isBreaking = (System.currentTimeMillis() - publishedAt) < (30 * 60 * 1000) ||
                                        cleanTitle.contains("срочно", ignoreCase = true) ||
                                        cleanTitle.contains("молния", ignoreCase = true) ||
                                        cleanTitle.contains("breaking", ignoreCase = true)

                                articles.add(
                                    NewsArticle(
                                        id = articleId,
                                        title = cleanTitle,
                                        description = cleanDesc,
                                        content = richContent,
                                        link = link,
                                        sourceName = source.name,
                                        sourceUrl = source.url,
                                        imageUrl = imageUrl,
                                        category = source.category,
                                        publishedAt = publishedAt,
                                        isBreaking = isBreaking,
                                        readTimeMinutes = readTime,
                                        topicId = null // Do not forcibly attach to topic on launch
                                    )
                                )
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e("NewsRssFetcher", "Parsing error for ${source.name}", e)
        }

        return articles
    }

    /**
     * Safely reads text across child elements, CDATA, and entity references
     * without throwing XmlPullParserException when child tags like <p>, <img>, <a> are present.
     */
    private fun readElementContent(parser: XmlPullParser): String {
        val sb = StringBuilder()
        val startDepth = parser.depth
        var event = parser.eventType

        if (event == XmlPullParser.TEXT || event == XmlPullParser.CDSECT) {
            sb.append(parser.text ?: "")
        }

        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            event = parser.eventType
            if (event == XmlPullParser.END_TAG && parser.depth == startDepth) {
                break
            }
            when (event) {
                XmlPullParser.TEXT, XmlPullParser.CDSECT -> {
                    sb.append(parser.text ?: "")
                }
                XmlPullParser.START_TAG -> {
                    val subTag = parser.name?.lowercase() ?: ""
                    if (subTag == "img") {
                        val src = parser.getAttributeValue(null, "src")
                        if (!src.isNullOrBlank()) {
                            sb.append(" [img:$src] ")
                        }
                    } else if (subTag == "br" || subTag == "p") {
                        sb.append("\n\n")
                    }
                }
            }
        }
        return sb.toString()
    }

    private fun generateId(input: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(input.toByteArray())
            digest.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            input.hashCode().toString()
        }
    }
}
