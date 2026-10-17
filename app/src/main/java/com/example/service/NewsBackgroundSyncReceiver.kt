package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.ArticleEntity
import com.example.data.local.NewsDatabase
import com.example.data.remote.InitialNewsData
import com.example.data.remote.NewsRssFetcher
import com.example.util.NewsPreferences
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewsBackgroundSyncReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = NewsPreferences(context)
        val action = intent.action

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            if (prefs.isBackgroundSyncEnabled) {
                scheduleSync(context, prefs.backgroundSyncIntervalMinutes)
            }
            return
        }

        if (!prefs.isBackgroundSyncEnabled) {
            cancelSync(context)
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                performSync(context, prefs)
            } catch (e: Exception) {
                Log.e("NewsSyncReceiver", "Error in background sync", e)
            } finally {
                // Re-schedule next trigger
                scheduleSync(context, prefs.backgroundSyncIntervalMinutes)
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 4001
        const val ACTION_SYNC_NOW = "com.example.action.SYNC_NOW"

        suspend fun performSync(context: Context, prefs: NewsPreferences): Int {
            val db = NewsDatabase.getInstance(context)
            val articleDao = db.articleDao()
            val fetcher = NewsRssFetcher()

            val enabledIds = prefs.getEnabledSourceIds()
            val allSources = InitialNewsData.getDefaultSources()
            val activeSources = allSources.filter { enabledIds.contains(it.id) }.take(8)

            val bookmarkedIds = try { articleDao.getBookmarkedArticleIds().toSet() } catch (_: Exception) { emptySet() }
            val readIds = try { articleDao.getReadArticleIds().toSet() } catch (_: Exception) { emptySet() }

            var newCount = 0
            for (source in activeSources) {
                try {
                    val articles = fetcher.fetchFeed(source)
                    if (articles.isNotEmpty()) {
                        val entities = articles.map { article ->
                            ArticleEntity.fromDomain(article, bookmarkedIds.contains(article.id), readIds.contains(article.id))
                        }
                        articleDao.insertArticles(entities)
                        newCount += entities.size
                    }
                } catch (e: Exception) {
                    Log.w("NewsSyncReceiver", "Failed feed ${source.name}", e)
                }
            }

            prefs.lastBackgroundSyncTime = System.currentTimeMillis()

            if (newCount > 0) {
                NotificationHelper.sendNotification(
                    context = context,
                    title = "📰 Свежие новости в ленте",
                    message = "Фоновое обновление: поступило $newCount новых публикаций."
                )
            }

            return newCount
        }

        fun scheduleSync(context: Context, intervalMinutes: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, NewsBackgroundSyncReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerAt = System.currentTimeMillis() + (intervalMinutes * 60 * 1000L)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
                Log.d("NewsSyncReceiver", "Background sync scheduled in $intervalMinutes minutes")
            } catch (e: Exception) {
                Log.e("NewsSyncReceiver", "Failed to schedule sync alarm", e)
            }
        }

        fun cancelSync(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, NewsBackgroundSyncReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                Log.d("NewsSyncReceiver", "Background sync cancelled")
            }
        }
    }
}
