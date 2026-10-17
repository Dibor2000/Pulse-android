package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NewsArticle
import com.example.data.model.NewsCategory
import com.example.ui.LayoutMode
import com.example.ui.components.ArticleCard
import com.example.ui.components.ArticleCompactRow
import com.example.ui.components.BreakingNewsTicker
import com.example.ui.components.CategoryChips
import com.example.ui.theme.NewsCrimson

@Composable
fun NewsFeedScreen(
    articles: List<NewsArticle>,
    breakingNews: List<NewsArticle>,
    selectedCategory: NewsCategory,
    filterUnreadOnly: Boolean,
    layoutMode: LayoutMode,
    isRefreshing: Boolean,
    isPersonalizedActive: Boolean = false,
    onCategorySelect: (NewsCategory) -> Unit,
    onToggleUnreadFilter: () -> Unit,
    onTogglePersonalized: () -> Unit = {},
    onOpenPersonalizationDialog: () -> Unit = {},
    onArticleClick: (NewsArticle) -> Unit,
    onBookmarkToggle: (String) -> Unit,
    onAudioPlay: (NewsArticle) -> Unit,
    onManualRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Breaking News Ticker (if any breaking items exist)
        if (breakingNews.isNotEmpty()) {
            BreakingNewsTicker(
                breakingNews = breakingNews,
                onArticleClick = onArticleClick
            )
        }

        // Category & Filter chips with personalization
        CategoryChips(
            selectedCategory = selectedCategory,
            filterUnreadOnly = filterUnreadOnly,
            isPersonalizedActive = isPersonalizedActive,
            onCategorySelect = onCategorySelect,
            onToggleUnreadFilter = onToggleUnreadFilter,
            onTogglePersonalized = onTogglePersonalized,
            onOpenPersonalizationDialog = onOpenPersonalizationDialog
        )

        if (isPersonalizedActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NewsCrimson.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⭐ Моя лента: формируется на основе ваших избранных СМИ и тем",
                        fontSize = 11.sp,
                        color = NewsCrimson,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Настроить",
                        fontSize = 11.sp,
                        color = NewsCrimson,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onOpenPersonalizationDialog() }
                    )
                }
            }
        }

        // Loading bar when refreshing
        if (isRefreshing && articles.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Получение свежих новостей...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Feed content or Empty State
        if (articles.isEmpty()) {
            EmptyFeedState(
                isRefreshing = isRefreshing,
                onRefresh = onManualRefresh
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = if (layoutMode == LayoutMode.CARDS) 16.dp else 0.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(if (layoutMode == LayoutMode.CARDS) 14.dp else 0.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("news_feed_list")
            ) {
                items(articles, key = { it.id }) { article ->
                    if (layoutMode == LayoutMode.CARDS) {
                        ArticleCard(
                            article = article,
                            onArticleClick = onArticleClick,
                            onBookmarkToggle = onBookmarkToggle,
                            onAudioPlay = onAudioPlay
                        )
                    } else {
                        ArticleCompactRow(
                            article = article,
                            onArticleClick = onArticleClick,
                            onBookmarkToggle = onBookmarkToggle
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyFeedState(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Feed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Нет новостей по вашему фильтру",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Попробуйте изменить категорию, сбросить поисковый запрос или обновить ленту прямо сейчас",
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onRefresh,
                enabled = !isRefreshing,
                modifier = Modifier.testTag("empty_feed_refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Обновить новости")
            }
        }
    }
}
