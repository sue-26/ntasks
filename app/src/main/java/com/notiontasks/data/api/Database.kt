package com.notiontasks.data.api

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ─── Entities ─────────────────────────────────────────────────────────────────

@Entity(tableName = "sources")
data class SourceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,       // "PAGE" or "DATABASE"
    val icon: String?,
    val isEnabled: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val isChecked: Boolean,
    val sourceId: String,
    val sourceName: String,
    val sourceType: String,
    val blockId: String?,
    val propertyName: String?,
    val createdTime: String,
    val lastEditedTime: String,
    val dueDate: String?,
    val syncedAt: Long = System.currentTimeMillis()
)

// ─── DAOs ─────────────────────────────────────────────────────────────────────

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources ORDER BY addedAt ASC")
    fun getAllSources(): Flow<List<SourceEntity>>

    @Query("SELECT * FROM sources WHERE isEnabled = 1 ORDER BY addedAt ASC")
    fun getEnabledSources(): Flow<List<SourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: SourceEntity)

    @Update
    suspend fun updateSource(source: SourceEntity)

    @Delete
    suspend fun deleteSource(source: SourceEntity)

    @Query("DELETE FROM sources WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE sources SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY lastEditedTime DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE sourceId = :sourceId ORDER BY lastEditedTime DESC")
    fun getTasksBySource(sourceId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isChecked = 0 ORDER BY lastEditedTime DESC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET isChecked = :checked WHERE id = :id")
    suspend fun setChecked(id: String, checked: Boolean)

    @Query("DELETE FROM tasks WHERE sourceId = :sourceId")
    suspend fun deleteBySource(sourceId: String)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}

// ─── Database ─────────────────────────────────────────────────────────────────

@Database(
    entities = [SourceEntity::class, TaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NotionTasksDatabase : RoomDatabase() {
    abstract fun sourceDao(): SourceDao
    abstract fun taskDao(): TaskDao
}
