package com.notiontasks.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notiontasks.data.model.Task
import com.notiontasks.ui.MainUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskListScreen(
    uiState: MainUiState,
    onToggleTask: (Task, Boolean) -> Unit,
    onSyncClick: () -> Unit,
    onManageSources: () -> Unit,
    onViewSettings: () -> Unit,
    onSignOut: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("All Tasks", style = MaterialTheme.typography.titleLarge)
                        if (uiState.lastSyncedAt != null) {
                            Text(
                                "Synced ${uiState.lastSyncedAt}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Sync button
                    IconButton(onClick = onSyncClick) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Outlined.Sync, contentDescription = "Sync")
                        }
                    }
                    // View settings
                    IconButton(onClick = onViewSettings) {
                        Icon(Icons.Outlined.FilterList, contentDescription = "View Settings")
                    }
                    // Menu
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Manage Sources") },
                                leadingIcon = { Icon(Icons.Outlined.Source, null) },
                                onClick = { showMenu = false; onManageSources() }
                            )
                            DropdownMenuItem(
                                text = { Text("Sign Out") },
                                leadingIcon = { Icon(Icons.Outlined.Logout, null) },
                                onClick = { showMenu = false; onSignOut() }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onManageSources,
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Source")
            }
        }
    ) { padding ->

        // Error snackbar
        if (uiState.error != null) {
            // shown via snackbar in MainActivity
        }

        if (uiState.tasks.isEmpty() && !uiState.isSyncing) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                uiState.groupedTasks.forEach { (group, tasks) ->
                    if (uiState.groupedTasks.size > 1) {
                        stickyHeader {
                            GroupHeader(group, tasks.size)
                        }
                    }
                    items(tasks, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            onToggle = { checked -> onToggleTask(task, checked) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    }
                }
                item { Spacer(Modifier.height(88.dp)) } // FAB clearance
            }
        }
    }
}

@Composable
private fun GroupHeader(name: String, count: Int) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$count",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TaskItem(
    task: Task,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!task.isChecked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom circle checkbox
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(
                    width = 1.5.dp,
                    color = if (task.isChecked)
                        MaterialTheme.colorScheme.onBackground
                    else
                        MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
                .background(
                    if (task.isChecked) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.background
                )
                .clickable { onToggle(!task.isChecked) },
            contentAlignment = Alignment.Center
        ) {
            if (task.isChecked) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                task.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (task.isChecked) TextDecoration.LineThrough else null
                ),
                color = if (task.isChecked)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (task.dueDate != null || task.sourceName.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        task.sourceName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    task.dueDate?.let { due ->
                        Text(
                            "· $due",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No tasks yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Add a Notion page or database to get started.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
