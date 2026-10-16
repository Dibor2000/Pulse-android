package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY publishedAt DESC")
    fun getBookmarkedArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    suspend fun getArticleById(id: String): ArticleEntity?

    @Query("SELECT * FROM articles WHERE topicId = :topicId ORDER BY publishedAt ASC")
    fun getArticlesByTopic(topicId: String): Flow<List<ArticleEntity>>

    @Query("UPDATE articles SET topicId = :topicId WHERE id = :articleId")
    suspend fun updateArticleTopic(articleId: String, topicId: String?)

    @Query("SELECT isBookmarked FROM articles WHERE id = :id LIMIT 1")
    suspend fun isArticleBookmarked(id: String): Boolean?

    @Query("SELECT isRead FROM articles WHERE id = :id LIMIT 1")
    suspend fun isArticleRead(id: String): Boolean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Query("UPDATE articles SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmark(id: String, isBookmarked: Boolean)

    @Query("UPDATE articles SET isRead = :isRead WHERE id = :id")
    suspend fun updateReadStatus(id: String, isRead: Boolean)

    @Query("DELETE FROM articles WHERE isBookmarked = 0 AND cachedAt < :expiryThreshold")
    suspend fun deleteOldUnsavedArticles(expiryThreshold: Long)

    @Query("DELETE FROM articles WHERE isBookmarked = 0")
    suspend fun clearUnsavedArticles()
}
