package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class InAppNotification(
    val id: String,
    val title: String,
    val message: String,
    val topicTitle: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val articleId: String? = null
)

object NotificationHelper {

    private const val CHANNEL_ID = "pinned_news_channel"
    private const val CHANNEL_NAME = "Закреплённые новости и сюжеты"

    private val _notifications = MutableStateFlow<List<InAppNotification>>(emptyList())
    val notifications: StateFlow<List<InAppNotification>> = _notifications.asStateFlow()

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о новых публикациях по закреплённым сюжетам и событиям"
                enableLights(true)
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun hasPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendNotification(
        context: Context,
        title: String,
        message: String,
        topicTitle: String? = null,
        articleId: String? = null
    ) {
        createNotificationChannel(context)

        // Store in-app notification record
        val inApp = InAppNotification(
            id = System.currentTimeMillis().toString(),
            title = title,
            message = message,
            topicTitle = topicTitle,
            articleId = articleId
        )
        _notifications.value = listOf(inApp) + _notifications.value.take(49)

        if (!hasPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("article_id", articleId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
        } catch (_: Exception) {}
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }
}
