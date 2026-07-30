package com.katalon.katalianbanking.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katalon.katalianbanking.data.LOAN_PRODUCTS
import com.katalon.katalianbanking.data.LoanProduct
import com.katalon.katalianbanking.data.LoanType
import com.katalon.katalianbanking.ui.components.KButton
import com.katalon.katalianbanking.ui.components.KButtonVariant
import com.katalon.katalianbanking.ui.components.SectionCard
import com.katalon.katalianbanking.ui.theme.Emerald500
import com.katalon.katalianbanking.ui.theme.Slate400

@Composable
fun LoansScreen(onApplyLoan: (LoanType) -> Unit, onContact: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        item {
            Text("Private Credit Solutions", color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text(
                "Sophisticated lending for your primary residence, luxury vehicles, or personal capital requirements.",
                color = Slate400, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
        }
        items(LOAN_PRODUCTS) { product: LoanProduct ->
            SectionCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Text(product.icon, fontSize = 40.sp)
                Spacer(Modifier.height(12.dp))
                Text(product.type.label, color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                Text(product.description, color = Slate400, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))
                Text("RATES FROM", color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Black)
                Text(product.rate + " APR", color = Emerald500, fontWeight = FontWeight.Black, fontSize = 24.sp)
                Spacer(Modifier.height(16.dp))
                KButton(text = "Apply for Funding", onClick = { onApplyLoan(product.type) }, fullWidth = true)
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text("Need a custom lending solution?", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text(
                    "Contact our wealth management team for commercial facilities or high-limit liquidity lines.",
                    color = Slate400, fontSize = 13.sp
                )
                Spacer(Modifier.height(16.dp))
                KButton(text = "Contact Asset Division", onClick = onContact, variant = KButtonVariant.Secondary)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
