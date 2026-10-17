package com.example.data.repository

import com.example.data.local.ArticleDao
import com.example.data.local.ArticleEntity
import com.example.data.local.TopicDao
import com.example.data.local.TopicEntity
import com.example.data.model.ChronologyEvent
import com.example.data.model.MilestoneType
import com.example.data.model.NewsArticle
import com.example.data.model.NewsCategory
import com.example.data.model.NewsSource
import com.example.data.model.TopicStory
import com.example.data.remote.InitialNewsData
import com.example.data.remote.NewsRssFetcher
import com.example.util.NewsPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

class NewsRepository(
    private val articleDao: ArticleDao,
    private val topicDao: TopicDao,
    val preferences: NewsPreferences,
    private val rssFetcher: NewsRssFetcher = NewsRssFetcher(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val _sources = MutableStateFlow(
        run {
            val enabledIds = preferences.getEnabledSourceIds()
            val favoriteIds = preferences.getFavoriteSourceIds()
            InitialNewsData.getDefaultSources().map { source ->
                source.copy(
                    isEnabled = enabledIds.contains(source.id),
                    isFavorite = favoriteIds.contains(source.id)
                )
            }
        }
    )
    val sources: StateFlow<List<NewsSource>> = _sources.asStateFlow()

    val topics: Flow<List<TopicStory>> = topicDao.getAllTopics().map { entities ->
        entities.map { it.toDomain() }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _lastUpdated = MutableStateFlow(System.currentTimeMillis())
    val lastUpdated: StateFlow<Long> = _lastUpdated.asStateFlow()

    private val _autoRefreshIntervalSec = MutableStateFlow(60) // Default 60s
    val autoRefreshIntervalSec: StateFlow<Int> = _autoRefreshIntervalSec.asStateFlow()

    private val _countdownSeconds = MutableStateFlow(60)
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
                // Trigger fast progressive fetch
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
            if (enabledSources.isEmpty()) return@withContext

            // Pre-fetch bookmarked and read IDs once to avoid O(N) database roundtrips
            val bookmarkedIds = try {
                articleDao.getBookmarkedArticleIds().toSet()
            } catch (_: Exception) {
                emptySet()
            }
            val readIds = try {
                articleDao.getReadArticleIds().toSet()
            } catch (_: Exception) {
                emptySet()
            }

            // Prioritize favorites and Russian sources first for instant visible feedback
            val prioritizedSources = enabledSources.sortedWith(
                compareByDescending<NewsSource> { it.isFavorite }
                    .thenByDescending { it.language == "RU" }
            )

            // Concurrency pool of 6 parallel workers
            val concurrencyLimit = Semaphore(6)
            val updatedSourcesMap = _sources.value.associateBy { it.id }.toMutableMap()

            coroutineScope {
                prioritizedSources.map { source ->
                    async {
                        concurrencyLimit.withPermit {
                            val fetched = rssFetcher.fetchFeed(source)
                            if (fetched.isNotEmpty()) {
                                // Progressive insert: as soon as any source finishes,
                                // immediately save to Room DB so the feed advances in real time!
                                val entities = fetched.map { article ->
                                    val isBookmarked = bookmarkedIds.contains(article.id)
                                    val isRead = readIds.contains(article.id)
                                    ArticleEntity.fromDomain(article, isBookmarked, isRead)
                                }
                                articleDao.insertArticles(entities)
                            }

                            synchronized(updatedSourcesMap) {
                                updatedSourcesMap[source.id] = source.copy(
                                    lastFetchedCount = fetched.size,
                                    status = if (fetched.isNotEmpty()) "Онлайн (${fetched.size})" else "Доступен"
                                )
                                _sources.value = _sources.value.map { updatedSourcesMap[it.id] ?: it }
                            }
                        }
                    }
                }.awaitAll()
            }

            _lastUpdated.value = System.currentTimeMillis()
        } catch (e: Exception) {
            android.util.Log.e("NewsRepository", "Error refreshing feeds", e)
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
        val enabledIds = _sources.value.filter { it.isEnabled }.map { it.id }.toSet()
        preferences.setEnabledSourceIds(enabledIds)
        scope.launch {
            refreshFeeds()
        }
    }

    fun toggleFavoriteSource(sourceId: String) {
        _sources.value = _sources.value.map { source ->
            if (source.id == sourceId) {
                source.copy(isFavorite = !source.isFavorite)
            } else {
                source
            }
        }
        val favoriteIds = _sources.value.filter { it.isFavorite }.map { it.id }.toSet()
        preferences.setFavoriteSourceIds(favoriteIds)
    }

    fun deleteTopic(topicId: String) {
        scope.launch {
            topicDao.deleteTopic(topicId)
            try {
                val articles = articleDao.getAllArticles().first()
                articles.filter { it.topicId == topicId }.forEach { article ->
                    articleDao.updateArticleTopic(article.id, null)
                }
            } catch (_: Exception) {}
        }
    }

    fun archiveTopic(topicId: String, isArchived: Boolean) {
        scope.launch {
            topicDao.updateArchived(topicId, isArchived)
        }
    }

    fun togglePinTopic(topicId: String, context: android.content.Context) {
        scope.launch {
            val entity = topicDao.getTopicById(topicId) ?: return@launch
            val nextPinned = !entity.isPinned
            topicDao.updatePinned(topicId, nextPinned)

            if (nextPinned) {
                com.example.util.NotificationHelper.sendNotification(
                    context = context,
                    title = "📌 Сюжет закреплен для отслеживания",
                    message = "Вы подписались на обновления по сюжету «${entity.title}». Мы пришлем уведомление при появлении новых материалов.",
                    topicTitle = entity.title
                )
            }
        }
    }

    fun sendTestNotification(topicId: String, context: android.content.Context) {
        scope.launch {
            val entity = topicDao.getTopicById(topicId) ?: return@launch
            val domain = entity.toDomain()
            val lastEvent = domain.events.lastOrNull()
            com.example.util.NotificationHelper.sendNotification(
                context = context,
                title = "🔴 Молния: «${domain.title.take(35)}...»",
                message = lastEvent?.summary ?: "Новое развитие событий по отслеживаемому сюжету.",
                topicTitle = domain.title,
                articleId = lastEvent?.articleId
            )
        }
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
            isPinned = false,
            isArchived = false,
            events = initialEvents,
            articleIds = if (initialArticle != null) listOf(initialArticle.id) else emptyList()
        )

        scope.launch {
            topicDao.insertTopic(TopicEntity.fromDomain(newTopic))
            if (initialArticle != null) {
                articleDao.updateArticleTopic(initialArticle.id, topicId)
            }
        }

        context?.let { ctx ->
            com.example.util.NotificationHelper.sendNotification(
                context = ctx,
                title = "✨ Создан новый сюжет",
                message = "Сюжет «$title» создан для отслеживания хронологии.",
                topicTitle = title
            )
        }

        return topicId
    }

    fun addArticleToTopic(topicId: String, article: NewsArticle, context: android.content.Context? = null) {
        scope.launch {
            val entity = topicDao.getTopicById(topicId) ?: return@launch
            val domain = entity.toDomain()
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

            val updatedTopic = domain.copy(
                events = (domain.events + newEvent).sortedBy { it.timestamp },
                articleIds = (domain.articleIds + article.id).distinct(),
                updatedAt = System.currentTimeMillis()
            )
            topicDao.insertTopic(TopicEntity.fromDomain(updatedTopic))
            articleDao.updateArticleTopic(article.id, topicId)

            if (domain.isPinned && context != null) {
                com.example.util.NotificationHelper.sendNotification(
                    context = context,
                    title = "⚡ Новое развитие сюжета: ${domain.title.take(30)}...",
                    message = article.title,
                    topicTitle = domain.title,
                    articleId = article.id
                )
            }
        }
    }

    fun setBackgroundSyncEnabled(enabled: Boolean, context: android.content.Context) {
        preferences.isBackgroundSyncEnabled = enabled
        if (enabled) {
            com.example.service.NewsBackgroundSyncReceiver.scheduleSync(context, preferences.backgroundSyncIntervalMinutes)
        } else {
            com.example.service.NewsBackgroundSyncReceiver.cancelSync(context)
        }
    }

    fun setBackgroundSyncInterval(minutes: Int, context: android.content.Context) {
        preferences.backgroundSyncIntervalMinutes = minutes
        if (preferences.isBackgroundSyncEnabled) {
            com.example.service.NewsBackgroundSyncReceiver.scheduleSync(context, minutes)
        }
    }

    fun triggerBackgroundSyncNow(context: android.content.Context, onComplete: (Int) -> Unit) {
        scope.launch {
            val count = com.example.service.NewsBackgroundSyncReceiver.performSync(context, preferences)
            onComplete(count)
        }
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        articleDao.clearUnsavedArticles()
        val initial = InitialNewsData.getInitialArticles()
        articleDao.insertArticles(initial.map { ArticleEntity.fromDomain(it) })
        _lastUpdated.value = System.currentTimeMillis()
    }
}
