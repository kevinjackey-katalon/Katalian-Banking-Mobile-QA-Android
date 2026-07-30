package com.katalon.katalianbanking.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katalon.katalianbanking.data.Account
import com.katalon.katalianbanking.data.AccountStatus
import com.katalon.katalianbanking.data.AccountType
import com.katalon.katalianbanking.data.User
import com.katalon.katalianbanking.ui.components.KButton
import com.katalon.katalianbanking.ui.components.KButtonVariant
import com.katalon.katalianbanking.ui.components.PillBadge
import com.katalon.katalianbanking.ui.components.SectionCard
import com.katalon.katalianbanking.ui.theme.*

private fun accountIcon(type: AccountType): String = when (type) {
    AccountType.Checking -> "\uD83D\uDCB3"
    AccountType.Savings -> "\uD83D\uDCB0"
    AccountType.CreditCard -> "\uD83D\uDCB3"
    AccountType.PlatinumCreditCard -> "\uD83D\uDC8E"
}

@Composable
fun DashboardScreen(
    user: User,
    onOpenAccount: (String) -> Unit,
    onTransfer: () -> Unit,
    onDeposit: () -> Unit,
    onDocumentLibrary: () -> Unit,
    onLoans: () -> Unit,
    onFreezeAll: () -> Unit,
    onContact: () -> Unit,
    onApply: (AccountType) -> Unit
) {
    val totalBalance = user.accounts.sumOf { it.balance }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text("NET LIQUIDITY", color = Slate500, fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "$" + "%,.2f".format(totalBalance),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 40.sp
                )
                Spacer(Modifier.height(12.dp))
                Row {
                    PillBadge("Active Assets", Emerald500)
                    Spacer(Modifier.width(8.dp))
                    PillBadge("Member since 2021", Slate500)
                }
                Spacer(Modifier.height(20.dp))
                Row {
                    KButton(text = "Move Funds", onClick = onTransfer, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(12.dp))
                    KButton(text = "Deposit", onClick = onDeposit, variant = KButtonVariant.Secondary, modifier = Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(28.dp))
            Text("ACCOUNTS & CARDS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))
        }

        items(user.accounts) { acc ->
            AccountCard(acc, onClick = { if (acc.status != AccountStatus.Frozen) onOpenAccount(acc.id) })
            Spacer(Modifier.height(12.dp))
        }

        item {
            Spacer(Modifier.height(20.dp))
            Text("APPLY FOR NEW PRODUCTS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))

            val options = buildList {
                add(Triple("Checking", AccountType.Checking, "\uD83D\uDCB3"))
                add(Triple("Savings", AccountType.Savings, "\uD83D\uDCB0"))
                add(Triple("Credit", AccountType.CreditCard, "\uD83D\uDCB3"))
                if (user.canApplyForPlatinum) add(Triple("Platinum", AccountType.PlatinumCreditCard, "\uD83D\uDC8E"))
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                options.forEach { (label, type, icon) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .background(Slate900, RoundedCornerShape(20.dp))
                            .clickable { onApply(type) }
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(icon, fontSize = 26.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(label.uppercase(), color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            Text("ASSET MANAGEMENT", color = Slate500, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(12.dp))

            QuickLink("Document Library", "\uD83D\uDCDA", onDocumentLibrary)
            QuickLink("Request Lending", "\uD83C\uDFDB\uFE0F", onLoans)
            QuickLink("Freeze All Cards", "\u2744\uFE0F", onFreezeAll)
            QuickLink("Fraud Reporting / Help Center", "\uD83D\uDEE1\uFE0F", onContact)

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AccountCard(acc: Account, onClick: () -> Unit) {
    val isFrozen = acc.status == AccountStatus.Frozen
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isFrozen) Slate950 else Slate900, RoundedCornerShape(24.dp))
            .clickable(enabled = !isFrozen, onClick = onClick)
            .padding(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(accountIcon(acc.type), fontSize = 28.sp)
            if (isFrozen) PillBadge("Frozen", Red500) else Text(acc.accountNumber, color = Slate500, fontSize = 11.sp)
        }
        Spacer(Modifier.height(20.dp))
        Text(acc.type.label.uppercase(), color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Text("$" + "%,.2f".format(acc.balance), color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
    }
}

@Composable
private fun QuickLink(label: String, icon: String, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WhiteFaint, RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 18.sp)
                Spacer(Modifier.width(12.dp))
                Text(label, color = Slate400, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
