package com.katalon.katalianbanking.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katalon.katalianbanking.ui.components.*
import com.katalon.katalianbanking.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class ChatMsg(val fromAi: Boolean, val text: String)

@Composable
fun ContactScreen(onReportStolen: () -> Unit, onLockdown: () -> Unit) {
    val messages = remember {
        mutableStateListOf(ChatMsg(true, "Welcome to Katalian Support. I am your personal concierge assistant. How may I facilitate your request today?"))
    }
    var input by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Global Support", color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp)
        Text(
            "Our concierge team is available around the clock to assist with your private wealth requirements.",
            color = Slate400, fontSize = 14.sp
        )
        Spacer(Modifier.height(20.dp))

        Row { ContactCard("\uD83D\uDCDE", "Private Line", "1-800-KATALIAN", Modifier.weight(1f)); Spacer(Modifier.width(12.dp)); ContactCard("\uD83D\uDCE7", "Secure Email", "wealth@katalian.com", Modifier.weight(1f)) }
        Spacer(Modifier.height(12.dp))
        Row { ContactCard("\uD83C\uDFE2", "Global HQ", "1200 Financial Plaza", Modifier.weight(1f)); Spacer(Modifier.width(12.dp)); ContactCard("\uD83D\uDD52", "Market Hours", "9AM-5PM EST", Modifier.weight(1f)) }
        Spacer(Modifier.height(20.dp))

        SectionCard(modifier = Modifier.fillMaxWidth()) {
            Text("\u26A0\uFE0F Security Incident", color = Red500, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text("Immediate actions for compromised accounts or stolen assets.", color = Slate500, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            KButton(text = "Report Stolen Asset", onClick = onReportStolen, variant = KButtonVariant.Danger, fullWidth = true)
            Spacer(Modifier.height(8.dp))
            KButton(text = "Account Lockdown", onClick = onLockdown, variant = KButtonVariant.Danger, fullWidth = true)
        }
        Spacer(Modifier.height(20.dp))

        Text("WEALTH CONCIERGE", color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Slate900, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(messages) { m ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = if (m.fromAi) Alignment.CenterStart else Alignment.CenterEnd
                    ) {
                        Text(
                            m.text,
                            color = if (m.fromAi) Slate400 else Slate950,
                            modifier = Modifier
                                .background(if (m.fromAi) WhiteFaint else Emerald500, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            fontSize = 13.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                KInput(label = "", value = input, onValueChange = { input = it }, placeholder = "Message Concierge...", modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                KButton(text = "Send", onClick = {
                    if (input.isNotBlank()) {
                        val userMsg = input
                        messages.add(ChatMsg(false, userMsg))
                        input = ""
                        scope.launch {
                            delay(1000)
                            val lower = userMsg.lowercase()
                            val response = when {
                                "card" in lower -> "Understood. For immediate card security, please use the Emergency Freeze options or call 1-800-KATALIAN."
                                "loan" in lower -> "Our lending products are currently offering competitive rates. I can initiate a consultation request for you."
                                else -> "I have noted your inquiry. A representative from our Private Banking division will be assigned to your case momentarily."
                            }
                            messages.add(ChatMsg(true, response))
                        }
                    }
                })
            }
        }
    }
}

@Composable
private fun ContactCard(icon: String, title: String, info: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Slate900, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text(icon, fontSize = 22.sp)
        Spacer(Modifier.height(8.dp))
        Text(title.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
        Text(info, color = Emerald500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
