package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChronologyEvent
import com.example.data.model.MilestoneType
import com.example.data.model.NewsArticle
import com.example.data.model.TopicStory
import com.example.ui.components.ArticleCompactRow
import com.example.ui.theme.NewsCrimson
import com.example.ui.theme.NewsEmerald
import com.example.util.TimeUtils

@Composable
fun TopicChronologyScreen(
    topics: List<TopicStory>,
    selectedTopic: TopicStory?,
    allArticles: List<NewsArticle>,
    onSelectTopic: (TopicStory?) -> Unit,
    onTogglePin: (String, Context) -> Unit,
    onSendTestNotification: (String, Context) -> Unit,
    onArticleClick: (NewsArticle) -> Unit,
    onBookmarkToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (selectedTopic != null) {
        // Detailed Topic Chronology Timeline View
        TopicDetailTimelineView(
            topic = selectedTopic,
            allArticles = allArticles,
            onBack = { onSelectTopic(null) },
            onTogglePin = { onTogglePin(selectedTopic.id, context) },
            onSendTestNotification = { onSendTestNotification(selectedTopic.id, context) },
            onArticleClick = onArticleClick,
            onBookmarkToggle = onBookmarkToggle,
            modifier = modifier
        )
    } else {
        // Storylines List Overview
        TopicStoriesListView(
            topics = topics,
            onSelectTopic = onSelectTopic,
            onTogglePin = { onTogglePin(it.id, context) },
            modifier = modifier
        )
    }
}

@Composable
fun TopicStoriesListView(
    topics: List<TopicStory>,
    onSelectTopic: (TopicStory) -> Unit,
    onTogglePin: (TopicStory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = NewsCrimson,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Сюжеты и хроника событий",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Хронология развивающихся инфоповодов с уведомлениями",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("topics_list")
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = NewsCrimson,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Закрепите интересующий сюжет (значок булавки), чтобы мгновенно получать системные уведомления о новых этапах события.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(topics, key = { it.id }) { topic ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTopic(topic) }
                        .testTag("topic_card_${topic.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Category & Pin badge row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = topic.category.displayName,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )

                                if (topic.isPinned) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(
                                                NewsCrimson.copy(alpha = 0.15f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PushPin,
                                            contentDescription = null,
                                            tint = NewsCrimson,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "УВЕДОМЛЕНИЯ ВКЛ",
                                            color = NewsCrimson,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }

                            // Pin / Unpin button
                            IconButton(
                                onClick = { onTogglePin(topic) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("pin_topic_button_${topic.id}")
                            ) {
                                Icon(
                                    imageVector = if (topic.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = "Закрепить для уведомлений",
                                    tint = if (topic.isPinned) NewsCrimson else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Topic Title
                        Text(
                            text = topic.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Topic Summary
                        Text(
                            text = topic.summary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Chronology preview counter
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Хронология: ${topic.events.size} этапов развития",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "Открыть хронологию →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailTimelineView(
    topic: TopicStory,
    allArticles: List<NewsArticle>,
    onBack: () -> Unit,
    onTogglePin: () -> Unit,
    onSendTestNotification: () -> Unit,
    onArticleClick: (NewsArticle) -> Unit,
    onBookmarkToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val relatedArticles = allArticles.filter { it.topicId == topic.id || topic.articleIds.contains(it.id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Хроника сюжета",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("timeline_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад к сюжетам")
                    }
                },
                actions = {
                    IconButton(onClick = onTogglePin, modifier = Modifier.testTag("timeline_pin_button")) {
                        Icon(
                            imageVector = if (topic.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Закрепить сюжет",
                            tint = if (topic.isPinned) NewsCrimson else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("timeline_events_list")
        ) {
            // Header Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = topic.category.displayName,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (topic.isPinned) {
                                Text(
                                    text = "🔔 Уведомления активны",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NewsCrimson
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = topic.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = topic.summary,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Test Notification Button
                        Button(
                            onClick = onSendTestNotification,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (topic.isPinned) NewsCrimson else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_topic_notification_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Проверить уведомление по сюжету")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Хронология развития событий",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Timeline Event Items
            val sortedEvents = topic.events.sortedBy { it.timestamp }
            items(sortedEvents.size) { index ->
                val event = sortedEvents[index]
                val isLast = index == sortedEvents.size - 1

                TimelineNodeItem(
                    event = event,
                    isLast = isLast,
                    onOpenArticle = { articleId ->
                        allArticles.find { it.id == articleId }?.let { onArticleClick(it) }
                    }
                )
            }

            // Related Articles Section
            if (relatedArticles.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Связанные статьи (${relatedArticles.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(relatedArticles, key = { it.id }) { article ->
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

@Composable
fun TimelineNodeItem(
    event: ChronologyEvent,
    isLast: Boolean,
    onOpenArticle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Vertical Timeline Column (Dot + Line)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(
                        when (event.milestoneType) {
                            MilestoneType.BREAKING -> NewsCrimson
                            MilestoneType.START -> MaterialTheme.colorScheme.primary
                            MilestoneType.MILESTONE -> NewsEmerald
                            else -> MaterialTheme.colorScheme.secondary
                        }
                    )
            )

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Event Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = event.milestoneType.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (event.milestoneType) {
                            MilestoneType.BREAKING -> NewsCrimson
                            MilestoneType.MILESTONE -> NewsEmerald
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )

                    Text(
                        text = TimeUtils.formatRelativeTime(event.timestamp),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.summary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!event.articleId.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Читать связанный материал →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onOpenArticle(event.articleId) }
                    )
                }
            }
        }
    }
}
