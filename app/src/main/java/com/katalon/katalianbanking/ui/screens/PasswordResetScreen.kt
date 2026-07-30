package com.katalon.katalianbanking.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katalon.katalianbanking.ui.components.KButton
import com.katalon.katalianbanking.ui.components.SectionCard
import com.katalon.katalianbanking.ui.theme.Slate400

@Composable
fun PasswordResetScreen(onBackToLogin: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        SectionCard {
            Text("Password Reset", color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "If an account with that email exists, we have sent password reset instructions. (This is a simulated feature).",
                color = Slate400,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            KButton(text = "Back to Login", onClick = onBackToLogin, fullWidth = true)
        }
    }
}
