package com.katalon.katalianbanking.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.katalon.katalianbanking.data.Account
import com.katalon.katalianbanking.data.Transaction
import com.katalon.katalianbanking.data.TransactionType
import com.katalon.katalianbanking.ui.components.KButton
import com.katalon.katalianbanking.ui.components.KButtonVariant
import com.katalon.katalianbanking.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

private fun monthLabel(isoDate: String): String {
    return try {
        val instant = Instant.parse(isoDate)
        val zdt = instant.atZone(ZoneId.systemDefault())
        "${zdt.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${zdt.year}"
    } catch (e: Exception) {
        "Unknown"
    }
}

/** Mirrors AccountDetailsScreen.tsx's jsPDF-generated statement export, using Android's PdfDocument API. */
private fun generateStatementPdf(context: Context, account: Account, monthFilter: String, transactions: List<Transaction>): File {
    val doc = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72dpi-ish
    var page = doc.startPage(pageInfo)
    var canvas: Canvas = page.canvas

    val titlePaint = Paint().apply { color = AColor.rgb(16, 185, 129); textSize = 22f; isFakeBoldText = true }
    val subPaint = Paint().apply { color = AColor.GRAY; textSize = 10f }
    val labelPaint = Paint().apply { color = AColor.BLACK; textSize = 11f; isFakeBoldText = true }
    val bodyPaint = Paint().apply { color = AColor.BLACK; textSize = 10f }
    val creditPaint = Paint().apply { color = AColor.rgb(16, 185, 129); textSize = 10f }
    val linePaint = Paint().apply { color = AColor.rgb(230, 230, 230) }

    canvas.drawText("KATALIAN BANK", 20f, 35f, titlePaint)
    canvas.drawText("PRIVATE WEALTH MANAGEMENT FACILITY", 20f, 48f, subPaint)
    canvas.drawLine(20f, 55f, 575f, 55f, linePaint)

    canvas.drawText("${account.type.label} Statement", 20f, 70f, labelPaint)
    canvas.drawText("Account Number: ${account.accountNumber}", 20f, 85f, bodyPaint)
    canvas.drawText("Period: ${if (monthFilter == "All") "Complete History" else monthFilter}", 20f, 98f, bodyPaint)
    canvas.drawText("Available Balance: $" + "%,.2f".format(account.balance), 400f, 85f, bodyPaint)
    canvas.drawText("Date of Issue: ${SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date())}", 400f, 98f, bodyPaint)

    var y = 125f
    canvas.drawText("DATE", 25f, y, labelPaint)
    canvas.drawText("DESCRIPTION", 110f, y, labelPaint)
    canvas.drawText("CATEGORY", 320f, y, labelPaint)
    canvas.drawText("AMOUNT", 500f, y, labelPaint)
    y += 15f

    for (tx in transactions) {
        if (y > 800f) {
            doc.finishPage(page)
            page = doc.startPage(pageInfo)
            canvas = page.canvas
            y = 40f
        }
        val dateStr = try {
            val zdt = Instant.parse(tx.date).atZone(ZoneId.systemDefault())
            "${zdt.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${zdt.dayOfMonth}, ${zdt.year}"
        } catch (e: Exception) { tx.date }

        canvas.drawText(dateStr, 25f, y, bodyPaint)
        val desc = if (tx.description.length > 28) tx.description.take(25) + "..." else tx.description
        canvas.drawText(desc.uppercase(), 110f, y, bodyPaint)
        canvas.drawText(tx.category.uppercase(), 320f, y, bodyPaint)
        val amountStr = (if (tx.type == TransactionType.Credit) "+$" else "-$") + "%,.2f".format(tx.amount)
        canvas.drawText(amountStr, 500f, y, if (tx.type == TransactionType.Credit) creditPaint else bodyPaint)
        y += 14f
        canvas.drawLine(20f, y - 4f, 575f, y - 4f, linePaint)
    }

    doc.finishPage(page)

    val statementsDir = File(context.cacheDir, "statements").apply { mkdirs() }
    val fileName = "Katalian_Statement_${account.type.label.replace(" ", "_")}_${monthFilter.replace(" ", "_")}.pdf"
    val file = File(statementsDir, fileName)
    FileOutputStream(file).use { doc.writeTo(it) }
    doc.close()
    return file
}

@Composable
fun AccountDetailsScreen(account: Account) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedMonth by remember { mutableStateOf("All") }
    var downloading by remember { mutableStateOf(false) }

    val months = remember(account) { listOf("All") + account.transactions.map { monthLabel(it.date) }.distinct() }
    val filtered = remember(account, selectedMonth) {
        val sorted = account.transactions.sortedByDescending { it.date }
        if (selectedMonth == "All") sorted else sorted.filter { monthLabel(it.date) == selectedMonth }
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(account.type.label.uppercase() + " LEDGER", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                Text(account.accountNumber + " \u2022 SECURE FACILITY", color = Slate500, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900, RoundedCornerShape(24.dp))
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("AVAILABLE CAPITAL", color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Text("$" + "%,.2f".format(account.balance), color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp)
            }
            KButton(
                text = if (downloading) "Provisioning..." else "Download Statement",
                onClick = {
                    if (!downloading) {
                        downloading = true
                        scope.launch {
                            delay(1200) // mirrors the 1800ms simulated provisioning delay in the web app
                            try {
                                val file = generateStatementPdf(context, account, selectedMonth, filtered)
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Statement saved: ${file.name}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "A technical error occurred while provisioning your statement.", Toast.LENGTH_LONG).show()
                            }
                            downloading = false
                        }
                    }
                },
                variant = KButtonVariant.Secondary,
                enabled = !downloading
            )
        }
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            months.forEach { m ->
                val selected = m == selectedMonth
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .background(if (selected) Emerald500 else WhiteFaint, RoundedCornerShape(50))
                        .clickable { selectedMonth = m }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(m.uppercase(), color = if (selected) Slate950 else Slate400, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp)) {
                Text("No ledger entries detected for this period.", color = Slate500, fontWeight = FontWeight.Bold)
            }
        } else {
            LazyColumn {
                items(filtered) { tx -> TransactionRow(tx) }
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate900.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(tx.description.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
            Text(tx.category.uppercase(), color = Slate500, fontSize = 10.sp)
        }
        Text(
            (if (tx.type == TransactionType.Credit) "+$" else "-$") + "%,.2f".format(tx.amount),
            color = if (tx.type == TransactionType.Credit) Emerald500 else Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp
        )
    }
}
