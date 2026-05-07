package com.notiontasks.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notiontasks.data.model.NotionSource
import com.notiontasks.data.repository.NotionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SourcesUiState(
    val connectedSources: List<NotionSource> = emptyList(),
    val searchResults: List<NotionSource> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SourcesViewModel @Inject constructor(
    private val repository: NotionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SourcesUiState())
    val state: StateFlow<SourcesUiState> = _state.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            repository.getAllSources().collect { sources ->
                _state.update { it.copy(connectedSources = sources) }
            }
        }

        // Debounced search
        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            searchQueryFlow
                .debounce(400)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.length >= 1) {
                        _state.update { it.copy(isSearching = true) }
                        repository.searchAvailableSources(query)
                            .onSuccess { results ->
                                _state.update { it.copy(searchResults = results, isSearching = false) }
                            }
                            .onFailure { e ->
                                _state.update { it.copy(isSearching = false, error = e.message) }
                            }
                    } else {
                        _state.update { it.copy(searchResults = emptyList()) }
                    }
                }
        }

        // Load all on start
        viewModelScope.launch {
            repository.searchAvailableSources()
                .onSuccess { results ->
                    _state.update { it.copy(searchResults = results) }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }

    fun addSource(source: NotionSource) {
        viewModelScope.launch {
            repository.addSource(source)
            // Auto-sync after adding
        }
    }

    fun removeSource(id: String) {
        viewModelScope.launch { repository.removeSource(id) }
    }

    fun toggleSource(id: String, enabled: Boolean) {
        viewModelScope.launch { repository.toggleSource(id, enabled) }
    }
}
