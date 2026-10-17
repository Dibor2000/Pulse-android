package com.example.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Feed
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.model.NewsArticle
import com.example.ui.components.CreateTopicDialog
import com.example.ui.components.NotificationCenterDialog
import com.example.ui.components.PersonalizationDialog
import com.example.ui.components.TopNewsBar
import com.example.ui.screens.ArticleReaderScreen
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.NewsFeedScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SourcesScreen
import com.example.ui.screens.TopicChronologyScreen
import com.example.ui.theme.NewsCrimson

@Composable
fun NewsApp(
    viewModel: NewsViewModel = viewModel()
) {
    val context = LocalContext.current

    val currentTab by viewModel.currentTab.collectAsState()
    val selectedArticle by viewModel.selectedArticle.collectAsState()
    val filteredArticles by viewModel.filteredArticles.collectAsState()
    val allArticles by viewModel.allArticles.collectAsState()
    val breakingNews by viewModel.breakingNews.collectAsState()
    val bookmarkedArticles by viewModel.bookmarkedArticles.collectAsState()
    val sources by viewModel.sources.collectAsState()
    val personalization by viewModel.personalization.collectAsState()
    val isPersonalizationDialogOpen by viewModel.isPersonalizationDialogOpen.collectAsState()

    val targetLanguage by viewModel.targetLanguage.collectAsState()
    val translatedArticles by viewModel.translatedArticles.collectAsState()
    val isTranslating by viewModel.isTranslating.collectAsState()

    val topics by viewModel.topics.collectAsState()
    val selectedTopic by viewModel.selectedTopic.collectAsState()
    val inAppNotifications by viewModel.inAppNotifications.collectAsState()
    val isNotificationPanelOpen by viewModel.isNotificationPanelOpen.collectAsState()

    var articleForTopicDialog by remember { mutableStateOf<NewsArticle?>(null) }

    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val filterUnreadOnly by viewModel.filterUnreadOnly.collectAsState()
    val layoutMode by viewModel.layoutMode.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val countdownSeconds by viewModel.countdownSeconds.collectAsState()
    val autoRefreshIntervalSec by viewModel.autoRefreshIntervalSec.collectAsState()
    val readerFontSizeSp by viewModel.readerFontSizeSp.collectAsState()
    val isBackgroundSyncEnabled by viewModel.isBackgroundSyncEnabled.collectAsState()
    val backgroundSyncIntervalMinutes by viewModel.backgroundSyncIntervalMinutes.collectAsState()
    val lastBackgroundSyncTime by viewModel.lastBackgroundSyncTime.collectAsState()

    val isAudioPlaying by viewModel.audioReader.isPlaying.collectAsState()

    Scaffold(
        topBar = {
            if (selectedArticle == null) {
                TopNewsBar(
                    isRefreshing = isRefreshing,
                    countdownSeconds = countdownSeconds,
                    autoRefreshIntervalSec = autoRefreshIntervalSec,
                    layoutMode = layoutMode,
                    isSearchActive = isSearchActive,
                    searchQuery = searchQuery,
                    notificationsCount = inAppNotifications.size,
                    onSearchToggle = { viewModel.setSearchActive(!isSearchActive) },
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onLayoutToggle = { viewModel.toggleLayoutMode() },
                    onNotificationCenterClick = { viewModel.toggleNotificationPanel() },
                    onManualRefresh = { viewModel.refreshManually() },
                    modifier = Modifier.statusBarsPadding()
                )
            }
        },
        bottomBar = {
            if (selectedArticle == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = currentTab == NavTab.FEED,
                        onClick = { viewModel.selectTab(NavTab.FEED) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavTab.FEED) Icons.Filled.Feed else Icons.Outlined.Feed,
                                contentDescription = stringResource(R.string.nav_feed)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_feed), fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NewsCrimson,
                            selectedTextColor = NewsCrimson,
                            indicatorColor = NewsCrimson.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_feed")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavTab.STORIES,
                        onClick = { viewModel.selectTab(NavTab.STORIES) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavTab.STORIES) Icons.Filled.Timeline else Icons.Outlined.Timeline,
                                contentDescription = stringResource(R.string.nav_stories)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_stories), fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NewsCrimson,
                            selectedTextColor = NewsCrimson,
                            indicatorColor = NewsCrimson.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_stories")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavTab.BOOKMARKS,
                        onClick = { viewModel.selectTab(NavTab.BOOKMARKS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavTab.BOOKMARKS) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = stringResource(R.string.nav_bookmarks)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_bookmarks), fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NewsCrimson,
                            selectedTextColor = NewsCrimson,
                            indicatorColor = NewsCrimson.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_bookmarks")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavTab.SOURCES,
                        onClick = { viewModel.selectTab(NavTab.SOURCES) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavTab.SOURCES) Icons.Filled.RssFeed else Icons.Outlined.RssFeed,
                                contentDescription = stringResource(R.string.nav_sources)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_sources), fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NewsCrimson,
                            selectedTextColor = NewsCrimson,
                            indicatorColor = NewsCrimson.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_sources")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavTab.SETTINGS,
                        onClick = { viewModel.selectTab(NavTab.SETTINGS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = stringResource(R.string.nav_settings)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_settings), fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NewsCrimson,
                            selectedTextColor = NewsCrimson,
                            indicatorColor = NewsCrimson.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavTab.FEED -> {
                    NewsFeedScreen(
                        articles = filteredArticles,
                        breakingNews = breakingNews,
                        selectedCategory = selectedCategory,
                        filterUnreadOnly = filterUnreadOnly,
                        layoutMode = layoutMode,
                        isRefreshing = isRefreshing,
                        isPersonalizedActive = personalization.isPersonalizedFeedActive,
                        onCategorySelect = { viewModel.selectCategory(it) },
                        onToggleUnreadFilter = { viewModel.toggleFilterUnreadOnly() },
                        onTogglePersonalized = { viewModel.togglePersonalizedFeed() },
                        onOpenPersonalizationDialog = { viewModel.setPersonalizationDialogVisible(true) },
                        onArticleClick = { viewModel.openArticle(it) },
                        onBookmarkToggle = { viewModel.toggleBookmark(it) },
                        onAudioPlay = { viewModel.playAudio(it) },
                        onManualRefresh = { viewModel.refreshManually() }
                    )
                }

                NavTab.STORIES -> {
                    TopicChronologyScreen(
                        topics = topics,
                        selectedTopic = selectedTopic,
                        allArticles = allArticles,
                        onSelectTopic = { viewModel.selectTopic(it) },
                        onTogglePin = { id, ctx -> viewModel.togglePinTopic(id, ctx) },
                        onArchiveTopic = { id, archived -> viewModel.archiveTopic(id, archived) },
                        onDeleteTopic = { id -> viewModel.deleteTopic(id) },
                        onCreateTopic = { title, summary, category ->
                            viewModel.createTopic(title, summary, category, context)
                        },
                        onSendTestNotification = { id, ctx -> viewModel.sendTestNotification(id, ctx) },
                        onArticleClick = { viewModel.openArticle(it) },
                        onBookmarkToggle = { viewModel.toggleBookmark(it) }
                    )
                }

                NavTab.BOOKMARKS -> {
                    BookmarksScreen(
                        bookmarkedArticles = bookmarkedArticles,
                        onArticleClick = { viewModel.openArticle(it) },
                        onBookmarkToggle = { viewModel.toggleBookmark(it) },
                        onAudioPlay = { viewModel.playAudio(it) }
                    )
                }

                NavTab.SOURCES -> {
                    SourcesScreen(
                        sources = sources,
                        onToggleSource = { viewModel.toggleSource(it) },
                        onToggleFavorite = { viewModel.toggleFavoriteSource(it) }
                    )
                }

                NavTab.SETTINGS -> {
                    SettingsScreen(
                        autoRefreshIntervalSec = autoRefreshIntervalSec,
                        targetLanguage = targetLanguage,
                        isBackgroundSyncEnabled = isBackgroundSyncEnabled,
                        backgroundSyncIntervalMinutes = backgroundSyncIntervalMinutes,
                        lastBackgroundSyncTime = lastBackgroundSyncTime,
                        onIntervalChange = { viewModel.setAutoRefreshInterval(it) },
                        onTargetLanguageChange = { viewModel.setTargetLanguage(it) },
                        onBackgroundSyncEnabledChange = { viewModel.setBackgroundSyncEnabled(it) },
                        onBackgroundSyncIntervalChange = { viewModel.setBackgroundSyncInterval(it) },
                        onTriggerBackgroundSyncNow = { onResult -> viewModel.triggerBackgroundSyncNow(onResult) },
                        onClearCache = { viewModel.clearCache() },
                        onOpenPersonalization = { viewModel.setPersonalizationDialogVisible(true) }
                    )
                }
            }

            // Article Detail Reader overlay
            AnimatedVisibility(
                visible = selectedArticle != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                selectedArticle?.let { article ->
                    ArticleReaderScreen(
                        article = article,
                        fontSizeSp = readerFontSizeSp,
                        isPlayingAudio = isAudioPlaying,
                        topics = topics,
                        translatedArticle = translatedArticles[article.id],
                        isTranslating = isTranslating[article.id] ?: false,
                        targetLanguageCode = targetLanguage,
                        onToggleTranslate = { viewModel.toggleTranslateArticle(it) },
                        onBack = { viewModel.closeArticle() },
                        onBookmarkToggle = { viewModel.toggleBookmark(it) },
                        onAudioPlay = { viewModel.playAudio(it) },
                        onAudioStop = { viewModel.stopAudio() },
                        onAdjustFontSize = { viewModel.adjustFontSize(it) },
                        onOpenTopic = { topic ->
                            viewModel.closeArticle()
                            viewModel.selectTab(NavTab.STORIES)
                            viewModel.selectTopic(topic)
                        },
                        onAddToTopicClick = {
                            articleForTopicDialog = article
                        }
                    )
                }
            }

            // Create or Link Topic Dialog
            articleForTopicDialog?.let { article ->
                CreateTopicDialog(
                    article = article,
                    existingTopics = topics,
                    onDismiss = { articleForTopicDialog = null },
                    onCreateNewTopic = { title, summary ->
                        viewModel.createTopicFromArticle(article, title, summary, context)
                        viewModel.selectTab(NavTab.STORIES)
                    },
                    onAddToExistingTopic = { topicId ->
                        viewModel.addArticleToTopic(topicId, article, context)
                        viewModel.selectTab(NavTab.STORIES)
                    }
                )
            }

            // In-app & System Notification Center Dialog
            if (isNotificationPanelOpen) {
                NotificationCenterDialog(
                    notifications = inAppNotifications,
                    onDismiss = { viewModel.toggleNotificationPanel() },
                    onClearAll = { viewModel.clearNotifications() },
                    onOpenArticle = { articleId ->
                        allArticles.find { it.id == articleId }?.let { viewModel.openArticle(it) }
                    }
                )
            }

            // User Personalization Dialog
            if (isPersonalizationDialogOpen) {
                PersonalizationDialog(
                    personalization = personalization,
                    sources = sources,
                    onToggleActive = { viewModel.setPersonalizedFeedActive(it) },
                    onToggleCategory = { viewModel.togglePreferredCategory(it) },
                    onToggleFavoriteSource = { viewModel.toggleFavoriteSource(it) },
                    onDismiss = { viewModel.setPersonalizationDialogVisible(false) }
                )
            }
        }
    }
}
