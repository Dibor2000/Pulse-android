package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimeUtils {

    private val rfc822Formats = listOf(
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US),
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm Z", Locale.US),
        SimpleDateFormat("dd MMM yyyy HH:mm:ss Z", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    )

    fun parseDate(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
        val trimmed = dateStr.trim()
        for (format in rfc822Formats) {
            try {
                val date = format.parse(trimmed)
                if (date != null) return date.time
            } catch (_: Exception) {}
        }
        return System.currentTimeMillis()
    }

    fun formatRelativeTime(epochMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = (now - epochMillis).coerceAtLeast(0)

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Только что"
            minutes < 2 -> "1 минуту назад"
            minutes in 2..4 -> "$minutes минуты назад"
            minutes in 5..20 -> "$minutes минут назад"
            minutes in 21..59 -> {
                val lastDigit = (minutes % 10).toInt()
                when (lastDigit) {
                    1 -> "$minutes минуту назад"
                    2, 3, 4 -> "$minutes минуты назад"
                    else -> "$minutes минут назад"
                }
            }
            hours == 1L -> "1 час назад"
            hours in 2..4 -> "$hours часа назад"
            hours in 5..20 -> "$hours часов назад"
            hours in 21..24 -> "$hours часа назад"
            days == 1L -> {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                "Вчера в ${timeFormat.format(Date(epochMillis))}"
            }
            days in 2..6 -> "$days дн. назад"
            else -> {
                val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale("ru"))
                dateFormat.format(Date(epochMillis))
            }
        }
    }

    fun formatExactDateTime(epochMillis: Long): String {
        val dateFormat = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru"))
        return dateFormat.format(Date(epochMillis))
    }
}
