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
    val preferences = com.example.util.NewsPreferences(application)
    private val repository = NewsRepository(
        articleDao = database.articleDao(),
        topicDao = database.topicDao(),
        preferences = preferences,
        scope = viewModelScope
    )
    val audioReader = AudioNewsReader(application)

    private val _currentTab = MutableStateFlow(NavTab.FEED)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow(NewsCategory.ALL)
    val selectedCategory: StateFlow<NewsCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _filterUnreadOnly = MutableStateFlow(preferences.filterUnreadOnly)
    val filterUnreadOnly: StateFlow<Boolean> = _filterUnreadOnly.asStateFlow()

    private val _layoutMode = MutableStateFlow(if (preferences.layoutMode == "COMPACT") LayoutMode.COMPACT else LayoutMode.CARDS)
    val layoutMode: StateFlow<LayoutMode> = _layoutMode.asStateFlow()

    private val _selectedArticle = MutableStateFlow<NewsArticle?>(null)
    val selectedArticle: StateFlow<NewsArticle?> = _selectedArticle.asStateFlow()

    private val _selectedTopic = MutableStateFlow<com.example.data.model.TopicStory?>(null)
    val selectedTopic: StateFlow<com.example.data.model.TopicStory?> = _selectedTopic.asStateFlow()

    private val _isNotificationPanelOpen = MutableStateFlow(false)
    val isNotificationPanelOpen: StateFlow<Boolean> = _isNotificationPanelOpen.asStateFlow()

    val inAppNotifications = com.example.util.NotificationHelper.notifications

    val topics: StateFlow<List<com.example.data.model.TopicStory>> = repository.topics.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _readerFontSizeSp = MutableStateFlow(preferences.readerFontSizeSp)
    val readerFontSizeSp: StateFlow<Int> = _readerFontSizeSp.asStateFlow()

    val isRefreshing: StateFlow<Boolean> = repository.isRefreshing
    val countdownSeconds: StateFlow<Int> = repository.countdownSeconds
    val autoRefreshIntervalSec: StateFlow<Int> = repository.autoRefreshIntervalSec
    val sources: StateFlow<List<NewsSource>> = repository.sources
    val lastUpdated: StateFlow<Long> = repository.lastUpdated

    private val translationService = com.example.data.remote.TranslationService()

    private val _targetLanguage = MutableStateFlow(preferences.targetLanguage)
    val targetLanguage: StateFlow<String> = _targetLanguage.asStateFlow()

    private val _isBackgroundSyncEnabled = MutableStateFlow(preferences.isBackgroundSyncEnabled)
    val isBackgroundSyncEnabled: StateFlow<Boolean> = _isBackgroundSyncEnabled.asStateFlow()

    private val _backgroundSyncIntervalMinutes = MutableStateFlow(preferences.backgroundSyncIntervalMinutes)
    val backgroundSyncIntervalMinutes: StateFlow<Int> = _backgroundSyncIntervalMinutes.asStateFlow()

    private val _lastBackgroundSyncTime = MutableStateFlow(preferences.lastBackgroundSyncTime)
    val lastBackgroundSyncTime: StateFlow<Long> = _lastBackgroundSyncTime.asStateFlow()

    private val _translatedArticles = MutableStateFlow<Map<String, NewsArticle>>(emptyMap())
    val translatedArticles: StateFlow<Map<String, NewsArticle>> = _translatedArticles.asStateFlow()

    private val _isTranslating = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val isTranslating: StateFlow<Map<String, Boolean>> = _isTranslating.asStateFlow()

    private val _personalization = MutableStateFlow(com.example.data.model.UserPersonalization())
    val personalization: StateFlow<com.example.data.model.UserPersonalization> = _personalization.asStateFlow()

    private val _isPersonalizationDialogOpen = MutableStateFlow(false)
    val isPersonalizationDialogOpen: StateFlow<Boolean> = _isPersonalizationDialogOpen.asStateFlow()

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

    val filteredArticles: StateFlow<List<NewsArticle>> = combine(
        repository.allArticles,
        _selectedCategory,
        _searchQuery,
        combine(_filterUnreadOnly, _personalization, repository.sources) { unread, prefs, srcs ->
            Triple(unread, prefs, srcs)
        }
    ) { articles, category, query, (unreadOnly, userPrefs, currentSources) ->
        val favoriteSourceNames = currentSources.filter { it.isFavorite || userPrefs.favoriteSourceIds.contains(it.id) }.map { it.name }.toSet()

        articles.filter { article ->
            val matchesCategory = (category == NewsCategory.ALL || article.category == category)
            val matchesQuery = query.isBlank() ||
                    article.title.contains(query, ignoreCase = true) ||
                    article.description.contains(query, ignoreCase = true) ||
                    article.sourceName.contains(query, ignoreCase = true)
            val matchesUnread = !unreadOnly || !article.isRead

            val matchesPersonalization = if (!userPrefs.isPersonalizedFeedActive) {
                true
            } else {
                val matchesCategoryPref = userPrefs.selectedCategoryNames.contains(article.category.name)
                val matchesSourcePref = favoriteSourceNames.any { prefName ->
                    article.sourceName.contains(prefName, ignoreCase = true) ||
                    prefName.contains(article.sourceName, ignoreCase = true)
                } || userPrefs.favoriteSourceIds.any { id ->
                    article.id.contains(id, ignoreCase = true) || article.sourceName.lowercase().contains(id.substringBefore("_"))
                }
                matchesCategoryPref || matchesSourcePref
            }

            matchesCategory && matchesQuery && matchesUnread && matchesPersonalization
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
        val next = !_filterUnreadOnly.value
        _filterUnreadOnly.value = next
        preferences.filterUnreadOnly = next
    }

    fun toggleLayoutMode() {
        val next = if (_layoutMode.value == LayoutMode.CARDS) LayoutMode.COMPACT else LayoutMode.CARDS
        _layoutMode.value = next
        preferences.layoutMode = next.name
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

    fun toggleFavoriteSource(sourceId: String) {
        repository.toggleFavoriteSource(sourceId)
        val current = _personalization.value.favoriteSourceIds.toMutableSet()
        if (current.contains(sourceId)) {
            current.remove(sourceId)
        } else {
            current.add(sourceId)
        }
        _personalization.value = _personalization.value.copy(favoriteSourceIds = current)
    }

    fun togglePersonalizedFeed() {
        _personalization.value = _personalization.value.copy(
            isPersonalizedFeedActive = !_personalization.value.isPersonalizedFeedActive
        )
    }

    fun setPersonalizedFeedActive(active: Boolean) {
        _personalization.value = _personalization.value.copy(
            isPersonalizedFeedActive = active
        )
    }

    fun togglePreferredCategory(categoryName: String) {
        val current = _personalization.value.selectedCategoryNames.toMutableSet()
        if (current.contains(categoryName)) {
            if (current.size > 1) {
                current.remove(categoryName)
            }
        } else {
            current.add(categoryName)
        }
        _personalization.value = _personalization.value.copy(selectedCategoryNames = current)
    }

    fun setPersonalizationDialogVisible(visible: Boolean) {
        _isPersonalizationDialogOpen.value = visible
    }

    fun setTargetLanguage(langCode: String) {
        _targetLanguage.value = langCode
        preferences.targetLanguage = langCode
        // Invalidate current translations to re-translate into new language if requested
        _translatedArticles.value = emptyMap()
    }

    fun toggleTranslateArticle(article: NewsArticle) {
        val articleId = article.id
        if (_translatedArticles.value.containsKey(articleId)) {
            // Revert back to original
            _translatedArticles.value = _translatedArticles.value - articleId
            return
        }

        viewModelScope.launch {
            _isTranslating.value = _isTranslating.value + (articleId to true)
            try {
                val translated = translationService.translateArticle(article, _targetLanguage.value)
                _translatedArticles.value = _translatedArticles.value + (articleId to translated)
            } catch (e: Exception) {
                android.util.Log.e("NewsViewModel", "Translation error for ${article.id}", e)
            } finally {
                _isTranslating.value = _isTranslating.value - articleId
            }
        }
    }

    fun selectTopic(topic: com.example.data.model.TopicStory?) {
        _selectedTopic.value = topic
    }

    fun togglePinTopic(topicId: String, context: android.content.Context) {
        repository.togglePinTopic(topicId, context)
        _selectedTopic.value?.let { current ->
            if (current.id == topicId) {
                _selectedTopic.value = current.copy(isPinned = !current.isPinned)
            }
        }
    }

    fun deleteTopic(topicId: String) {
        if (_selectedTopic.value?.id == topicId) {
            _selectedTopic.value = null
        }
        repository.deleteTopic(topicId)
    }

    fun archiveTopic(topicId: String, isArchived: Boolean) {
        repository.archiveTopic(topicId, isArchived)
        if (_selectedTopic.value?.id == topicId) {
            _selectedTopic.value = _selectedTopic.value?.copy(isArchived = isArchived)
        }
    }

    fun createTopic(
        title: String,
        summary: String,
        category: NewsCategory,
        context: android.content.Context
    ): String {
        return repository.createTopic(
            title = title,
            summary = summary,
            category = category,
            initialArticle = null,
            context = context
        )
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
        val created = topics.value.find { it.id == topicId }
        _selectedTopic.value = created
    }

    fun addArticleToTopic(topicId: String, article: NewsArticle, context: android.content.Context) {
        repository.addArticleToTopic(topicId, article, context)
        val updated = topics.value.find { it.id == topicId }
        _selectedTopic.value = updated
    }

    fun setBackgroundSyncEnabled(enabled: Boolean) {
        _isBackgroundSyncEnabled.value = enabled
        repository.setBackgroundSyncEnabled(enabled, getApplication())
    }

    fun setBackgroundSyncInterval(minutes: Int) {
        _backgroundSyncIntervalMinutes.value = minutes
        repository.setBackgroundSyncInterval(minutes, getApplication())
    }

    fun triggerBackgroundSyncNow(onComplete: (Int) -> Unit) {
        repository.triggerBackgroundSyncNow(getApplication()) { count ->
            _lastBackgroundSyncTime.value = preferences.lastBackgroundSyncTime
            onComplete(count)
        }
    }

    fun toggleNotificationPanel() {
        _isNotificationPanelOpen.value = !_isNotificationPanelOpen.value
    }

    fun clearNotifications() {
        com.example.util.NotificationHelper.clearNotifications()
    }

    fun adjustFontSize(delta: Int) {
        val current = _readerFontSizeSp.value
        val next = (current + delta).coerceIn(13, 26)
        _readerFontSizeSp.value = next
        preferences.readerFontSizeSp = next
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
