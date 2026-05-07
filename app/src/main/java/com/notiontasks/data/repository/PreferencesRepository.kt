package com.notiontasks.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.notiontasks.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notion_tasks_prefs")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val WORKSPACE_NAME = stringPreferencesKey("workspace_name")
        val WORKSPACE_ID = stringPreferencesKey("workspace_id")
        val USER_NAME = stringPreferencesKey("user_name")
        // View settings
        val SHOW_COMPLETED = booleanPreferencesKey("show_completed")
        val SHOW_OVERDUE = booleanPreferencesKey("show_overdue")
        val SORT_FIELD = stringPreferencesKey("sort_field")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val GROUP_BY = stringPreferencesKey("group_by")
    }

    // ─── Auth token ───────────────────────────────────────────────────────

    val accessToken: Flow<String?> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.ACCESS_TOKEN] }

    val isAuthenticated: Flow<Boolean> = accessToken.map { !it.isNullOrBlank() }

    val workspaceName: Flow<String?> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.WORKSPACE_NAME] }

    val userName: Flow<String?> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.USER_NAME] }

    suspend fun saveAuthToken(
        token: String,
        workspaceName: String?,
        workspaceId: String?,
        userName: String?
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = token
            workspaceName?.let { prefs[Keys.WORKSPACE_NAME] = it }
            workspaceId?.let { prefs[Keys.WORKSPACE_ID] = it }
            userName?.let { prefs[Keys.USER_NAME] = it }
        }
    }

    suspend fun clearAuth() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.ACCESS_TOKEN)
            prefs.remove(Keys.WORKSPACE_NAME)
            prefs.remove(Keys.WORKSPACE_ID)
            prefs.remove(Keys.USER_NAME)
        }
    }

    // ─── View settings ────────────────────────────────────────────────────

    val viewSettings: Flow<TaskViewSettings> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            TaskViewSettings(
                showCompleted = prefs[Keys.SHOW_COMPLETED] ?: true,
                showOverdue = prefs[Keys.SHOW_OVERDUE] ?: true,
                sortField = prefs[Keys.SORT_FIELD]?.let { SortField.valueOf(it) }
                    ?: SortField.CREATED_TIME,
                sortOrder = prefs[Keys.SORT_ORDER]?.let { SortOrder.valueOf(it) }
                    ?: SortOrder.DESCENDING,
                groupBy = prefs[Keys.GROUP_BY]?.let { GroupBy.valueOf(it) } ?: GroupBy.NONE
            )
        }

    suspend fun saveViewSettings(settings: TaskViewSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SHOW_COMPLETED] = settings.showCompleted
            prefs[Keys.SHOW_OVERDUE] = settings.showOverdue
            prefs[Keys.SORT_FIELD] = settings.sortField.name
            prefs[Keys.SORT_ORDER] = settings.sortOrder.name
            prefs[Keys.GROUP_BY] = settings.groupBy.name
        }
    }
}
