package com.katalon.katalianbanking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katalon.katalianbanking.data.GeminiAssistant
import com.katalon.katalianbanking.data.User
import com.katalon.katalianbanking.ui.theme.Cyan500
import com.katalon.katalianbanking.ui.theme.Slate900
import kotlinx.coroutines.launch

/**
 * Mirrors components/common/AiAssistant.tsx: a floating "Ask AI Assistant" button
 * that expands into a chat panel querying the (optionally Gemini-backed) financial
 * intelligence engine over the full user/account dataset.
 */
@Composable
fun AiAssistantWidget(allUsers: List<User>) {
    var isOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var response by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isOpen) {
            ExtendedFloatingActionButton(
                onClick = { isOpen = true },
                containerColor = Cyan500,
                contentColor = Color.White,
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Ask AI Assistant", fontWeight = FontWeight.Bold)
            }
        } else {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .widthIn(max = 380.dp)
                    .heightIn(max = 520.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Cyan500)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Katalian Financial AI", color = Color.White, fontWeight = FontWeight.Black)
                        IconButton(onClick = { isOpen = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        when {
                            isLoading -> {
                                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                    KSpinner()
                                }
                            }
                            response != null -> {
                                Text(response!!, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                            }
                            else -> {
                                Text(
                                    "Ask me anything about user accounts, total balances, or financial trends in the current system.",
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("Search accounts or ask a question...") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (query.isNotBlank() && !isLoading) {
                                    val q = query
                                    isLoading = true
                                    response = null
                                    scope.launch {
                                        response = GeminiAssistant.ask(q, allUsers)
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = query.isNotBlank() && !isLoading
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Cyan500)
                        }
                    }
                }
            }
        }
    }
}
