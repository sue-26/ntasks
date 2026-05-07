package com.notiontasks.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    onConnectNotion: (String) -> Unit
) {
    var token by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(96.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.onBackground
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "N",
                    color = MaterialTheme.colorScheme.background,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "NotionTasks",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Your Notion checklists, everywhere.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(48.dp))

        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("Notion Integration Token") },
            placeholder = { Text("secret_...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Paste your Internal Integration token from notion.so/my-integrations",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { if (token.isNotBlank()) onConnectNotion(token.trim()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = token.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            Text(
                "Connect Notion",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.background
            )
        }
    }
}
