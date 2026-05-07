package com.notiontasks.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.*
import androidx.glance.appwidget.*
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.*
import androidx.glance.material3.ColorProviders
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.notiontasks.data.api.NotionTasksDatabase
import com.notiontasks.data.model.Task
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import androidx.datastore.preferences.core.*
import com.notiontasks.di.WidgetEntryPoint

// ─── Widget Receiver ──────────────────────────────────────────────────────────

class TasksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = TasksWidget()
}

// ─── Widget ───────────────────────────────────────────────────────────────────

class TasksWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Load tasks from database
        val tasks = loadTasks(context)
        provideContent {
            WidgetContent(tasks = tasks, context = context)
        }
    }

    private suspend fun loadTasks(context: Context): List<Task> = withContext(Dispatchers.IO) {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            val db = entryPoint.database()
            db.taskDao().getPendingTasks().first()
                .take(8) // show max 8 in widget
                .map { entity ->
                    Task(
                        id = entity.id,
                        title = entity.title,
                        isChecked = entity.isChecked,
                        sourceId = entity.sourceId,
                        sourceName = entity.sourceName,
                        sourceType = com.notiontasks.data.model.SourceType.valueOf(entity.sourceType),
                        blockId = entity.blockId,
                        propertyName = entity.propertyName,
                        createdTime = entity.createdTime,
                        lastEditedTime = entity.lastEditedTime,
                        dueDate = entity.dueDate
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

// ─── Widget UI (Glance Composable) ────────────────────────────────────────────

@Composable
private fun WidgetContent(tasks: List<Task>, context: Context) {
    GlanceTheme {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(16.dp)
                .cornerRadius(20.dp)
                .appWidgetBackground()
        ) {
            // Header row: app icon + add button
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App icon / logo
                Box(
                    modifier = GlanceModifier
                        .size(48.dp)
                        .background(Color.Black)
                        .cornerRadius(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "N",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(GlanceModifier.defaultWeight())

                // Add / open app button
                Box(
                    modifier = GlanceModifier
                        .size(48.dp)
                        .background(Color(0xFFF2F2F7))
                        .cornerRadius(14.dp)
                        .clickable(actionStartActivity<com.notiontasks.MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "+",
                            style = TextStyle(
                                color = ColorProvider(Color.Black),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            "INBOX",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF8E8E93)),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Spacer(GlanceModifier.height(16.dp))

            // "Today" label
            Text(
                "Today",
                style = TextStyle(
                    color = ColorProvider(Color.Black),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(GlanceModifier.height(8.dp))

            // Task list
            if (tasks.isEmpty()) {
                Text(
                    "No pending tasks",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF8E8E93)),
                        fontSize = 13.sp
                    )
                )
            } else {
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    tasks.forEach { task ->
                        WidgetTaskRow(task)
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetTaskRow(task: Task) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable(
                actionStartActivity<com.notiontasks.MainActivity>()
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circle checkbox (static in widget — tapping opens app)
        Box(
            modifier = GlanceModifier
                .size(18.dp)
                .background(Color.Transparent)
                .cornerRadius(9.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outlined circle effect via nested boxes
            Box(
                modifier = GlanceModifier
                    .size(18.dp)
                    .background(if (task.isChecked) Color.Black else Color.Transparent)
                    .cornerRadius(9.dp)
            ) {}
            if (!task.isChecked) {
                Box(
                    modifier = GlanceModifier
                        .size(16.dp)
                        .background(Color(0xFFF2F2F7))
                        .cornerRadius(8.dp)
                ) {}
            }
        }

        Spacer(GlanceModifier.width(10.dp))

        Text(
            task.title,
            style = TextStyle(
                color = ColorProvider(if (task.isChecked) Color(0xFF8E8E93) else Color.Black),
                fontSize = 13.sp,
                textDecoration = if (task.isChecked) TextDecoration.LineThrough else null
            ),
            maxLines = 1
        )
    }
}
