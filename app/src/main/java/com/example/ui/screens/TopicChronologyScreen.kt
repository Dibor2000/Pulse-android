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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.DeleteOutline
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.NewsCategory
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
    onArchiveTopic: (String, Boolean) -> Unit,
    onDeleteTopic: (String) -> Unit,
    onCreateTopic: (String, String, NewsCategory) -> Unit,
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
            onArchiveTopic = { isArchived -> onArchiveTopic(selectedTopic.id, isArchived) },
            onDeleteTopic = { onDeleteTopic(selectedTopic.id) },
            onSendTestNotification = { onSendTestNotification(selectedTopic.id, context) },
            onArticleClick = onArticleClick,
            onBookmarkToggle = onBookmarkToggle,
            modifier = modifier
        )
    } else {
        // Storylines List Overview with Active / Archive tabs and Creation
        TopicStoriesListView(
            topics = topics,
            onSelectTopic = onSelectTopic,
            onTogglePin = { onTogglePin(it.id, context) },
            onArchiveTopic = onArchiveTopic,
            onDeleteTopic = onDeleteTopic,
            onCreateTopic = onCreateTopic,
            modifier = modifier
        )
    }
}

@Composable
fun TopicStoriesListView(
    topics: List<TopicStory>,
    onSelectTopic: (TopicStory) -> Unit,
    onTogglePin: (TopicStory) -> Unit,
    onArchiveTopic: (String, Boolean) -> Unit,
    onDeleteTopic: (String) -> Unit,
    onCreateTopic: (String, String, NewsCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Active, 1: Archive
    var showCreateDialog by remember { mutableStateOf(false) }
    var topicToDelete by remember { mutableStateOf<TopicStory?>(null) }

    val activeTopics = topics.filter { !it.isArchived }
    val archivedTopics = topics.filter { it.isArchived }
    val displayedTopics = if (selectedTab == 0) activeTopics else archivedTopics

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = NewsCrimson,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Сюжеты и хроника",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Хронология событий и развитие тем",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { showCreateDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("create_topic_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Сюжет",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Tabs: Active vs Archive
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Активные (${activeTopics.size})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("tab_active_topics")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "В архиве (${archivedTopics.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("tab_archived_topics")
                    )
                }
            }
        }

        if (displayedTopics.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.Timeline else Icons.Default.Archive,
                            contentDescription = null,
                            tint = NewsCrimson,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedTab == 0) "Нет активных сюжетов" else "Архив сюжетов пуст",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedTab == 0)
                                "Вы можете создать собственный сюжет для отслеживания развития ситуации или прикрепить к нему новость из ленты."
                            else
                                "Здесь будут отображаться сюжеты, завершившие активное развитие и отправленные в архив.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        if (selectedTab == 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showCreateDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("empty_create_topic_button")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Создать сюжет")
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("topics_list")
            ) {
                if (selectedTab == 0) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = NewsCrimson,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Закрепите сюжет значком булавки, чтобы мгновенно получать системные оповещения при развитии ситуации.",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(displayedTopics, key = { it.id }) { topic ->
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
                            // Category & Action icons row
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
                                        Spacer(modifier = Modifier.width(6.dp))
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
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "ВКЛ",
                                                color = NewsCrimson,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }

                                    if (topic.isArchived) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "АРХИВ",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Quick action buttons: Pin, Archive, Delete
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Pin / Unpin
                                    IconButton(
                                        onClick = { onTogglePin(topic) },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("pin_topic_button_${topic.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (topic.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                            contentDescription = "Закрепить для уведомлений",
                                            tint = if (topic.isPinned) NewsCrimson else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Archive / Unarchive
                                    IconButton(
                                        onClick = { onArchiveTopic(topic.id, !topic.isArchived) },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("archive_topic_button_${topic.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (topic.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                                            contentDescription = if (topic.isArchived) "Вернуть из архива" else "В архив",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Delete
                                    IconButton(
                                        onClick = { topicToDelete = topic },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("delete_topic_button_${topic.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Удалить сюжет",
                                            tint = NewsCrimson,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Topic Title
                            Text(
                                text = topic.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Topic Summary
                            Text(
                                text = topic.summary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Chronology preview counter
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Хронология: ${topic.events.size} этапов",
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

    // Delete Confirmation Dialog
    topicToDelete?.let { topic ->
        AlertDialog(
            onDismissRequest = { topicToDelete = null },
            title = {
                Text(
                    text = "Удалить сюжет?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Сюжет «${topic.title}» и его хронологическая лента будут удалены. Все статьи останутся доступными в общей ленте.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTopic(topic.id)
                        topicToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NewsCrimson)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { topicToDelete = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Create New Topic Dialog
    if (showCreateDialog) {
        CreateTopicStandaloneDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, summary, category ->
                onCreateTopic(title, summary, category)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun CreateTopicStandaloneDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, summary: String, category: NewsCategory) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(NewsCategory.WORLD) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Создать сюжет",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Объединяйте новости по одной теме и отслеживайте их развитие в хронологии.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название сюжета") },
                    placeholder = { Text("Например: Развитие космической программы") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Краткое описание / фабула") },
                    placeholder = { Text("О чем сюжет и за какими событиями следим") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Категория:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                val availableCategories = listOf(
                    NewsCategory.WORLD,
                    NewsCategory.TECH,
                    NewsCategory.BUSINESS,
                    NewsCategory.SCIENCE,
                    NewsCategory.GAMES
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableCategories.forEach { cat ->
                        val isSel = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { selectedCategory = cat }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = cat.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title.trim(), summary.trim().ifBlank { title.trim() }, selectedCategory)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailTimelineView(
    topic: TopicStory,
    allArticles: List<NewsArticle>,
    onBack: () -> Unit,
    onTogglePin: () -> Unit,
    onArchiveTopic: (Boolean) -> Unit,
    onDeleteTopic: () -> Unit,
    onSendTestNotification: () -> Unit,
    onArticleClick: (NewsArticle) -> Unit,
    onBookmarkToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
                    // Pin
                    IconButton(onClick = onTogglePin, modifier = Modifier.testTag("timeline_pin_button")) {
                        Icon(
                            imageVector = if (topic.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Закрепить сюжет",
                            tint = if (topic.isPinned) NewsCrimson else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Archive
                    IconButton(
                        onClick = { onArchiveTopic(!topic.isArchived) },
                        modifier = Modifier.testTag("timeline_archive_button")
                    ) {
                        Icon(
                            imageVector = if (topic.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                            contentDescription = if (topic.isArchived) "Вернуть из архива" else "В архив",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Delete
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("timeline_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Удалить сюжет",
                            tint = NewsCrimson
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
                    onOpenEvent = {
                        val matchingArticle = allArticles.find { it.id == event.articleId }
                            ?: allArticles.find { it.title.contains(event.title.take(15), ignoreCase = true) }
                            ?: NewsArticle(
                                id = event.articleId ?: event.id,
                                title = event.title,
                                description = event.summary,
                                content = "${event.summary}\n\nМатериал зафиксирован в рамках развития сюжета «${topic.title}». По данным издания «${event.sourceName}», событие квалифицировано как «${event.milestoneType.label}» и имеет существенное значение для общего контекста ситуации.\n\nПродолжается мониторинг оперативных сообщений и экспертных комментариев профильных ведомств.",
                                link = "",
                                sourceName = event.sourceName,
                                sourceUrl = "",
                                imageUrl = null,
                                category = topic.category,
                                publishedAt = event.timestamp,
                                isBreaking = event.milestoneType == MilestoneType.BREAKING,
                                readTimeMinutes = 2,
                                topicId = topic.id
                            )
                        onArticleClick(matchingArticle)
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

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Удалить сюжет?", fontWeight = FontWeight.Bold) },
                text = { Text("Сюжет «${topic.title}» и его хронологическая лента будут удалены. Все статьи останутся доступными в общей ленте.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirm = false
                            onDeleteTopic()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NewsCrimson)
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@Composable
fun TimelineNodeItem(
    event: ChronologyEvent,
    isLast: Boolean,
    onOpenEvent: () -> Unit,
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

        // Event Card - whole card is clickable to open details
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp)
                .clickable { onOpenEvent() }
                .testTag("timeline_event_${event.id}")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
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

                Spacer(modifier = Modifier.height(6.dp))

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

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = event.sourceName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Подробнее о ситуации →",
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
