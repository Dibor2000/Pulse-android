package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.remote.InitialNewsData

class NewsPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("newspulse_prefs", Context.MODE_PRIVATE)

    var autoRefreshIntervalSec: Int
        get() = prefs.getInt("auto_refresh_interval_sec", 60)
        set(value) = prefs.edit().putInt("auto_refresh_interval_sec", value).apply()

    var isBackgroundSyncEnabled: Boolean
        get() = prefs.getBoolean("is_background_sync_enabled", true)
        set(value) = prefs.edit().putBoolean("is_background_sync_enabled", value).apply()

    var backgroundSyncIntervalMinutes: Int
        get() = prefs.getInt("background_sync_interval_minutes", 60)
        set(value) = prefs.edit().putInt("background_sync_interval_minutes", value).apply()

    var lastBackgroundSyncTime: Long
        get() = prefs.getLong("last_background_sync_time", 0L)
        set(value) = prefs.edit().putLong("last_background_sync_time", value).apply()

    var readerFontSizeSp: Int
        get() = prefs.getInt("reader_font_size_sp", 17)
        set(value) = prefs.edit().putInt("reader_font_size_sp", value).apply()

    var layoutMode: String
        get() = prefs.getString("layout_mode", "CARDS") ?: "CARDS"
        set(value) = prefs.edit().putString("layout_mode", value).apply()

    var targetLanguage: String
        get() = prefs.getString("target_language", "ru") ?: "ru"
        set(value) = prefs.edit().putString("target_language", value).apply()

    var filterUnreadOnly: Boolean
        get() = prefs.getBoolean("filter_unread_only", false)
        set(value) = prefs.edit().putBoolean("filter_unread_only", value).apply()

    fun getEnabledSourceIds(): Set<String> {
        val stored = prefs.getStringSet("enabled_source_ids", null)
        return stored ?: InitialNewsData.getDefaultSources().filter { it.isEnabled }.map { it.id }.toSet()
    }

    fun setEnabledSourceIds(ids: Set<String>) {
        prefs.edit().putStringSet("enabled_source_ids", ids).apply()
    }

    fun getFavoriteSourceIds(): Set<String> {
        // By default, no sources are automatically favorited on initial launch unless user marked them
        return prefs.getStringSet("favorite_source_ids", emptySet()) ?: emptySet()
    }

    fun setFavoriteSourceIds(ids: Set<String>) {
        prefs.edit().putStringSet("favorite_source_ids", ids).apply()
    }
}
