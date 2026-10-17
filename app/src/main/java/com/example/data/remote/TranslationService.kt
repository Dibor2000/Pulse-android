package com.example.data.remote

import android.util.Log
import com.example.data.model.NewsArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class SupportedLanguage(
    val code: String,
    val displayName: String,
    val flag: String
)

object TranslationLanguages {
    val ALL = listOf(
        SupportedLanguage("ru", "Русский", "🇷🇺"),
        SupportedLanguage("en", "English", "🇬🇧"),
        SupportedLanguage("de", "Deutsch", "🇩🇪"),
        SupportedLanguage("fr", "Français", "🇫🇷"),
        SupportedLanguage("es", "Español", "🇪🇸"),
        SupportedLanguage("zh", "中文", "🇨🇳")
    )

    fun getByCode(code: String): SupportedLanguage {
        return ALL.find { it.code.equals(code, ignoreCase = true) } ?: ALL[0]
    }
}

class TranslationService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    // Cache: key = "$targetLang:$textHash" -> translatedText
    private val translationCache = ConcurrentHashMap<String, String>()

    suspend fun translateText(text: String, targetLang: String): String = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext text
        val cacheKey = "$targetLang:${text.trim().hashCode()}"
        translationCache[cacheKey]?.let { return@withContext it }

        try {
            // For longer texts, break by paragraphs to keep requests crisp and prevent URL truncation
            val paragraphs = text.split("\n\n").filter { it.isNotBlank() }
            if (paragraphs.size > 1) {
                val translatedParagraphs = paragraphs.map { p -> translateChunk(p, targetLang) }
                val result = translatedParagraphs.joinToString("\n\n")
                translationCache[cacheKey] = result
                return@withContext result
            }

            val result = translateChunk(text, targetLang)
            translationCache[cacheKey] = result
            result
        } catch (e: Exception) {
            Log.w("TranslationService", "Translation failed for text: ${e.message}")
            text
        }
    }

    private fun translateChunk(chunk: String, targetLang: String): String {
        if (chunk.isBlank()) return chunk
        val trimmed = chunk.trim()
        val cacheKey = "$targetLang:${trimmed.hashCode()}"
        translationCache[cacheKey]?.let { return it }

        val encoded = URLEncoder.encode(trimmed.take(1800), "UTF-8")
        val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=$targetLang&dt=t&q=$encoded"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return trimmed
            val body = response.body?.string() ?: return trimmed

            val jsonArray = JSONArray(body)
            val sentencesArray = jsonArray.optJSONArray(0) ?: return trimmed
            val sb = StringBuilder()
            for (i in 0 until sentencesArray.length()) {
                val sentence = sentencesArray.optJSONArray(i)
                if (sentence != null) {
                    val part = sentence.optString(0)
                    if (part.isNotBlank() && part != "null") {
                        sb.append(part)
                    }
                }
            }
            val res = sb.toString().trim()
            if (res.isNotBlank()) {
                translationCache[cacheKey] = res
                return res
            }
            return trimmed
        }
    }

    suspend fun translateArticle(article: NewsArticle, targetLang: String): NewsArticle = withContext(Dispatchers.IO) {
        val translatedTitle = translateText(article.title, targetLang)
        val translatedDesc = translateText(article.description, targetLang)
        val translatedContent = translateText(article.content, targetLang)

        article.copy(
            title = translatedTitle,
            description = translatedDesc,
            content = translatedContent
        )
    }
}
