package com.notiontasks.ui.widget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.notiontasks.ui.theme.NotionTasksTheme

/**
 * Widget configuration activity — shown when adding the widget to the home screen.
 * Currently uses defaults; can be extended for per-widget filter/sort settings.
 */
class WidgetConfigActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        setContent {
            NotionTasksTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("NotionTasks Widget", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Shows your pending tasks from all connected Notion sources.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(32.dp))
                        Button(
                            onClick = {
                                // Set result and finish
                                val resultValue = android.content.Intent().apply {
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                }
                                setResult(RESULT_OK, resultValue)
                                finish()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Widget")
                        }
                    }
                }
            }
        }
    }
}
