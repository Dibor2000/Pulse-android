package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.NewsArticle
import com.example.ui.theme.NewsCrimson
import com.example.ui.theme.NewsEmerald
import com.example.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleReaderScreen(
    article: NewsArticle,
    fontSizeSp: Int,
    isPlayingAudio: Boolean,
    topics: List<com.example.data.model.TopicStory> = emptyList(),
    onBack: () -> Unit,
    onBookmarkToggle: (String) -> Unit,
    onAudioPlay: (NewsArticle) -> Unit,
    onAudioStop: () -> Unit,
    onAdjustFontSize: (Int) -> Unit,
    onOpenTopic: (com.example.data.model.TopicStory) -> Unit = {},
    onAddToTopicClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "audio_bars")
    val waveHeight1 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave1"
    )
    val waveHeight2 by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave2"
    )
    val waveHeight3 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave3"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("reader_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад к ленте"
                        )
                    }
                },
                actions = {
                    // Font size adjuster
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "A-",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onAdjustFontSize(-2) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                        Text(
                            text = "${fontSizeSp}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "A+",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onAdjustFontSize(2) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Bookmark action
                    IconButton(
                        onClick = { onBookmarkToggle(article.id) },
                        modifier = Modifier.testTag("reader_bookmark_button")
                    ) {
                        Icon(
                            imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "В закладки",
                            tint = if (article.isBookmarked) NewsCrimson else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Share action
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TITLE, article.title)
                                putExtra(Intent.EXTRA_TEXT, "${article.title}\n\n${article.link}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Поделиться новостью"))
                        },
                        modifier = Modifier.testTag("reader_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Поделиться"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Audio TTS Player floating bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isPlayingAudio) {
                            // Animated audio waves
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Box(modifier = Modifier.width(4.dp).height(waveHeight1.dp).clip(CircleShape).background(NewsEmerald))
                                Box(modifier = Modifier.width(4.dp).height(waveHeight2.dp).clip(CircleShape).background(NewsEmerald))
                                Box(modifier = Modifier.width(4.dp).height(waveHeight3.dp).clip(CircleShape).background(NewsEmerald))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Озвучивание новости...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NewsEmerald
                                )
                                Text(
                                    text = "Аудио-синтез речи в реальном времени",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Слушать новость",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "~ ${article.readTimeMinutes} мин аудио-чтения",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (isPlayingAudio) {
                        Button(
                            onClick = onAudioStop,
                            colors = ButtonDefaults.buttonColors(containerColor = NewsCrimson),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("audio_stop_button")
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Стоп", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Стоп")
                        }
                    } else {
                        Button(
                            onClick = { onAudioPlay(article) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("audio_play_button")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Слушать", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Слушать")
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            // Hero Image
            if (!article.imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    AsyncImage(
                        model = article.imageUrl,
                        contentDescription = article.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.5f)
                                    )
                                )
                            )
                    )
                }
            }

            // Article Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Category & Breaking badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (article.isBreaking) {
                        Text(
                            text = "СРОЧНАЯ НОВОСТЬ",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .background(NewsCrimson, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = article.category.displayName,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = article.title,
                    fontSize = (fontSizeSp + 5).sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = (fontSizeSp + 11).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Meta row: source, exact date, read time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = article.sourceName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = TimeUtils.formatExactDateTime(article.publishedAt),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lead Description (highlighted callout)
                if (article.description.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = article.description,
                            fontSize = (fontSizeSp + 1).sp,
                            lineHeight = (fontSizeSp + 7).sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Full Content
                val contentToDisplay = if (article.content.isNotBlank()) article.content else article.description
                Text(
                    text = contentToDisplay,
                    fontSize = fontSizeSp.sp,
                    lineHeight = (fontSizeSp + 8).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Event Storyline Card
                val linkedTopic = topics.find { it.id == article.topicId || it.articleIds.contains(article.id) }
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Timeline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (linkedTopic != null) "Сюжет события" else "Развитие инфоповода",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (linkedTopic?.isPinned == true) {
                                Text(
                                    text = "📌 Закреплено",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NewsCrimson
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (linkedTopic != null) {
                            Text(
                                text = linkedTopic.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Хронология: ${linkedTopic.events.size} этапов развития",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            FilledTonalButton(
                                onClick = { onOpenTopic(linkedTopic) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_storyline_button")
                            ) {
                                Text("Открыть полную хронологию сюжета →")
                            }
                        } else {
                            Text(
                                text = "Объедините связанные новости в хронологию или создайте развивающийся сюжет с уведомлениями.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = onAddToTopicClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("create_or_link_topic_button")
                            ) {
                                Text("Создать сюжет / Прикрепить к хронологии")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // External browser button
                if (article.link.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.link))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_in_browser_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Читать полный материал в источнике")
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
