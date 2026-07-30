package com.katalon.katalianbanking.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katalon.katalianbanking.data.LoginResult
import com.katalon.katalianbanking.ui.components.*
import com.katalon.katalianbanking.ui.theme.Emerald500
import com.katalon.katalianbanking.ui.theme.Slate500
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    onLogin: (String, String) -> LoginResult,
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(loading) {
        if (loading) {
            delay(1000) // mirrors setTimeout(..., 1000) in LoginScreen.tsx
            val result = onLogin(username, password)
            loading = false
            when (result) {
                LoginResult.Invalid -> error = "Authentication failed. Check Secure ID and Code."
                LoginResult.Locked -> error = "Account locked for security reasons."
                LoginResult.Success -> onLoginSuccess()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("KATALIAN", fontSize = 34.sp, fontWeight = FontWeight.Black, color = Color.White, fontStyle = FontStyle.Italic)
            Spacer(Modifier.height(8.dp))
            Text("Private Banking & Asset Management", color = Slate500, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(32.dp))

            SectionCard(modifier = Modifier.fillMaxWidth()) {
                if (error.isNotEmpty()) {
                    ErrorBanner(error)
                    Spacer(Modifier.height(20.dp))
                }
                KInput(label = "Secure ID", value = username, onValueChange = { username = it; error = "" }, placeholder = "USER_0000")
                Spacer(Modifier.height(16.dp))
                KInput(label = "Access Code", value = password, onValueChange = { password = it; error = "" }, placeholder = "••••••••", isPassword = true)
                Spacer(Modifier.height(24.dp))
                KButton(
                    text = if (loading) "Authorizing..." else "Enter Vault Access",
                    onClick = { if (!loading) { error = ""; loading = true } },
                    fullWidth = true,
                    enabled = !loading
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "LOST ACCESS CREDENTIALS?",
                    color = Slate500,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clickableText { onForgotPassword() }
                )
            }
            Spacer(Modifier.height(32.dp))
            Text("PROTECTED BY AES-256", color = Slate500.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        }
    }
}

/** Small helper to make a Text composable clickable without pulling in a full Modifier chain at call sites. */
private fun Modifier.clickableText(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
