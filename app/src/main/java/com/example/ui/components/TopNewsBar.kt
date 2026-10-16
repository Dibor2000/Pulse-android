package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LayoutMode
import com.example.ui.theme.NewsCrimson
import com.example.ui.theme.NewsEmerald
import com.example.ui.theme.NewsLivePulse

@Composable
fun TopNewsBar(
    isRefreshing: Boolean,
    countdownSeconds: Int,
    autoRefreshIntervalSec: Int,
    layoutMode: LayoutMode,
    isSearchActive: Boolean,
    searchQuery: String,
    notificationsCount: Int = 0,
    onSearchToggle: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onLayoutToggle: () -> Unit,
    onNotificationCenterClick: () -> Unit,
    onManualRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Logo / Title
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ПУЛЬС",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = NewsCrimson,
                            modifier = Modifier
                                .background(
                                    color = NewsCrimson.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Live badge & countdown
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(NewsLivePulse)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (autoRefreshIntervalSec > 0) {
                                "Эфир • Обновление через ${countdownSeconds}с"
                            } else {
                                "Эфир • Ручное обновление"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Notification Center Button with count badge
                Box {
                    IconButton(
                        onClick = onNotificationCenterClick,
                        modifier = Modifier.testTag("notification_center_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Уведомления",
                            tint = if (notificationsCount > 0) NewsCrimson else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (notificationsCount > 0) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 4.dp, end = 4.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(NewsCrimson)
                        ) {
                            Text(
                                text = if (notificationsCount > 9) "9+" else notificationsCount.toString(),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Search Toggle Button
                IconButton(
                    onClick = onSearchToggle,
                    modifier = Modifier.testTag("search_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Поиск новостей",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Layout Mode Toggle Button (Cards vs Compact)
                IconButton(
                    onClick = onLayoutToggle,
                    modifier = Modifier.testTag("layout_toggle_button")
                ) {
                    Icon(
                        imageVector = if (layoutMode == LayoutMode.CARDS) Icons.Default.ViewAgenda else Icons.Default.GridView,
                        contentDescription = "Переключить вид",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Manual Refresh Button
                IconButton(
                    onClick = onManualRefresh,
                    enabled = !isRefreshing,
                    modifier = Modifier.testTag("refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Обновить ленту",
                        tint = if (isRefreshing) NewsCrimson else MaterialTheme.colorScheme.primary,
                        modifier = if (isRefreshing) Modifier.rotate(spinRotation) else Modifier
                    )
                }
            }

            // Search Bar Input Field when active
            if (isSearchActive) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Поиск по заголовку или источнику...") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Очистить"
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input_field")
                )
            }
        }
    }
}
