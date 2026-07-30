package com.katalon.katalianbanking.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katalon.katalianbanking.data.Account
import com.katalon.katalianbanking.data.AccountType
import com.katalon.katalianbanking.data.User
import com.katalon.katalianbanking.ui.components.*
import com.katalon.katalianbanking.ui.theme.*
import kotlinx.coroutines.launch

@Composable
private fun AccountDropdown(label: String, options: List<Account>, selectedId: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selected = options.find { it.id == selectedId }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label.uppercase(), color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate950, RoundedCornerShape(16.dp))
                    .clickable { expanded = true }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    selected?.let { "${it.type.label} (${it.accountNumber}) — $${"%,.2f".format(it.balance)}" } ?: "Select facility...",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { acc ->
                    DropdownMenuItem(
                        text = { Text("${acc.type.label} (${acc.accountNumber}) — $${"%,.2f".format(acc.balance)}") },
                        onClick = { onSelect(acc.id); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
fun TransferScreen(user: User, onNavigateBack: () -> Unit, onTransfer: suspend (String, String, Double) -> Unit) {
    val sourceAccounts = remember(user) { user.accounts.filter { it.type == AccountType.Checking || it.type == AccountType.Savings } }
    val recipientAccounts = user.accounts

    var fromAccountId by remember { mutableStateOf(sourceAccounts.firstOrNull()?.id ?: "") }
    var toAccountId by remember { mutableStateOf(recipientAccounts.firstOrNull { it.id != fromAccountId }?.id ?: "") }
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val fromAccount = sourceAccounts.find { it.id == fromAccountId }
    val toAccount = recipientAccounts.find { it.id == toAccountId }
    val isCreditPayment = toAccount?.type?.isCard == true

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text(
            if (isCreditPayment) "CREDIT LIQUIDATION" else "ASSET MOVEMENT",
            color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp
        )
        Text(
            if (isConfirming) "FINAL AUTHORIZATION" else if (isCreditPayment) "PAYMENT FACILITY" else "INTERNAL TRANSFER",
            color = Emerald500, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp
        )
        Spacer(Modifier.height(20.dp))

        SectionCard(modifier = Modifier.fillMaxWidth()) {
            if (isConfirming) {
                Text("AMOUNT TO LIQUIDATE", color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Text("$" + "%,.2f".format(amount.toDoubleOrNull() ?: 0.0), color = Color.White, fontWeight = FontWeight.Black, fontSize = 34.sp)
                Spacer(Modifier.height(20.dp))
                Text("ORIGIN: ${fromAccount?.type?.label} ${fromAccount?.accountNumber}", color = Slate400, fontSize = 13.sp)
                Text("RECIPIENT: ${toAccount?.type?.label} ${toAccount?.accountNumber}", color = Slate400, fontSize = 13.sp)
                Spacer(Modifier.height(24.dp))
                Row {
                    KButton(text = "Back", onClick = { isConfirming = false }, variant = KButtonVariant.Secondary, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(12.dp))
                    KButton(
                        text = if (isSubmitting) "Processing..." else "Authorize ${if (isCreditPayment) "Payment" else "Transfer"}",
                        enabled = !isSubmitting,
                        onClick = {
                            isSubmitting = true
                            scope.launch {
                                onTransfer(fromAccountId, toAccountId, amount.toDouble())
                                isSubmitting = false
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                if (error.isNotEmpty()) {
                    ErrorBanner(error)
                    Spacer(Modifier.height(16.dp))
                }
                AccountDropdown("Origin Liquidity", sourceAccounts, fromAccountId) { newId ->
                    fromAccountId = newId
                    if (newId == toAccountId) {
                        toAccountId = recipientAccounts.firstOrNull { it.id != newId }?.id ?: toAccountId
                    }
                }
                Spacer(Modifier.height(16.dp))
                AccountDropdown(
                    if (isCreditPayment) "Credit Facility Recipient" else "Recipient Facility",
                    recipientAccounts, toAccountId
                ) { toAccountId = it }

                if (isCreditPayment && toAccount != null) {
                    Spacer(Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Emerald500.copy(alpha = 0.06f), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Text("CURRENT INDEBTEDNESS", color = Slate500, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        Text("$" + "%,.2f".format(toAccount.balance), color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Spacer(Modifier.height(8.dp))
                        Row {
                            KButton(text = "Min Payment ($25)", onClick = { amount = "25.00" }, variant = KButtonVariant.Ghost)
                            Spacer(Modifier.width(8.dp))
                            KButton(text = "Pay Full Balance", onClick = { amount = toAccount.balance.toString() }, variant = KButtonVariant.Ghost)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                KInput(label = "Liquid Capital Amount", value = amount, onValueChange = { amount = it }, placeholder = "0.00", isNumber = true)
                Spacer(Modifier.height(24.dp))

                Row {
                    KButton(text = "Cancel", onClick = onNavigateBack, variant = KButtonVariant.Ghost, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(12.dp))
                    KButton(
                        text = "Review Protocol",
                        onClick = {
                            val transferAmount = amount.toDoubleOrNull()
                            error = when {
                                fromAccountId.isEmpty() || toAccountId.isEmpty() -> "Please select both origin and destination facilities."
                                fromAccountId == toAccountId -> "Self-transfer to identical facility is prohibited."
                                transferAmount == null || transferAmount <= 0 -> "Valid capital amount required."
                                fromAccount != null && transferAmount > fromAccount.balance -> "Insufficient liquidity in origin facility."
                                else -> ""
                            }
                            if (error.isEmpty()) isConfirming = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
