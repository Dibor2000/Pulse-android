package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.NewsArticle
import com.example.data.model.NewsCategory

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val content: String,
    val link: String,
    val sourceName: String,
    val sourceUrl: String,
    val imageUrl: String?,
    val category: String,
    val publishedAt: Long,
    val isBreaking: Boolean,
    val isBookmarked: Boolean,
    val isRead: Boolean,
    val readTimeMinutes: Int,
    val topicId: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): NewsArticle = NewsArticle(
        id = id,
        title = title,
        description = description,
        content = content,
        link = link,
        sourceName = sourceName,
        sourceUrl = sourceUrl,
        imageUrl = imageUrl,
        category = NewsCategory.fromString(category),
        publishedAt = publishedAt,
        isBreaking = isBreaking,
        isBookmarked = isBookmarked,
        isRead = isRead,
        readTimeMinutes = readTimeMinutes,
        topicId = topicId
    )

    companion object {
        fun fromDomain(article: NewsArticle, existingBookmark: Boolean = false, existingRead: Boolean = false): ArticleEntity {
            return ArticleEntity(
                id = article.id,
                title = article.title,
                description = article.description,
                content = article.content,
                link = article.link,
                sourceName = article.sourceName,
                sourceUrl = article.sourceUrl,
                imageUrl = article.imageUrl,
                category = article.category.name,
                publishedAt = article.publishedAt,
                isBreaking = article.isBreaking,
                isBookmarked = if (article.isBookmarked) true else existingBookmark,
                isRead = if (article.isRead) true else existingRead,
                readTimeMinutes = article.readTimeMinutes,
                topicId = article.topicId
            )
        }
    }
}
