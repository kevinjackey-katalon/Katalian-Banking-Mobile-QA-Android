package com.katalon.katalianbanking.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katalon.katalianbanking.data.SecurityAction
import com.katalon.katalianbanking.data.User
import com.katalon.katalianbanking.ui.components.*
import com.katalon.katalianbanking.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SecurityScreen(user: User, action: SecurityAction, onCancel: () -> Unit, onComplete: (SecurityAction) -> Unit) {
    var step by remember { mutableStateOf(1) }
    var loading by remember { mutableStateOf(false) }
    var selectedAssetId by remember { mutableStateOf(user.accounts.firstOrNull()?.id ?: "") }
    var incidentDescription by remember { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val accentColor = when (action) {
        SecurityAction.Lockdown -> Red600
        SecurityAction.FreezeAll -> Cyan500
        SecurityAction.Report -> Red500
    }

    fun confirm() {
        loading = true
        scope.launch {
            delay(2000) // mirrors the 2500ms simulated freeze/lockdown provisioning delay
            loading = false
            step = 3
        }
    }

    LaunchedEffect(step, action) {
        if (action == SecurityAction.Lockdown && step == 3) {
            delay(2000)
            onComplete(action)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("SECURITY PROTOCOL", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
        Text(
            if (loading) "ESTABLISHING BLOCK" else if (step == 3) "OPERATION COMPLETE" else when (action) {
                SecurityAction.Lockdown -> "CRITICAL ACTION NEEDED"
                SecurityAction.FreezeAll -> "CRYO-FREEZE PROTOCOL"
                SecurityAction.Report -> "INCIDENT MANAGEMENT"
            },
            color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(20.dp))

        SectionCard(modifier = Modifier.fillMaxWidth()) {
            if (loading) {
                Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        KSpinner()
                        Spacer(Modifier.height(16.dp))
                        Text(
                            when (action) {
                                SecurityAction.Lockdown -> "Terminating All Sessions"
                                SecurityAction.FreezeAll -> "Deep-Freezing Card Facilities"
                                SecurityAction.Report -> "Provisioning Asset Block"
                            },
                            color = accentColor, fontWeight = FontWeight.Black, fontSize = 14.sp
                        )
                    }
                }
            } else when (action) {
                SecurityAction.Report -> when (step) {
                    1 -> {
                        Text("Asset Compromise Report", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text("Identify the specific facility that has been compromised.", color = Slate500, fontSize = 13.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("AFFECTED FACILITY", color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(8.dp))
                        val selected = user.accounts.find { it.id == selectedAssetId }
                        Box {
                            Row(
                                modifier = Modifier.fillMaxWidth().background(Slate950, RoundedCornerShape(16.dp))
                                    .clickable { menuOpen = true }.padding(16.dp)
                            ) { Text(selected?.let { "${it.type.label} (Ending ${it.accountNumber.takeLast(4)})" } ?: "Select", color = Color.White, fontSize = 13.sp) }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                user.accounts.forEach { acc ->
                                    DropdownMenuItem(text = { Text("${acc.type.label} (Ending ${acc.accountNumber.takeLast(4)})") }, onClick = { selectedAssetId = acc.id; menuOpen = false })
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        KInput("Incident Narrative", incidentDescription, { incidentDescription = it }, placeholder = "Briefly describe the compromise...")
                        Spacer(Modifier.height(20.dp))
                        Row {
                            KButton(text = "Cancel", onClick = onCancel, variant = KButtonVariant.Secondary, modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(12.dp))
                            KButton(text = "Authorize Asset Freeze", onClick = { step = 2 }, enabled = incidentDescription.isNotBlank(), variant = KButtonVariant.Danger, modifier = Modifier.weight(1f))
                        }
                    }
                    2 -> {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("\uD83D\uDD12", fontSize = 32.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("Confirm Asset Freeze", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Text(
                                    "You are about to freeze ${user.accounts.find { it.id == selectedAssetId }?.type?.label}. This will block all authorizations immediately.",
                                    color = Slate400, fontSize = 13.sp
                                )
                                Spacer(Modifier.height(20.dp))
                                KButton(text = "Execute Freeze Protocol", onClick = { confirm() }, variant = KButtonVariant.Danger, fullWidth = true)
                                Spacer(Modifier.height(8.dp))
                                KButton(text = "Back to Selection", onClick = { step = 1 }, variant = KButtonVariant.Ghost, fullWidth = true)
                            }
                        }
                    }
                    3 -> SuccessBlock(
                        icon = "\uD83D\uDD12", title = "Asset Frozen",
                        message = "Technical block applied. Our fraud prevention squad will contact you within 15 minutes.",
                        onDone = { onComplete(action) }
                    )
                }
                SecurityAction.FreezeAll -> when (step) {
                    1 -> {
                        val affected = user.accounts.filter { it.type.isCard || it.type == com.katalon.katalianbanking.data.AccountType.Checking }
                        Text("\u2744\uFE0F Cryo-Freeze Cards", color = Cyan500, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text(
                            "This will temporarily suspend all active cards and digital payment facilities. External ACH and Savings transfers will remain functional.",
                            color = Slate400, fontSize = 13.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("AFFECTED FACILITIES: ${affected.joinToString { it.type.label }}", color = Slate500, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                KButton(text = "Authorize Cryo-Freeze", onClick = { confirm() }, fullWidth = true)
                                Spacer(Modifier.height(8.dp))
                                KButton(text = "Cancel Protocol", onClick = onCancel, variant = KButtonVariant.Ghost, fullWidth = true)
                            }
                        }
                    }
                    3 -> SuccessBlock(
                        icon = "\u2744\uFE0F", title = "Facilities Suspended",
                        message = "All identified cards have been moved to deep-freeze status.",
                        onDone = { onComplete(action) }
                    )
                }
                SecurityAction.Lockdown -> when (step) {
                    1 -> {
                        Text("\u2622\uFE0F Nuclear Lockdown", color = Red500, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text(
                            "This procedure will terminate all active sessions and freeze ALL financial facilities tied to this identity.\nTHIS ACTION IS IRREVERSIBLE VIA MOBILE.",
                            color = Slate400, fontSize = 13.sp
                        )
                        Spacer(Modifier.height(20.dp))
                        KButton(text = "Initiate Global Lockdown", onClick = { step = 2 }, variant = KButtonVariant.Danger, fullWidth = true)
                        Spacer(Modifier.height(8.dp))
                        KButton(text = "Abort Procedure", onClick = onCancel, variant = KButtonVariant.Ghost, fullWidth = true)
                    }
                    2 -> {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("\u26A0\uFE0F", fontSize = 32.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("Final Warning", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Text("Global ledger freeze will commence upon confirmation.", color = Slate500, fontSize = 13.sp)
                                Spacer(Modifier.height(20.dp))
                                KButton(text = "CONFIRM GLOBAL FREEZE", onClick = { confirm() }, variant = KButtonVariant.Danger, fullWidth = true)
                                Spacer(Modifier.height(8.dp))
                                KButton(text = "Back to Safety", onClick = { step = 1 }, variant = KButtonVariant.Secondary, fullWidth = true)
                            }
                        }
                    }
                    3 -> {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("\uD83D\uDD12", fontSize = 40.sp)
                                Spacer(Modifier.height(16.dp))
                                Text("System Locked", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
                                Text("All digital facilities have been severed. You will be logged out shortly.", color = Slate400, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessBlock(icon: String, title: String, message: String, onDone: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 40.sp)
            Spacer(Modifier.height(16.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))
            Text(message, color = Slate400, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            KButton(text = "Back to Portfolio", onClick = onDone)
        }
    }
}
