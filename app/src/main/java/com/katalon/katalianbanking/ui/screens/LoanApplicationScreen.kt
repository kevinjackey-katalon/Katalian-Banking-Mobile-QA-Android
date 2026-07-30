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
import com.katalon.katalianbanking.data.LoanApplicationData
import com.katalon.katalianbanking.data.LoanType
import com.katalon.katalianbanking.ui.components.*
import com.katalon.katalianbanking.ui.theme.Slate500
import com.katalon.katalianbanking.ui.theme.Slate950
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoanApplicationScreen(loanType: LoanType, onCancel: () -> Unit, onSubmit: suspend (LoanType, LoanApplicationData) -> Unit) {
    var step by remember { mutableStateOf(1) }
    var loading by remember { mutableStateOf(false) }
    val formData = remember { LoanApplicationData(loanTerm = 12) }
    var refreshTick by remember { mutableStateOf(0) } // forces recomposition on mutable data class edits
    var termMenuOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("${loanType.label.uppercase()} FACILITY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
        Text(if (loading) "PROCESSING" else "STEP $step OF 3", color = com.katalon.katalianbanking.ui.theme.Emerald500, fontSize = 11.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(20.dp))

        SectionCard(modifier = Modifier.fillMaxWidth()) {
            if (loading) {
                Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        KSpinner()
                        Spacer(Modifier.height(16.dp))
                        Text("Running Risk Profile", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            } else {
                when (step) {
                    1 -> {
                        Text("Personal Verification", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text("Verify your identity for the lending institution.", color = Slate500, fontSize = 13.sp)
                        Spacer(Modifier.height(16.dp))
                        KInput("First Name", formData.firstName, { formData.firstName = it; refreshTick++ })
                        Spacer(Modifier.height(12.dp))
                        KInput("Last Name", formData.lastName, { formData.lastName = it; refreshTick++ })
                        Spacer(Modifier.height(12.dp))
                        KInput("Date of Birth (YYYY-MM-DD)", formData.dob, { formData.dob = it; refreshTick++ })
                        Spacer(Modifier.height(12.dp))
                        KInput("Primary Residence", formData.address, { formData.address = it; refreshTick++ })
                    }
                    2 -> {
                        Text("Capital & Employment", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text("Verify your income sources for risk assessment.", color = Slate500, fontSize = 13.sp)
                        Spacer(Modifier.height(16.dp))
                        KInput("Current Employer", formData.employer, { formData.employer = it; refreshTick++ })
                        Spacer(Modifier.height(12.dp))
                        KInput("Job Title", formData.jobTitle, { formData.jobTitle = it; refreshTick++ })
                        Spacer(Modifier.height(12.dp))
                        KInput("Annual Income ($)", if (formData.annualIncome == 0.0) "" else formData.annualIncome.toString(),
                            { formData.annualIncome = it.toDoubleOrNull() ?: 0.0; refreshTick++ }, isNumber = true)
                    }
                    3 -> {
                        Text("Facility Requirements", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text("Define repayment and utilization parameters.", color = Slate500, fontSize = 13.sp)
                        Spacer(Modifier.height(16.dp))
                        KInput("Required Amount ($)", if (formData.loanAmount == 0.0) "" else formData.loanAmount.toString(),
                            { formData.loanAmount = it.toDoubleOrNull() ?: 0.0; refreshTick++ }, isNumber = true)
                        Spacer(Modifier.height(12.dp))
                        Text("PROPOSED TERM", color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(8.dp))
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Slate950, RoundedCornerShape(16.dp))
                                    .clickable { termMenuOpen = true }
                                    .padding(16.dp)
                            ) {
                                Text("${formData.loanTerm} Months", color = Color.White, fontSize = 13.sp)
                            }
                            DropdownMenu(expanded = termMenuOpen, onDismissRequest = { termMenuOpen = false }) {
                                listOf(12, 24, 36).forEach { term ->
                                    DropdownMenuItem(text = { Text("$term Months") }, onClick = { formData.loanTerm = term; refreshTick++; termMenuOpen = false })
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        KInput("Purpose", formData.purpose, { formData.purpose = it; refreshTick++ })
                    }
                }

                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    if (step > 1) KButton(text = "Back", onClick = { step -= 1 }, variant = KButtonVariant.Secondary) else Spacer(Modifier.width(1.dp))
                    if (step < 3) {
                        KButton(text = "Continue", onClick = { step += 1 })
                    } else {
                        KButton(text = "Submit Application", onClick = {
                            loading = true
                            scope.launch {
                                delay(1500)
                                onSubmit(loanType, formData)
                                loading = false
                                onCancel()
                            }
                        })
                    }
                }
            }
        }
    }
}
