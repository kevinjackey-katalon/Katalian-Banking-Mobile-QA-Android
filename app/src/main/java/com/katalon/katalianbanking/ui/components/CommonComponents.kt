package com.katalon.katalianbanking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.password
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katalon.katalianbanking.data.User
import com.katalon.katalianbanking.ui.theme.*

enum class KButtonVariant { Primary, Secondary, Danger, Ghost }

/**
 * Mirrors components/common/Button.tsx
 *
 * [testTag] is required on purpose: it is the automation contract (exposed as resource-id)
 * and must never be derived from the visible text, which can change or be translated.
 */
@Composable
fun KButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: KButtonVariant = KButtonVariant.Primary,
    enabled: Boolean = true,
    fullWidth: Boolean = false,
    testTag: String
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
        modifier = (if (fullWidth) modifier.fillMaxWidth() else modifier).testTag(testTag)
    ) {
        Text(text, fontWeight = FontWeight.Black, fontSize = 14.sp)
    }
}

/**
 * Semantic type of a [KInput]. Each type maps to the Android-standard keyboard
 * type, capitalization, autocorrect setting and input filter for that kind of data.
 */
enum class KInputType {
    /** Free-form single-line text (sentence capitalization, autocorrect on). */
    Text,
    /** Person names: word capitalization, no autocorrect. */
    Name,
    /** Login identifiers: ASCII keyboard, no capitalization, no autocorrect. */
    Username,
    /** Passwords: password keyboard (no suggestions/learning), masked, show/hide toggle. */
    Password,
    /** Email addresses: email keyboard, no autocorrect. */
    Email,
    /** Postal addresses and cities: word capitalization, no autocorrect. */
    Address,
    /** Currency amounts: decimal keyboard, digits with up to 2 decimal places. */
    Decimal,
    /** Digit-only values such as ZIP codes: number keyboard, digits only. */
    Digits,
    /** Dates typed as YYYY-MM-DD: digits and dashes only, max 10 characters. */
    Date,
    /** Multi-line free text such as incident descriptions. */
    Multiline,
    /** Single-line chat/message composer (sentence capitalization, autocorrect on). */
    Message
}

/** Converts a label like "Access Code" into a stable test tag like "access_code". */
fun String.toTestTag(): String =
    lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')

private val DECIMAL_PATTERN = Regex("^\\d*(\\.\\d{0,2})?$")

internal fun KInputType.accepts(candidate: String, maxLength: Int?): Boolean {
    val limit = maxLength ?: if (this == KInputType.Date) 10 else null
    if (limit != null && candidate.length > limit) return false
    return when (this) {
        KInputType.Decimal -> DECIMAL_PATTERN.matches(candidate)
        KInputType.Digits -> candidate.all { it.isDigit() }
        KInputType.Date -> candidate.all { it.isDigit() || it == '-' }
        else -> true
    }
}

private fun KInputType.keyboardOptions(imeAction: ImeAction): KeyboardOptions = when (this) {
    KInputType.Password -> KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Password,
        imeAction = imeAction
    )
    KInputType.Username -> KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Ascii,
        imeAction = imeAction
    )
    KInputType.Email -> KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Email,
        imeAction = imeAction
    )
    KInputType.Name, KInputType.Address -> KeyboardOptions(
        capitalization = KeyboardCapitalization.Words,
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Text,
        imeAction = imeAction
    )
    KInputType.Decimal -> KeyboardOptions(
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Decimal,
        imeAction = imeAction
    )
    KInputType.Digits -> KeyboardOptions(
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Number,
        imeAction = imeAction
    )
    KInputType.Date -> KeyboardOptions(
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Text,
        imeAction = imeAction
    )
    KInputType.Text, KInputType.Multiline, KInputType.Message -> KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        autoCorrectEnabled = true,
        keyboardType = KeyboardType.Text,
        imeAction = imeAction
    )
}

/**
 * Mirrors components/common/Input.tsx, built to Android/Material text-field conventions:
 *  - keyboard type, capitalization and autocorrect chosen per [type];
 *  - IME action (Next by default, Done/Send/Search on request) with an optional handler;
 *  - errors shown as Material supporting text and exposed to accessibility;
 *  - the visible label is announced as the field's accessible name;
 *  - a stable [testTag] (exposed as resource-id) on the field and on the password toggle.
 */
@Composable
fun KInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    type: KInputType = KInputType.Text,
    error: String? = null,
    testTag: String,
    accessibilityLabel: String = label,
    maxLength: Int? = null,
    imeAction: ImeAction = if (type == KInputType.Multiline) ImeAction.Default else ImeAction.Next,
    onImeAction: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val isPassword = type == KInputType.Password
    val errorMessage = error
    val isMultiline = type == KInputType.Multiline
    var isRevealed by rememberSaveable { mutableStateOf(false) }
    val maskText = isPassword && !isRevealed

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = Slate500,
                letterSpacing = 1.5.sp,
                // The field itself carries this label as its accessible name, so the
                // visual caption is hidden from accessibility to avoid double announcements.
                modifier = Modifier.clearAndSetSemantics { }
            )
            Spacer(Modifier.height(8.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = { candidate -> if (type.accepts(candidate, maxLength)) onValueChange(candidate) },
            enabled = enabled,
            placeholder = { Text(placeholder, color = Slate500) },
            singleLine = !isMultiline,
            minLines = if (isMultiline) 3 else 1,
            maxLines = if (isMultiline) 6 else 1,
            visualTransformation = if (maskText) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = type.keyboardOptions(imeAction),
            keyboardActions = onImeAction?.let { action ->
                KeyboardActions(
                    onDone = { action() },
                    onGo = { action() },
                    onSearch = { action() },
                    onSend = { action() }
                )
            } ?: KeyboardActions.Default,
            trailingIcon = if (isPassword) {
                {
                    IconButton(
                        onClick = { isRevealed = !isRevealed },
                        modifier = Modifier
                            .testTag("${testTag}_visibility_toggle")
                            .semantics { stateDescription = if (isRevealed) "Password visible" else "Password hidden" }
                    ) {
                        Icon(
                            imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isRevealed) "Hide password" else "Show password",
                            tint = Slate500
                        )
                    }
                }
            } else null,
            isError = error != null,
            supportingText = error?.let { message ->
                { Text(message, color = Red500, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Slate950,
                unfocusedContainerColor = Slate950,
                focusedBorderColor = Emerald500,
                unfocusedBorderColor = WhiteFaint,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
                .semantics {
                    if (accessibilityLabel.isNotEmpty()) contentDescription = accessibilityLabel
                    if (maskText) password()
                    if (errorMessage != null) this.error(errorMessage)
                }
        )
    }
}

/**
 * Makes a custom "select" row behave like a standard Android dropdown: dropdown role,
 * an accessible click label, expanded/collapsed state, and a stable test tag.
 */
fun Modifier.kDropdownTrigger(
    testTag: String,
    label: String,
    expanded: Boolean,
    onClick: () -> Unit
): Modifier = this
    .testTag(testTag)
    .semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" }
    .clickable(role = Role.DropdownList, onClickLabel = label, onClick = onClick)

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                    KButton(text = "Sign Out", onClick = onLogout, variant = KButtonVariant.Secondary, testTag = "sign_out")
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
