package com.example.data.repository

import com.example.data.local.ArticleDao
import com.example.data.local.ArticleEntity
import com.example.data.model.ChronologyEvent
import com.example.data.model.MilestoneType
import com.example.data.model.NewsArticle
import com.example.data.model.NewsCategory
import com.example.data.model.NewsSource
import com.example.data.model.TopicStory
import com.example.data.remote.InitialNewsData
import com.example.data.remote.NewsRssFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewsRepository(
    private val articleDao: ArticleDao,
    private val rssFetcher: NewsRssFetcher = NewsRssFetcher(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val _sources = MutableStateFlow(InitialNewsData.getDefaultSources())
    val sources: StateFlow<List<NewsSource>> = _sources.asStateFlow()

    private val _topics = MutableStateFlow(InitialNewsData.getDefaultTopics())
    val topics: StateFlow<List<TopicStory>> = _topics.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _lastUpdated = MutableStateFlow(System.currentTimeMillis())
    val lastUpdated: StateFlow<Long> = _lastUpdated.asStateFlow()

    private val _autoRefreshIntervalSec = MutableStateFlow(30) // Default 30s
    val autoRefreshIntervalSec: StateFlow<Int> = _autoRefreshIntervalSec.asStateFlow()

    private val _countdownSeconds = MutableStateFlow(30)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

    private var autoRefreshJob: Job? = null
    private var countdownJob: Job? = null

    val allArticles: Flow<List<NewsArticle>> = articleDao.getAllArticles().map { entities ->
        entities.map { it.toDomain() }
    }

    val bookmarkedArticles: Flow<List<NewsArticle>> = articleDao.getBookmarkedArticles().map { entities ->
        entities.map { it.toDomain() }
    }

    init {
        scope.launch {
            try {
                // Check if DB is empty, seed with initial real-time articles
                val existing = articleDao.getAllArticles().first()
                if (existing.isEmpty()) {
                    val initial = InitialNewsData.getInitialArticles()
                    articleDao.insertArticles(initial.map { ArticleEntity.fromDomain(it) })
                }
            } catch (e: Exception) {
                android.util.Log.e("NewsRepository", "Error seeding initial database", e)
            }
            try {
                // Trigger fresh fetch
                refreshFeeds()
            } catch (e: Exception) {
                android.util.Log.e("NewsRepository", "Error in initial feeds refresh", e)
            }
            startAutoRefresh()
        }
    }

    fun setAutoRefreshInterval(seconds: Int) {
        _autoRefreshIntervalSec.value = seconds
        _countdownSeconds.value = seconds
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        countdownJob?.cancel()

        val interval = _autoRefreshIntervalSec.value
        if (interval <= 0) return // Manual mode

        _countdownSeconds.value = interval

        countdownJob = scope.launch {
            while (isActive) {
                delay(1000)
                if (_countdownSeconds.value > 1) {
                    _countdownSeconds.value -= 1
                } else {
                    _countdownSeconds.value = interval
                    refreshFeeds()
                }
            }
        }
    }

    suspend fun refreshFeeds() = withContext(Dispatchers.IO) {
        if (_isRefreshing.value) return@withContext
        _isRefreshing.value = true

        try {
            val enabledSources = _sources.value.filter { it.isEnabled }
            val newArticles = mutableListOf<NewsArticle>()

            val updatedSources = _sources.value.map { source ->
                if (!source.isEnabled) return@map source
                val fetched = rssFetcher.fetchFeed(source)
                newArticles.addAll(fetched)
                source.copy(
                    lastFetchedCount = fetched.size,
                    status = if (fetched.isNotEmpty()) "Онлайн (${fetched.size})" else "Доступен"
                )
            }
            _sources.value = updatedSources

            if (newArticles.isNotEmpty()) {
                val entitiesToInsert = newArticles.map { article ->
                    val existingBookmark = articleDao.isArticleBookmarked(article.id) ?: false
                    val existingRead = articleDao.isArticleRead(article.id) ?: false
                    ArticleEntity.fromDomain(article, existingBookmark, existingRead)
                }
                articleDao.insertArticles(entitiesToInsert)
            }

            _lastUpdated.value = System.currentTimeMillis()
        } catch (_: Exception) {
            // Fallback gracefully
        } finally {
            _isRefreshing.value = false
            _countdownSeconds.value = _autoRefreshIntervalSec.value
        }
    }

    suspend fun toggleBookmark(articleId: String) = withContext(Dispatchers.IO) {
        val current = articleDao.isArticleBookmarked(articleId) ?: false
        articleDao.updateBookmark(articleId, !current)
    }

    suspend fun markAsRead(articleId: String) = withContext(Dispatchers.IO) {
        articleDao.updateReadStatus(articleId, true)
    }

    fun toggleSource(sourceId: String) {
        _sources.value = _sources.value.map { source ->
            if (source.id == sourceId) {
                source.copy(isEnabled = !source.isEnabled)
            } else {
                source
            }
        }
        scope.launch {
            refreshFeeds()
        }
    }

    fun togglePinTopic(topicId: String, context: android.content.Context) {
        var newlyPinnedTopic: com.example.data.model.TopicStory? = null
        _topics.value = _topics.value.map { topic ->
            if (topic.id == topicId) {
                val nextPinned = !topic.isPinned
                val updated = topic.copy(isPinned = nextPinned)
                if (nextPinned) newlyPinnedTopic = updated
                updated
            } else {
                topic
            }
        }

        newlyPinnedTopic?.let { topic ->
            com.example.util.NotificationHelper.sendNotification(
                context = context,
                title = "📌 Тема закреплена для отслеживания",
                message = "Вы подписались на обновления по сюжету «${topic.title}». Мы пришлем уведомление при появлении новых материалов.",
                topicTitle = topic.title
            )
        }
    }

    fun sendTestNotification(topicId: String, context: android.content.Context) {
        val topic = _topics.value.find { it.id == topicId } ?: return
        val lastEvent = topic.events.lastOrNull()
        com.example.util.NotificationHelper.sendNotification(
            context = context,
            title = "🔴 Молния: «${topic.title.take(35)}...»",
            message = lastEvent?.summary ?: "Новое развитие событий по отслеживаемому сюжету.",
            topicTitle = topic.title,
            articleId = lastEvent?.articleId
        )
    }

    fun createTopic(
        title: String,
        summary: String,
        category: NewsCategory,
        initialArticle: NewsArticle? = null,
        context: android.content.Context? = null
    ): String {
        val topicId = "topic_${System.currentTimeMillis()}"
        val initialEvents = if (initialArticle != null) {
            listOf(
                com.example.data.model.ChronologyEvent(
                    id = "ev_${System.currentTimeMillis()}",
                    topicId = topicId,
                    title = initialArticle.title,
                    summary = initialArticle.description.ifBlank { "Первое упоминание в ленте новостей." },
                    timestamp = initialArticle.publishedAt,
                    sourceName = initialArticle.sourceName,
                    milestoneType = com.example.data.model.MilestoneType.START,
                    articleId = initialArticle.id
                )
            )
        } else emptyList()

        val newTopic = com.example.data.model.TopicStory(
            id = topicId,
            title = title,
            summary = summary,
            category = category,
            isPinned = true,
            events = initialEvents,
            articleIds = if (initialArticle != null) listOf(initialArticle.id) else emptyList()
        )

        _topics.value = listOf(newTopic) + _topics.value

        if (initialArticle != null) {
            scope.launch {
                articleDao.updateArticleTopic(initialArticle.id, topicId)
            }
        }

        context?.let { ctx ->
            com.example.util.NotificationHelper.sendNotification(
                context = ctx,
                title = "✨ Создан новый сюжет",
                message = "Сюжет «$title» создан и закреплен для отслеживания хронологии.",
                topicTitle = title
            )
        }

        return topicId
    }

    fun addArticleToTopic(topicId: String, article: NewsArticle, context: android.content.Context? = null) {
        val targetTopic = _topics.value.find { it.id == topicId } ?: return
        val newEvent = com.example.data.model.ChronologyEvent(
            id = "ev_${System.currentTimeMillis()}",
            topicId = topicId,
            title = article.title,
            summary = article.description.ifBlank { article.title },
            timestamp = article.publishedAt,
            sourceName = article.sourceName,
            milestoneType = if (article.isBreaking) com.example.data.model.MilestoneType.BREAKING else com.example.data.model.MilestoneType.DEVELOPMENT,
            articleId = article.id
        )

        _topics.value = _topics.value.map { topic ->
            if (topic.id == topicId) {
                topic.copy(
                    events = (topic.events + newEvent).sortedBy { it.timestamp },
                    articleIds = (topic.articleIds + article.id).distinct(),
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                topic
            }
        }

        scope.launch {
            articleDao.updateArticleTopic(article.id, topicId)
        }

        if (targetTopic.isPinned && context != null) {
            com.example.util.NotificationHelper.sendNotification(
                context = context,
                title = "⚡ Новое развитие сюжета: ${targetTopic.title.take(30)}...",
                message = article.title,
                topicTitle = targetTopic.title,
                articleId = article.id
            )
        }
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        articleDao.clearUnsavedArticles()
        val initial = InitialNewsData.getInitialArticles()
        articleDao.insertArticles(initial.map { ArticleEntity.fromDomain(it) })
        _topics.value = InitialNewsData.getDefaultTopics()
        _lastUpdated.value = System.currentTimeMillis()
    }
}
