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
import com.katalon.katalianbanking.data.AccountType
import com.katalon.katalianbanking.data.ApplicationData
import com.katalon.katalianbanking.data.STATES
import com.katalon.katalianbanking.data.User
import com.katalon.katalianbanking.ui.components.*
import com.katalon.katalianbanking.ui.theme.Emerald500
import com.katalon.katalianbanking.ui.theme.Slate500
import com.katalon.katalianbanking.ui.theme.Slate950
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ApplicationScreen(user: User, accountType: AccountType, onCancel: () -> Unit, onSubmit: suspend (AccountType, ApplicationData) -> Unit) {
    val isDepositAccount = accountType == AccountType.Checking || accountType == AccountType.Savings
    val maxSteps = if (isDepositAccount) 3 else 2

    var step by remember { mutableStateOf(1) }
    var loading by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    val formData = remember { ApplicationData(state = STATES.first()) }
    var refreshTick by remember { mutableStateOf(0) }
    var errors by remember { mutableStateOf(mapOf<String, String>()) }
    var stateMenuOpen by remember { mutableStateOf(false) }
    var fundingMenuOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun validate(currentStep: Int): Boolean {
        val newErrors = mutableMapOf<String, String>()
        if (currentStep == 1) {
            if (formData.firstName.isBlank()) newErrors["firstName"] = "Legal first name required."
            if (formData.lastName.isBlank()) newErrors["lastName"] = "Legal last name required."
            if (formData.dob.isBlank()) newErrors["dob"] = "Date of birth required."
        } else if (currentStep == 2) {
            if (formData.address.isBlank()) newErrors["address"] = "Primary residence required."
            if (formData.city.isBlank()) newErrors["city"] = "City required."
            if (!Regex("^\\d{5}$").matches(formData.zip)) newErrors["zip"] = "Valid 5-digit ZIP code required."
        }
        errors = newErrors
        return newErrors.isEmpty()
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("KATALIAN PRODUCTS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
        Text("PROVISIONING ${accountType.label.uppercase()}", color = Emerald500, fontSize = 11.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(20.dp))

        SectionCard(modifier = Modifier.fillMaxWidth()) {
            when {
                loading -> {
                    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            KSpinner()
                            Spacer(Modifier.height(16.dp))
                            Text("Processing Credentials", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text("Running regulatory background checks...", color = Slate500, fontSize = 12.sp)
                        }
                    }
                }
                success -> {
                    Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("\u2705", fontSize = 40.sp)
                            Spacer(Modifier.height(16.dp))
                            Text("Facility Approved", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Your ${accountType.label} has been successfully provisioned and is now available in your portfolio.",
                                color = Slate500, fontSize = 13.sp
                            )
                            Spacer(Modifier.height(20.dp))
                            KButton(text = "Enter Facility Dashboard", onClick = {
                                scope.launch { onSubmit(accountType, formData); onCancel() }
                            })
                        }
                    }
                }
                step == 1 -> {
                    Text("Identity Verification", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("Provide your legal credentials as they appear on official documents.", color = Slate500, fontSize = 13.sp)
                    Spacer(Modifier.height(16.dp))
                    KInput("Legal First Name", formData.firstName, { formData.firstName = it; refreshTick++ }, error = errors["firstName"])
                    Spacer(Modifier.height(12.dp))
                    KInput("Middle Name", formData.middleName, { formData.middleName = it; refreshTick++ })
                    Spacer(Modifier.height(12.dp))
                    KInput("Legal Last Name", formData.lastName, { formData.lastName = it; refreshTick++ }, error = errors["lastName"])
                    Spacer(Modifier.height(12.dp))
                    KInput("Date of Birth (YYYY-MM-DD)", formData.dob, { formData.dob = it; refreshTick++ }, error = errors["dob"])
                }
                step == 2 -> {
                    Text("Residence Information", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("Your primary physical address for regulatory compliance.", color = Slate500, fontSize = 13.sp)
                    Spacer(Modifier.height(16.dp))
                    KInput("Primary Address Line", formData.address, { formData.address = it; refreshTick++ }, error = errors["address"])
                    Spacer(Modifier.height(12.dp))
                    KInput("City", formData.city, { formData.city = it; refreshTick++ }, error = errors["city"])
                    Spacer(Modifier.height(12.dp))
                    Text("STATE", color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate950, RoundedCornerShape(16.dp))
                                .clickable { stateMenuOpen = true }
                                .padding(16.dp)
                        ) { Text(formData.state, color = Color.White, fontSize = 13.sp) }
                        DropdownMenu(expanded = stateMenuOpen, onDismissRequest = { stateMenuOpen = false }) {
                            STATES.forEach { s ->
                                DropdownMenuItem(text = { Text(s) }, onClick = { formData.state = s; refreshTick++; stateMenuOpen = false })
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    KInput("Zip Code", formData.zip, { formData.zip = it; refreshTick++ }, error = errors["zip"], isNumber = true)
                }
                step == 3 -> {
                    Text("Initial Asset Allocation", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("Set your starting liquidity for this new facility.", color = Slate500, fontSize = 13.sp)
                    Spacer(Modifier.height(16.dp))
                    KInput(
                        "Funding Amount ($)",
                        formData.initialDeposit?.toString() ?: "",
                        { formData.initialDeposit = it.toDoubleOrNull(); refreshTick++ },
                        isNumber = true
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("TRANSFER FROM EXISTING ACCOUNT", color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    val eligibleAccounts = user.accounts.filter { it.type == AccountType.Checking || it.type == AccountType.Savings }
                    val selectedFunding = eligibleAccounts.find { it.id == formData.depositFromAccountId }
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate950, RoundedCornerShape(16.dp))
                                .clickable { fundingMenuOpen = true }
                                .padding(16.dp)
                        ) {
                            Text(selectedFunding?.let { "${it.type.label} (Ending ${it.accountNumber.takeLast(4)})" } ?: "External Wire / New Funds", color = Color.White, fontSize = 13.sp)
                        }
                        DropdownMenu(expanded = fundingMenuOpen, onDismissRequest = { fundingMenuOpen = false }) {
                            DropdownMenuItem(text = { Text("External Wire / New Funds") }, onClick = { formData.depositFromAccountId = null; refreshTick++; fundingMenuOpen = false })
                            eligibleAccounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text("${acc.type.label} (Ending ${acc.accountNumber.takeLast(4)})") },
                                    onClick = { formData.depositFromAccountId = acc.id; refreshTick++; fundingMenuOpen = false }
                                )
                            }
                        }
                    }
                }
            }

            if (!loading && !success) {
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    if (step > 1) KButton(text = "Back", onClick = { errors = emptyMap(); step -= 1 }, variant = KButtonVariant.Secondary) else Spacer(Modifier.width(1.dp))
                    if (step < maxSteps) {
                        KButton(text = "Continue", onClick = { if (validate(step)) step += 1 })
                    } else {
                        KButton(text = "Authorize Provisioning", onClick = {
                            if (validate(step)) {
                                loading = true
                                scope.launch {
                                    delay(2000)
                                    loading = false
                                    success = true
                                }
                            }
                        })
                    }
                }
            }
        }
    }
}
