package com.notiontasks.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.notiontasks.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewSettingsSheet(
    current: TaskViewSettings,
    onDismiss: () -> Unit,
    onSave: (TaskViewSettings) -> Unit
) {
    var showCompleted by remember { mutableStateOf(current.showCompleted) }
    var showOverdue by remember { mutableStateOf(current.showOverdue) }
    var sortField by remember { mutableStateOf(current.sortField) }
    var sortOrder by remember { mutableStateOf(current.sortOrder) }
    var groupBy by remember { mutableStateOf(current.groupBy) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Text("View", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = {
                    onSave(TaskViewSettings(showCompleted, showOverdue, emptySet(), sortField, sortOrder, groupBy))
                    onDismiss()
                }) { Text("Done", style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary)) }
            }

            Spacer(Modifier.height(16.dp))
            SectionLabel("Shown in list")

            ToggleRow(
                icon = Icons.Outlined.AccessTime,
                label = "Overdue tasks",
                checked = showOverdue,
                onChecked = { showOverdue = it }
            )
            ToggleRow(
                icon = Icons.Outlined.Cancel,
                label = "Cancelled tasks",
                checked = false,
                onChecked = {},
                enabled = false
            )
            ToggleRow(
                icon = Icons.Outlined.CheckCircle,
                label = "Completed tasks",
                checked = showCompleted,
                onChecked = { showCompleted = it }
            )

            Spacer(Modifier.height(16.dp))
            SectionLabel("Sort")

            // Sort field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Sort, null, modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Text("Sort", style = MaterialTheme.typography.bodyLarge)
                }
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.menuAnchor()
                    ) {
                        Text(sortField.label)
                        Icon(Icons.Outlined.UnfoldMore, null, modifier = Modifier.size(16.dp))
                    }
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        SortField.entries.forEach { field ->
                            DropdownMenuItem(
                                text = { Text(field.label) },
                                onClick = { sortField = field; expanded = false }
                            )
                        }
                    }
                }
            }

            // Sort order
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ArrowDownward, null, modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Text("Order", style = MaterialTheme.typography.bodyLarge)
                }
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedButton(onClick = {}, modifier = Modifier.menuAnchor()) {
                        Text(sortOrder.label)
                        Icon(Icons.Outlined.UnfoldMore, null, modifier = Modifier.size(16.dp))
                    }
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        SortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = { Text(order.label) },
                                onClick = { sortOrder = order; expanded = false }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionLabel("Group")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.TableRows, null, modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Text("Group", style = MaterialTheme.typography.bodyLarge)
                }
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedButton(onClick = {}, modifier = Modifier.menuAnchor()) {
                        Text(groupBy.label)
                        Icon(Icons.Outlined.UnfoldMore, null, modifier = Modifier.size(16.dp))
                    }
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        GroupBy.entries.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g.label) },
                                onClick = { groupBy = g; expanded = false }
                            )
                        }
                    }
                }
            }

            // Group order
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ArrowDownward, null, modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Text("Order", style = MaterialTheme.typography.bodyLarge)
                }
                OutlinedButton(onClick = {}) {
                    Text("Ascending")
                    Icon(Icons.Outlined.UnfoldMore, null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun ToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.background,
                checkedTrackColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}
