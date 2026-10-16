package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.NewsDatabase
import com.example.data.model.NewsArticle
import com.example.data.model.NewsCategory
import com.example.data.model.NewsSource
import com.example.data.repository.NewsRepository
import com.example.util.AudioNewsReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LayoutMode {
    CARDS, COMPACT
}

enum class NavTab(val title: String) {
    FEED("Лента"),
    STORIES("Сюжеты"),
    BOOKMARKS("Закладки"),
    SOURCES("Источники"),
    SETTINGS("Настройки")
}

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NewsDatabase.getInstance(application)
    private val repository = NewsRepository(database.articleDao(), scope = viewModelScope)
    val audioReader = AudioNewsReader(application)

    private val _currentTab = MutableStateFlow(NavTab.FEED)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow(NewsCategory.ALL)
    val selectedCategory: StateFlow<NewsCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _filterUnreadOnly = MutableStateFlow(false)
    val filterUnreadOnly: StateFlow<Boolean> = _filterUnreadOnly.asStateFlow()

    private val _layoutMode = MutableStateFlow(LayoutMode.CARDS)
    val layoutMode: StateFlow<LayoutMode> = _layoutMode.asStateFlow()

    private val _selectedArticle = MutableStateFlow<NewsArticle?>(null)
    val selectedArticle: StateFlow<NewsArticle?> = _selectedArticle.asStateFlow()

    private val _selectedTopic = MutableStateFlow<com.example.data.model.TopicStory?>(null)
    val selectedTopic: StateFlow<com.example.data.model.TopicStory?> = _selectedTopic.asStateFlow()

    private val _isNotificationPanelOpen = MutableStateFlow(false)
    val isNotificationPanelOpen: StateFlow<Boolean> = _isNotificationPanelOpen.asStateFlow()

    val inAppNotifications = com.example.util.NotificationHelper.notifications

    val topics = repository.topics

    private val _readerFontSizeSp = MutableStateFlow(17)
    val readerFontSizeSp: StateFlow<Int> = _readerFontSizeSp.asStateFlow()

    val isRefreshing: StateFlow<Boolean> = repository.isRefreshing
    val countdownSeconds: StateFlow<Int> = repository.countdownSeconds
    val autoRefreshIntervalSec: StateFlow<Int> = repository.autoRefreshIntervalSec
    val sources: StateFlow<List<NewsSource>> = repository.sources
    val lastUpdated: StateFlow<Long> = repository.lastUpdated

    val allArticles = repository.allArticles.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val bookmarkedArticles = repository.bookmarkedArticles.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val filteredArticles = combine(
        repository.allArticles,
        _selectedCategory,
        _searchQuery,
        _filterUnreadOnly
    ) { articles, category, query, unreadOnly ->
        articles.filter { article ->
            val matchesCategory = (category == NewsCategory.ALL || article.category == category)
            val matchesQuery = query.isBlank() ||
                    article.title.contains(query, ignoreCase = true) ||
                    article.description.contains(query, ignoreCase = true) ||
                    article.sourceName.contains(query, ignoreCase = true)
            val matchesUnread = !unreadOnly || !article.isRead

            matchesCategory && matchesQuery && matchesUnread
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val breakingNews: StateFlow<List<NewsArticle>> = repository.allArticles.combine(_searchQuery) { articles, _ ->
        articles.filter { it.isBreaking }.take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: NavTab) {
        _currentTab.value = tab
    }

    fun selectCategory(category: NewsCategory) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
        }
    }

    fun toggleFilterUnreadOnly() {
        _filterUnreadOnly.value = !_filterUnreadOnly.value
    }

    fun toggleLayoutMode() {
        _layoutMode.value = if (_layoutMode.value == LayoutMode.CARDS) LayoutMode.COMPACT else LayoutMode.CARDS
    }

    fun openArticle(article: NewsArticle) {
        _selectedArticle.value = article
        viewModelScope.launch {
            repository.markAsRead(article.id)
        }
    }

    fun closeArticle() {
        _selectedArticle.value = null
        audioReader.stop()
    }

    fun toggleBookmark(articleId: String) {
        viewModelScope.launch {
            repository.toggleBookmark(articleId)
            // If the selected article is the same, update its bookmark status locally
            _selectedArticle.value?.let { current ->
                if (current.id == articleId) {
                    _selectedArticle.value = current.copy(isBookmarked = !current.isBookmarked)
                }
            }
        }
    }

    fun refreshManually() {
        viewModelScope.launch {
            repository.refreshFeeds()
        }
    }

    fun setAutoRefreshInterval(seconds: Int) {
        repository.setAutoRefreshInterval(seconds)
    }

    fun toggleSource(sourceId: String) {
        repository.toggleSource(sourceId)
    }

    fun selectTopic(topic: com.example.data.model.TopicStory?) {
        _selectedTopic.value = topic
    }

    fun togglePinTopic(topicId: String, context: android.content.Context) {
        repository.togglePinTopic(topicId, context)
        // If current selected topic matches, update its state
        _selectedTopic.value?.let { current ->
            if (current.id == topicId) {
                _selectedTopic.value = current.copy(isPinned = !current.isPinned)
            }
        }
    }

    fun sendTestNotification(topicId: String, context: android.content.Context) {
        repository.sendTestNotification(topicId, context)
    }

    fun createTopicFromArticle(
        article: NewsArticle,
        title: String,
        summary: String,
        context: android.content.Context
    ) {
        val topicId = repository.createTopic(
            title = title,
            summary = summary,
            category = article.category,
            initialArticle = article,
            context = context
        )
        // Find created topic and select it
        val created = repository.topics.value.find { it.id == topicId }
        _selectedTopic.value = created
    }

    fun addArticleToTopic(topicId: String, article: NewsArticle, context: android.content.Context) {
        repository.addArticleToTopic(topicId, article, context)
        val updated = repository.topics.value.find { it.id == topicId }
        _selectedTopic.value = updated
    }

    fun toggleNotificationPanel() {
        _isNotificationPanelOpen.value = !_isNotificationPanelOpen.value
    }

    fun clearNotifications() {
        com.example.util.NotificationHelper.clearNotifications()
    }

    fun adjustFontSize(delta: Int) {
        val current = _readerFontSizeSp.value
        _readerFontSizeSp.value = (current + delta).coerceIn(13, 26)
    }

    fun playAudio(article: NewsArticle) {
        val textToSpeak = "${article.title}. ${article.description}. ${article.content}"
        audioReader.speak(article.id, textToSpeak)
    }

    fun stopAudio() {
        audioReader.stop()
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.clearCache()
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioReader.shutdown()
    }
}
