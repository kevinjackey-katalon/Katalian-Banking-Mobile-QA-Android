package com.katalon.katalianbanking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katalon.katalianbanking.data.User
import com.katalon.katalianbanking.ui.theme.*

enum class KButtonVariant { Primary, Secondary, Danger, Ghost }

/** Mirrors components/common/Button.tsx */
@Composable
fun KButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: KButtonVariant = KButtonVariant.Primary,
    enabled: Boolean = true,
    fullWidth: Boolean = false
) {
    val colors = when (variant) {
        KButtonVariant.Primary -> ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Slate950)
        KButtonVariant.Secondary -> ButtonDefaults.buttonColors(containerColor = WhiteFaint, contentColor = Color.White)
        KButtonVariant.Danger -> ButtonDefaults.buttonColors(containerColor = Red600, contentColor = Color.White)
        KButtonVariant.Ghost -> ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Slate400)
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        shape = RoundedCornerShape(50),
        modifier = (if (fullWidth) modifier.fillMaxWidth() else modifier)
    ) {
        Text(text, fontWeight = FontWeight.Black, fontSize = 14.sp)
    }
}

/** Mirrors components/common/Input.tsx */
@Composable
fun KInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    isNumber: Boolean = false,
    error: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = Slate500,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Slate500) },
            singleLine = true,
            visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = if (isNumber) androidx.compose.ui.text.input.KeyboardType.Number else androidx.compose.ui.text.input.KeyboardType.Text
            ),
            isError = error != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Slate950,
                unfocusedContainerColor = Slate950,
                focusedBorderColor = Emerald500,
                unfocusedBorderColor = WhiteFaint,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        )
        if (error != null) {
            Text(error, color = Red500, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
        }
    }
}

/** Mirrors components/common/Spinner.tsx */
@Composable
fun KSpinner(modifier: Modifier = Modifier) {
    CircularProgressIndicator(color = Emerald500, modifier = modifier.size(48.dp), strokeWidth = 4.dp)
}

/** Mirrors components/common/Header.tsx - top bar with back/close affordance + session info. */
@Composable
fun KTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    user: User? = null,
    onLogout: (() -> Unit)? = null
) {
    Surface(color = Slate950) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
                Column {
                    Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    if (subtitle != null) {
                        Text(subtitle.uppercase(), color = Emerald500, fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 1.5.sp)
                    }
                }
            }
            if (user != null && onLogout != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("@${user.username}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("PREMIER MEMBER", color = Emerald500, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    KButton(text = "Sign Out", onClick = onLogout, variant = KButtonVariant.Secondary)
                }
            }
        }
    }
}

@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Red500.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .border(1.dp, Red500.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            message.uppercase(),
            color = Red500,
            fontWeight = FontWeight.Black,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .background(Slate900, RoundedCornerShape(32.dp))
            .border(1.dp, WhiteFaint, RoundedCornerShape(32.dp))
            .padding(24.dp),
        content = content
    )
}

@Composable
fun PillBadge(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(50))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text.uppercase(), color = color, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}
