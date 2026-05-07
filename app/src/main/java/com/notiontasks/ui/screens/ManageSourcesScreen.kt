package com.notiontasks.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notiontasks.data.model.NotionSource
import com.notiontasks.data.model.SourceType
import com.notiontasks.ui.SourcesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSourcesScreen(
    onBack: () -> Unit,
    viewModel: SourcesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Sources") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search pages & databases…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (state.isSearching) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (state.connectedSources.isNotEmpty()) {
                SectionHeader("Connected")
                LazyColumn {
                    items(state.connectedSources, key = { it.id }) { source ->
                        SourceRow(
                            source = source,
                            isConnected = true,
                            onToggle = { viewModel.toggleSource(source.id, it) },
                            onRemove = { viewModel.removeSource(source.id) }
                        )
                    }
                }
            }

            if (state.searchResults.isNotEmpty()) {
                SectionHeader("Available")
                LazyColumn {
                    items(state.searchResults.filter { result ->
                        state.connectedSources.none { it.id == result.id }
                    }, key = { it.id }) { source ->
                        SourceRow(
                            source = source,
                            isConnected = false,
                            onAdd = { viewModel.addSource(source) }
                        )
                    }
                }
            }

            if (state.searchResults.isEmpty() && state.connectedSources.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Search for Notion pages or databases to connect",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SourceRow(
    source: NotionSource,
    isConnected: Boolean,
    onToggle: ((Boolean) -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
    onAdd: (() -> Unit)? = null
) {
    var showRemoveDialog by remember { mutableStateOf(false) }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove Source") },
            text = { Text("Remove '${source.name}'? All synced tasks from this source will be deleted locally.") },
            confirmButton = {
                TextButton(onClick = { onRemove?.invoke(); showRemoveDialog = false }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) { Text("Cancel") }
            }
        )
    }

    ListItem(
        headlineContent = { Text(source.name, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = {
            Text(
                if (source.type == SourceType.DATABASE) "Database" else "Page",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(source.icon ?: if (source.type == SourceType.DATABASE) "🗄" else "📄")
                }
            }
        },
        trailingContent = {
            if (isConnected) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = source.isEnabled,
                        onCheckedChange = { onToggle?.invoke(it) },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    IconButton(onClick = { showRemoveDialog = true }) {
                        Icon(Icons.Outlined.DeleteOutline, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                FilledTonalButton(onClick = { onAdd?.invoke() }) {
                    Text("Add")
                }
            }
        }
    )
    HorizontalDivider(
        modifier = Modifier.padding(start = 68.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    )
}
