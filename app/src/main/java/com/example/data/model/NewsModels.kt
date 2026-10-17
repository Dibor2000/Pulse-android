package com.example.data.model

enum class NewsCategory(val displayName: String, val iconName: String) {
    ALL("Все новости", "auto_awesome"),
    TECH("Технологии", "memory"),
    WORLD("В мире", "public"),
    SCIENCE("Наука & Космос", "science"),
    BUSINESS("Экономика", "trending_up"),
    GAMES("Игры & Медиа", "sports_esports");

    companion object {
        fun fromString(value: String): NewsCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: ALL
        }
    }
}

data class NewsSource(
    val id: String,
    val name: String,
    val url: String,
    val feedUrl: String,
    val category: NewsCategory,
    val country: String = "Мир",
    val flag: String = "🌐",
    val language: String = "RU",
    val isEnabled: Boolean = true,
    val isFavorite: Boolean = false,
    val lastFetchedCount: Int = 0,
    val status: String = "Онлайн"
)

data class UserPersonalization(
    val isPersonalizedFeedActive: Boolean = false,
    val selectedCategoryNames: Set<String> = setOf("TECH", "WORLD", "SCIENCE"),
    val favoriteSourceIds: Set<String> = setOf("bbc_world", "cnn", "reuters", "habr", "techcrunch"),
    val customKeywords: List<String> = emptyList()
)

data class NewsArticle(
    val id: String,
    val title: String,
    val description: String,
    val content: String,
    val link: String,
    val sourceName: String,
    val sourceUrl: String,
    val imageUrl: String? = null,
    val category: NewsCategory,
    val publishedAt: Long, // Epoch millis
    val isBreaking: Boolean = false,
    val isBookmarked: Boolean = false,
    val isRead: Boolean = false,
    val readTimeMinutes: Int = 3,
    val topicId: String? = null
)

enum class MilestoneType(val label: String) {
    START("Начало событий"),
    DEVELOPMENT("Развитие темы"),
    BREAKING("Срочное обновление"),
    MILESTONE("Ключевой этап"),
    CONCLUSION("Итоги и анализ")
}

data class ChronologyEvent(
    val id: String,
    val topicId: String,
    val title: String,
    val summary: String,
    val timestamp: Long,
    val sourceName: String,
    val milestoneType: MilestoneType = MilestoneType.DEVELOPMENT,
    val articleId: String? = null
)

data class TopicStory(
    val id: String,
    val title: String,
    val summary: String,
    val category: NewsCategory,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val events: List<ChronologyEvent> = emptyList(),
    val articleIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

