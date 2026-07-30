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
import com.katalon.katalianbanking.data.User
import com.katalon.katalianbanking.ui.components.*
import com.katalon.katalianbanking.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class DepositMethod { ACH, Check }

@Composable
fun DepositScreen(user: User, onNavigateBack: () -> Unit, onDeposit: suspend (String, Double) -> Unit) {
    var step by remember { mutableStateOf(1) }
    var method by remember { mutableStateOf(DepositMethod.ACH) }
    var toAccountId by remember { mutableStateOf(user.accounts.firstOrNull()?.id ?: "") }
    var amount by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var checkFrontCaptured by remember { mutableStateOf(false) }
    var checkBackCaptured by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val selectedAccount = user.accounts.find { it.id == toAccountId }
    val maxSteps = 3

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("DEPOSIT FACILITY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
        Text(
            if (loading) "AUTHORIZING REQUEST" else if (step == 4) "TRANSACTION COMPLETE" else "STEP $step OF $maxSteps",
            color = Emerald500, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp
        )
        Spacer(Modifier.height(20.dp))

        SectionCard(modifier = Modifier.fillMaxWidth()) {
            when {
                loading -> {
                    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            KSpinner()
                            Spacer(Modifier.height(16.dp))
                            Text(
                                if (method == DepositMethod.Check) "PROCESSING IMAGE DATA" else "VALIDATING LIQUIDITY SOURCE",
                                color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp
                            )
                        }
                    }
                }
                step == 1 -> {
                    Text("Deposit Configuration", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("Define your funding source and capital amount.", color = Slate500, fontSize = 13.sp)
                    Spacer(Modifier.height(20.dp))
                    Row {
                        DepositMethodOption("Electronic Transfer", "\uD83C\uDFDB\uFE0F", method == DepositMethod.ACH, Modifier.weight(1f)) { method = DepositMethod.ACH }
                        Spacer(Modifier.width(12.dp))
                        DepositMethodOption("Check Deposit", "\uD83D\uDCC4", method == DepositMethod.Check, Modifier.weight(1f)) { method = DepositMethod.Check }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("DESTINATION FACILITY", color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate950, RoundedCornerShape(16.dp))
                                .clickable { menuOpen = true }
                                .padding(16.dp)
                        ) {
                            Text(
                                selectedAccount?.let { "${it.type.label} - ${it.accountNumber} ($${"%,.2f".format(it.balance)})" } ?: "Select account",
                                color = Color.White, fontSize = 13.sp
                            )
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            user.accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text("${acc.type.label} - ${acc.accountNumber}") },
                                    onClick = { toAccountId = acc.id; menuOpen = false }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    KInput(label = "Provision Amount ($)", value = amount, onValueChange = { amount = it }, placeholder = "0.00", isNumber = true)
                }
                step == 2 && method == DepositMethod.ACH -> {
                    Text("Funding Source", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("Verify the linked external account for this transaction.", color = Slate500, fontSize = 13.sp)
                    Spacer(Modifier.height(20.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Emerald500.copy(alpha = 0.06f), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        LabelValueRow("Source Entity", "EXTERNAL PARTNER BANK")
                        LabelValueRow("Account ID", "********5542")
                        LabelValueRow("Availability", "Immediate Provisioning")
                    }
                }
                step == 2 && method == DepositMethod.Check -> {
                    Text("Mobile Check Capture", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("Upload high-resolution images of the check instrument.", color = Slate500, fontSize = 13.sp)
                    Spacer(Modifier.height(20.dp))
                    Row {
                        CheckCaptureBox("Check Front", checkFrontCaptured, Modifier.weight(1f)) { checkFrontCaptured = true }
                        Spacer(Modifier.width(12.dp))
                        CheckCaptureBox("Check Back", checkBackCaptured, Modifier.weight(1f)) { checkBackCaptured = true }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Ensure the back of the check is endorsed with \"For Mobile Deposit at Katalian Bank Only\".",
                        color = Slate500, fontSize = 11.sp
                    )
                }
                step == 3 -> {
                    Text("Final Authorization", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("Review asset allocation before ledger commitment.", color = Slate500, fontSize = 13.sp)
                    Spacer(Modifier.height(20.dp))
                    Text("LEDGER AMOUNT", color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text("$" + "%,.2f".format(amount.toDoubleOrNull() ?: 0.0), color = Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp)
                    Spacer(Modifier.height(16.dp))
                    LabelValueRow("Target Facility", "${selectedAccount?.type?.label} (..${selectedAccount?.accountNumber?.takeLast(4)})")
                    LabelValueRow("Submission Method", if (method == DepositMethod.ACH) "Priority ACH" else "Remote Image Capture")
                }
                step == 4 -> {
                    Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("\u2705", fontSize = 40.sp)
                            Spacer(Modifier.height(16.dp))
                            Text("Deposit Confirmed", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (method == DepositMethod.Check)
                                    "Your check image has been queued for clearing."
                                else "Funds have been successfully provisioned to your ${selectedAccount?.type?.label} account.",
                                color = Slate400, fontSize = 13.sp
                            )
                            Spacer(Modifier.height(20.dp))
                            KButton(text = "Return to Portfolio", onClick = onNavigateBack)
                        }
                    }
                }
            }

            if (!loading && step in 1..3) {
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    if (step > 1) {
                        KButton(text = "Back", onClick = { step -= 1 }, variant = KButtonVariant.Secondary)
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }
                    val canContinue = amount.toDoubleOrNull()?.let { it > 0 } == true &&
                            !(step == 2 && method == DepositMethod.Check && !(checkFrontCaptured && checkBackCaptured))
                    if (step < 3) {
                        KButton(text = "Continue", enabled = canContinue, onClick = { step += 1 })
                    } else {
                        KButton(text = "Authorize Deposit", onClick = {
                            loading = true
                            scope.launch {
                                delay(1500)
                                onDeposit(toAccountId, amount.toDouble())
                                loading = false
                                step = 4
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun DepositMethodOption(label: String, icon: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(if (selected) Emerald500.copy(alpha = 0.1f) else Slate950, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))
            Text(label.uppercase(), color = if (selected) Emerald400 else Slate500, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun CheckCaptureBox(label: String, captured: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier = modifier) {
        Text(label.uppercase(), color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(if (captured) Emerald500.copy(alpha = 0.08f) else Slate950, RoundedCornerShape(20.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(if (captured) "\u2705 Captured" else "\uD83D\uDCF8 Capture $label", color = Slate500, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun LabelValueRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label.uppercase(), color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
