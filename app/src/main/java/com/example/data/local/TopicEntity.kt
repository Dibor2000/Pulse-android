package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ChronologyEvent
import com.example.data.model.MilestoneType
import com.example.data.model.NewsCategory
import com.example.data.model.TopicStory
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val category: String,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val eventsJson: String = "[]",
    val articleIdsJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): TopicStory {
        val parsedEvents = mutableListOf<ChronologyEvent>()
        try {
            val jsonArray = JSONArray(eventsJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                parsedEvents.add(
                    ChronologyEvent(
                        id = obj.getString("id"),
                        topicId = obj.getString("topicId"),
                        title = obj.getString("title"),
                        summary = obj.getString("summary"),
                        timestamp = obj.getLong("timestamp"),
                        sourceName = obj.optString("sourceName", "СМИ"),
                        milestoneType = try {
                            MilestoneType.valueOf(obj.getString("milestoneType"))
                        } catch (_: Exception) {
                            MilestoneType.DEVELOPMENT
                        },
                        articleId = if (obj.has("articleId") && !obj.isNull("articleId")) obj.getString("articleId") else null
                    )
                )
            }
        } catch (_: Exception) {
            // Ignore parse errors
        }

        val parsedArticleIds = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(articleIdsJson)
            for (i in 0 until jsonArray.length()) {
                parsedArticleIds.add(jsonArray.getString(i))
            }
        } catch (_: Exception) {
            // Ignore parse errors
        }

        return TopicStory(
            id = id,
            title = title,
            summary = summary,
            category = NewsCategory.fromString(category),
            isPinned = isPinned,
            isArchived = isArchived,
            events = parsedEvents,
            articleIds = parsedArticleIds,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(domain: TopicStory): TopicEntity {
            val eventsArray = JSONArray()
            domain.events.forEach { ev ->
                val obj = JSONObject().apply {
                    put("id", ev.id)
                    put("topicId", ev.topicId)
                    put("title", ev.title)
                    put("summary", ev.summary)
                    put("timestamp", ev.timestamp)
                    put("sourceName", ev.sourceName)
                    put("milestoneType", ev.milestoneType.name)
                    put("articleId", ev.articleId)
                }
                eventsArray.put(obj)
            }

            val articleIdsArray = JSONArray()
            domain.articleIds.forEach { articleIdsArray.put(it) }

            return TopicEntity(
                id = domain.id,
                title = domain.title,
                summary = domain.summary,
                category = domain.category.name,
                isPinned = domain.isPinned,
                isArchived = domain.isArchived,
                eventsJson = eventsArray.toString(),
                articleIdsJson = articleIdsArray.toString(),
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
        }
    }
}
