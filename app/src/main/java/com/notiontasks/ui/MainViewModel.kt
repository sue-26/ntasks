package com.notiontasks.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notiontasks.data.model.*
import com.notiontasks.data.repository.NotionRepository
import com.notiontasks.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val groupedTasks: Map<String, List<Task>> = emptyMap(),
    val sources: List<NotionSource> = emptyList(),
    val viewSettings: TaskViewSettings = TaskViewSettings(),
    val error: String? = null,
    val lastSyncedAt: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: NotionRepository,
    private val prefs: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // Observe auth state
        viewModelScope.launch {
            prefs.isAuthenticated.collect { auth ->
                _uiState.update { it.copy(isAuthenticated = auth) }
                if (auth) loadData()
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            // Combine tasks + settings into a reactive stream
            combine(
                repository.getAllTasks(),
                repository.viewSettings,
                repository.getAllSources()
            ) { tasks, settings, sources ->
                Triple(tasks, settings, sources)
            }.collect { (tasks, settings, sources) ->
                val filtered = applySettings(tasks, settings)
                val grouped = groupTasks(filtered, settings)
                _uiState.update { state ->
                    state.copy(
                        tasks = filtered,
                        groupedTasks = grouped,
                        viewSettings = settings,
                        sources = sources
                    )
                }
            }
        }
    }

    fun sync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, error = null) }
            repository.syncAll()
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(isSyncing = false, lastSyncedAt = java.time.LocalTime.now().toString())
                    }
                }
                .onFailure { e ->
                    _uiState.update { state ->
                        state.copy(isSyncing = false, error = e.message ?: "Sync failed")
                    }
                }
        }
    }

    fun toggleTask(task: Task, checked: Boolean) {
        viewModelScope.launch {
            repository.toggleTask(task, checked)
                .onFailure { e ->
                    _uiState.update { it.copy(error = "Failed to update task: ${e.message}") }
                }
        }
    }

    fun updateViewSettings(settings: TaskViewSettings) {
        viewModelScope.launch {
            repository.saveViewSettings(settings)
        }
    }

    fun removeSource(id: String) {
        viewModelScope.launch {
            repository.removeSource(id)
        }
    }

    fun toggleSource(id: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleSource(id, enabled)
            sync()
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun connectWithToken(token: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            prefs.saveAuthToken(
                token = token,
                workspaceName = null,
                workspaceId = null,
                userName = null
            )
            _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
            sync()
        }
    }

    fun handleOAuthCallback(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.exchangeCodeForToken(code)
                .onSuccess { tokenResponse ->
                    prefs.saveAuthToken(
                        token = tokenResponse.accessToken,
                        workspaceName = tokenResponse.workspaceName,
                        workspaceId = tokenResponse.workspaceId,
                        userName = tokenResponse.owner?.user?.name
                    )
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                    sync()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            prefs.clearAuth()
            _uiState.update { MainUiState() }
        }
    }

    // ─── Filter / Sort / Group logic ──────────────────────────────────────

    private fun applySettings(tasks: List<Task>, settings: TaskViewSettings): List<Task> {
        var result = tasks

        // Filter
        if (!settings.showCompleted) result = result.filter { !it.isChecked }
        if (settings.filterSourceIds.isNotEmpty()) {
            result = result.filter { it.sourceId in settings.filterSourceIds }
        }

        // Sort
        result = when (settings.sortField) {
            SortField.TITLE -> result.sortedBy { it.title }
            SortField.CREATED_TIME -> result.sortedBy { it.createdTime }
            SortField.LAST_EDITED_TIME -> result.sortedBy { it.lastEditedTime }
            SortField.DUE_DATE -> result.sortedBy { it.dueDate ?: "9999" }
            SortField.SOURCE -> result.sortedBy { it.sourceName }
        }

        if (settings.sortOrder == SortOrder.DESCENDING) result = result.reversed()

        return result
    }

    private fun groupTasks(tasks: List<Task>, settings: TaskViewSettings): Map<String, List<Task>> {
        return when (settings.groupBy) {
            GroupBy.NONE -> mapOf("All Tasks" to tasks)
            GroupBy.SOURCE -> tasks.groupBy { it.sourceName }
            GroupBy.STATUS -> tasks.groupBy { if (it.isChecked) "Completed" else "Pending" }
                .let { map ->
                    val ordered = linkedMapOf<String, List<Task>>()
                    map["Pending"]?.let { ordered["Pending"] = it }
                    map["Completed"]?.let { ordered["Completed"] = it }
                    ordered
                }
            GroupBy.DUE_DATE -> tasks.groupBy { task ->
                task.dueDate?.let { "Due $it" } ?: "No Due Date"
            }
        }
    }
}
