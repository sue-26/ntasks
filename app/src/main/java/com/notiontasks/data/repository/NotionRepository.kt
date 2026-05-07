package com.notiontasks.data.repository

import android.util.Base64
import com.notiontasks.BuildConfig
import com.notiontasks.data.api.*
import com.notiontasks.data.model.*
import kotlinx.coroutines.flow.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotionRepository @Inject constructor(
    private val api: NotionApiService,
    private val sourceDao: SourceDao,
    private val taskDao: TaskDao,
    private val prefs: PreferencesRepository
) {

    // ─── Auth ──────────────────────────────────────────────────────────────

    suspend fun exchangeCodeForToken(code: String): Result<NotionTokenResponse> = runCatching {
        val credentials = "${BuildConfig.NOTION_CLIENT_ID}:${BuildConfig.NOTION_CLIENT_SECRET}"
        val basicAuth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        val body = "grant_type=authorization_code&code=$code&redirect_uri=${BuildConfig.NOTION_REDIRECT_URI}"
            .toRequestBody("application/x-www-form-urlencoded".toMediaType())
        api.exchangeToken(BuildConfig.NOTION_OAUTH_URL, basicAuth, body)
    }

    // ─── Sources ───────────────────────────────────────────────────────────

    fun getAllSources(): Flow<List<NotionSource>> =
        sourceDao.getAllSources().map { list ->
            list.map { it.toDomain() }
        }

    suspend fun addSource(source: NotionSource) {
        sourceDao.insertSource(source.toEntity())
    }

    suspend fun removeSource(id: String) {
        sourceDao.deleteById(id)
        taskDao.deleteBySource(id)
    }

    suspend fun toggleSource(id: String, enabled: Boolean) {
        sourceDao.setEnabled(id, enabled)
    }

    /**
     * Search for pages and databases the user has access to.
     */
    suspend fun searchAvailableSources(query: String = ""): Result<List<NotionSource>> = runCatching {
        val results = mutableListOf<NotionSource>()

        // Search pages
        val pageReq = NotionSearchRequest(
            filter = NotionSearchFilter("page"),
            sort = NotionSearchSort()
        )
        val pages = api.search(pageReq)
        results += pages.results
            .filter { !it.archived }
            .filter { query.isEmpty() || it.getTitle().contains(query, ignoreCase = true) }
            .map { page ->
                NotionSource(
                    id = page.id,
                    name = page.getTitle(),
                    type = SourceType.PAGE,
                    icon = page.icon?.emoji
                )
            }

        // Search databases
        val dbReq = NotionSearchRequest(
            filter = NotionSearchFilter("database"),
            sort = NotionSearchSort()
        )
        val databases = api.search(dbReq)
        results += databases.results
            .filter { !it.archived }
            .filter { query.isEmpty() || it.getTitle().contains(query, ignoreCase = true) }
            .map { db ->
                NotionSource(
                    id = db.id,
                    name = db.getTitle(),
                    type = SourceType.DATABASE,
                    icon = db.icon?.emoji
                )
            }

        results
    }

    // ─── Tasks ─────────────────────────────────────────────────────────────

    fun getAllTasks(): Flow<List<Task>> =
        taskDao.getAllTasks().map { list -> list.map { it.toDomain() } }

    /**
     * Full sync: fetch all tasks from all enabled sources.
     */
    suspend fun syncAll(): Result<Int> = runCatching {
        val sources = sourceDao.getEnabledSources().first()
        var count = 0
        for (source in sources) {
            val tasks = when (source.type) {
                "PAGE" -> fetchTasksFromPage(source.id, source.name)
                "DATABASE" -> fetchTasksFromDatabase(source.id, source.name)
                else -> emptyList()
            }
            taskDao.deleteBySource(source.id)
            taskDao.insertTasks(tasks.map { it.toEntity() })
            count += tasks.size
        }
        count
    }

    /**
     * Fetch checklist (to_do) blocks from a page recursively.
     */
    private suspend fun fetchTasksFromPage(
        pageId: String,
        pageName: String,
        parentId: String = pageId
    ): List<Task> {
        val tasks = mutableListOf<Task>()
        var cursor: String? = null

        do {
            val response = api.getBlockChildren(parentId, startCursor = cursor)
            for (block in response.results) {
                if (block.type == "to_do" && block.toDo != null && !block.archived) {
                    tasks += Task(
                        id = "${pageId}_${block.id}",
                        title = block.toDo.plainText(),
                        isChecked = block.toDo.checked,
                        sourceId = pageId,
                        sourceName = pageName,
                        sourceType = SourceType.PAGE,
                        blockId = block.id,
                        propertyName = null,
                        createdTime = block.createdTime,
                        lastEditedTime = block.lastEditedTime
                    )
                }
                // Recurse into children if needed
                if (block.hasChildren && block.type != "child_page") {
                    tasks += fetchTasksFromPage(pageId, pageName, block.id)
                }
            }
            cursor = if (response.hasMore) response.nextCursor else null
        } while (cursor != null)

        return tasks
    }

    /**
     * Fetch rows with checkbox properties from a Notion database.
     */
    private suspend fun fetchTasksFromDatabase(
        databaseId: String,
        dbName: String
    ): List<Task> {
        val tasks = mutableListOf<Task>()
        var cursor: String? = null

        do {
            val request = NotionDatabaseQueryRequest(startCursor = cursor)
            val response = api.queryDatabase(databaseId, request)

            for (page in response.results) {
                if (page.archived) continue
                val props = page.properties ?: continue
                val titleText = page.getTitle()

                // Find the first checkbox property to use as task status
                val checkboxProp = props.entries.firstOrNull { entry ->
                    (entry.value as? Map<*, *>)?.get("type") == "checkbox"
                }
                val isChecked = checkboxProp?.let { entry ->
                    (entry.value as? Map<*, *>)?.get("checkbox") as? Boolean
                } ?: false

                // Try to get due date
                val dueDateProp = props.entries.firstOrNull { entry ->
                    (entry.value as? Map<*, *>)?.get("type") == "date" &&
                            entry.key.lowercase().contains("due")
                }
                val dueDate = dueDateProp?.let { entry ->
                    ((entry.value as? Map<*, *>)?.get("date") as? Map<*, *>)?.get("start") as? String
                }

                tasks += Task(
                    id = "${databaseId}_${page.id}",
                    title = titleText,
                    isChecked = isChecked,
                    sourceId = databaseId,
                    sourceName = dbName,
                    sourceType = SourceType.DATABASE,
                    blockId = null,
                    propertyName = checkboxProp?.key,
                    createdTime = page.createdTime,
                    lastEditedTime = page.lastEditedTime,
                    dueDate = dueDate
                )
            }
            cursor = if (response.hasMore) response.nextCursor else null
        } while (cursor != null)

        return tasks
    }

    // ─── Check/Uncheck sync back to Notion ────────────────────────────────

    suspend fun toggleTask(task: Task, checked: Boolean): Result<Unit> = runCatching {
        // Optimistically update local cache
        taskDao.setChecked(task.id, checked)

        when (task.sourceType) {
            SourceType.PAGE -> {
                // Update the to_do block
                val blockId = task.blockId ?: throw IllegalStateException("No block ID for page task")
                api.updateBlock(
                    blockId,
                    NotionUpdateBlockRequest(NotionToDoUpdate(checked))
                )
            }
            SourceType.DATABASE -> {
                // Update the checkbox property on the page
                val pageId = task.id.removePrefix("${task.sourceId}_")
                val propName = task.propertyName ?: throw IllegalStateException("No property name")
                api.updatePage(
                    pageId,
                    NotionUpdatePageRequest(
                        mapOf(propName to mapOf("checkbox" to checked))
                    )
                )
            }
        }
    }

    // ─── View settings ────────────────────────────────────────────────────

    val viewSettings: Flow<TaskViewSettings> = prefs.viewSettings
    suspend fun saveViewSettings(settings: TaskViewSettings) = prefs.saveViewSettings(settings)
}

// ─── Mapping extensions ───────────────────────────────────────────────────────

private fun SourceEntity.toDomain() = NotionSource(
    id = id, name = name,
    type = SourceType.valueOf(type),
    icon = icon, isEnabled = isEnabled
)

private fun NotionSource.toEntity() = SourceEntity(
    id = id, name = name, type = type.name,
    icon = icon, isEnabled = isEnabled
)

private fun TaskEntity.toDomain() = Task(
    id = id, title = title, isChecked = isChecked,
    sourceId = sourceId, sourceName = sourceName,
    sourceType = SourceType.valueOf(sourceType),
    blockId = blockId, propertyName = propertyName,
    createdTime = createdTime, lastEditedTime = lastEditedTime,
    dueDate = dueDate
)

private fun Task.toEntity() = TaskEntity(
    id = id, title = title, isChecked = isChecked,
    sourceId = sourceId, sourceName = sourceName,
    sourceType = sourceType.name,
    blockId = blockId, propertyName = propertyName,
    createdTime = createdTime, lastEditedTime = lastEditedTime,
    dueDate = dueDate
)
