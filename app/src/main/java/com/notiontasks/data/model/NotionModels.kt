package com.notiontasks.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ─── OAuth ────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class NotionTokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "bot_id") val botId: String,
    @Json(name = "workspace_name") val workspaceName: String?,
    @Json(name = "workspace_icon") val workspaceIcon: String?,
    @Json(name = "workspace_id") val workspaceId: String,
    @Json(name = "owner") val owner: NotionOwner?
)

@JsonClass(generateAdapter = true)
data class NotionOwner(
    @Json(name = "type") val type: String,
    @Json(name = "user") val user: NotionUser?
)

@JsonClass(generateAdapter = true)
data class NotionUser(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String?,
    @Json(name = "avatar_url") val avatarUrl: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "person") val person: NotionPerson?
)

@JsonClass(generateAdapter = true)
data class NotionPerson(
    @Json(name = "email") val email: String?
)

// ─── Search / Pages / Databases ───────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class NotionSearchRequest(
    @Json(name = "filter") val filter: NotionSearchFilter? = null,
    @Json(name = "sort") val sort: NotionSearchSort? = null,
    @Json(name = "start_cursor") val startCursor: String? = null,
    @Json(name = "page_size") val pageSize: Int = 100
)

@JsonClass(generateAdapter = true)
data class NotionSearchFilter(
    @Json(name = "value") val value: String, // "page" or "database"
    @Json(name = "property") val property: String = "object"
)

@JsonClass(generateAdapter = true)
data class NotionSearchSort(
    @Json(name = "direction") val direction: String = "descending",
    @Json(name = "timestamp") val timestamp: String = "last_edited_time"
)

@JsonClass(generateAdapter = true)
data class NotionListResponse<T>(
    @Json(name = "object") val objectType: String,
    @Json(name = "results") val results: List<T>,
    @Json(name = "next_cursor") val nextCursor: String?,
    @Json(name = "has_more") val hasMore: Boolean
)

@JsonClass(generateAdapter = true)
data class NotionPage(
    @Json(name = "object") val objectType: String,       // "page" or "database"
    @Json(name = "id") val id: String,
    @Json(name = "created_time") val createdTime: String,
    @Json(name = "last_edited_time") val lastEditedTime: String,
    @Json(name = "archived") val archived: Boolean,
    @Json(name = "icon") val icon: NotionIcon?,
    @Json(name = "parent") val parent: NotionParent?,
    @Json(name = "properties") val properties: Map<String, Any?>?,
    @Json(name = "url") val url: String?
) {
    fun getTitle(): String {
        if (objectType == "database") {
            val titleProp = properties?.get("title") as? List<*>
            return (titleProp?.firstOrNull() as? Map<*, *>)
                ?.get("plain_text")?.toString() ?: "Untitled Database"
        }
        val titleProp = properties?.get("title") as? Map<*, *>
        val titleArr = titleProp?.get("title") as? List<*>
        return (titleArr?.firstOrNull() as? Map<*, *>)
            ?.get("plain_text")?.toString() ?: "Untitled"
    }
}

@JsonClass(generateAdapter = true)
data class NotionIcon(
    @Json(name = "type") val type: String,
    @Json(name = "emoji") val emoji: String?,
    @Json(name = "external") val external: NotionFile?
)

@JsonClass(generateAdapter = true)
data class NotionFile(
    @Json(name = "url") val url: String
)

@JsonClass(generateAdapter = true)
data class NotionParent(
    @Json(name = "type") val type: String,
    @Json(name = "database_id") val databaseId: String?,
    @Json(name = "page_id") val pageId: String?,
    @Json(name = "workspace") val workspace: Boolean?
)

// ─── Blocks (Checklist items from Pages) ─────────────────────────────────────

@JsonClass(generateAdapter = true)
data class NotionBlock(
    @Json(name = "object") val objectType: String,
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String,
    @Json(name = "created_time") val createdTime: String,
    @Json(name = "last_edited_time") val lastEditedTime: String,
    @Json(name = "archived") val archived: Boolean,
    @Json(name = "has_children") val hasChildren: Boolean,
    @Json(name = "to_do") val toDo: NotionToDo?
)

@JsonClass(generateAdapter = true)
data class NotionToDo(
    @Json(name = "rich_text") val richText: List<NotionRichText>,
    @Json(name = "checked") val checked: Boolean,
    @Json(name = "color") val color: String?
) {
    fun plainText(): String = richText.joinToString("") { it.plainText ?: "" }
}

@JsonClass(generateAdapter = true)
data class NotionRichText(
    @Json(name = "type") val type: String,
    @Json(name = "plain_text") val plainText: String?,
    @Json(name = "annotations") val annotations: NotionAnnotations?
)

@JsonClass(generateAdapter = true)
data class NotionAnnotations(
    @Json(name = "bold") val bold: Boolean,
    @Json(name = "italic") val italic: Boolean,
    @Json(name = "strikethrough") val strikethrough: Boolean,
    @Json(name = "underline") val underline: Boolean,
    @Json(name = "code") val code: Boolean,
    @Json(name = "color") val color: String
)

// ─── Database Query (for checkbox properties in databases) ────────────────────

@JsonClass(generateAdapter = true)
data class NotionDatabaseQueryRequest(
    @Json(name = "filter") val filter: Any? = null,
    @Json(name = "sorts") val sorts: List<Any>? = null,
    @Json(name = "start_cursor") val startCursor: String? = null,
    @Json(name = "page_size") val pageSize: Int = 100
)

// ─── Update block (check/uncheck) ─────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class NotionUpdateBlockRequest(
    @Json(name = "to_do") val toDo: NotionToDoUpdate
)

@JsonClass(generateAdapter = true)
data class NotionToDoUpdate(
    @Json(name = "checked") val checked: Boolean
)

// ─── Update page property (checkbox in database) ───────────────────────────────

@JsonClass(generateAdapter = true)
data class NotionUpdatePageRequest(
    @Json(name = "properties") val properties: Map<String, Any>
)

// ─── Local domain model ───────────────────────────────────────────────────────

data class Task(
    val id: String,
    val title: String,
    val isChecked: Boolean,
    val sourceId: String,        // page or database ID
    val sourceName: String,
    val sourceType: SourceType,  // PAGE or DATABASE
    val blockId: String?,        // for page checklist blocks
    val propertyName: String?,   // for database checkbox property
    val createdTime: String,
    val lastEditedTime: String,
    val dueDate: String? = null
)

enum class SourceType { PAGE, DATABASE }

data class NotionSource(
    val id: String,
    val name: String,
    val type: SourceType,
    val icon: String?,
    val isEnabled: Boolean = true
)

// ─── Filter / Sort / Group settings ──────────────────────────────────────────

data class TaskViewSettings(
    val showCompleted: Boolean = true,
    val showOverdue: Boolean = true,
    val filterSourceIds: Set<String> = emptySet(), // empty = all
    val sortField: SortField = SortField.CREATED_TIME,
    val sortOrder: SortOrder = SortOrder.DESCENDING,
    val groupBy: GroupBy = GroupBy.NONE
)

enum class SortField(val label: String) {
    TITLE("Title"),
    CREATED_TIME("Created"),
    LAST_EDITED_TIME("Last Edited"),
    DUE_DATE("Due Date"),
    SOURCE("Source")
}

enum class SortOrder(val label: String) {
    ASCENDING("Ascending"),
    DESCENDING("Descending")
}

enum class GroupBy(val label: String) {
    NONE("None"),
    SOURCE("Source"),
    STATUS("Status"),
    DUE_DATE("Due Date")
}
