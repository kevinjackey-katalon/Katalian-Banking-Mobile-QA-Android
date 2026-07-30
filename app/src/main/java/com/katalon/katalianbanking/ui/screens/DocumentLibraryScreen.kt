package com.katalon.katalianbanking.ui.screens

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.katalon.katalianbanking.ui.components.KButton
import com.katalon.katalianbanking.ui.components.KButtonVariant
import com.katalon.katalianbanking.ui.components.SectionCard
import com.katalon.katalianbanking.ui.theme.Slate400
import java.io.File
import java.io.FileOutputStream

/** Mirrors buildLoanRequestFormPdf() in DocumentLibraryScreen.tsx using Android's PdfDocument API. */
private fun buildLoanRequestFormPdf(): PdfDocument {
    val doc = PdfDocument()
    val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
    val canvas: Canvas = page.canvas

    val headerBg = Paint().apply { color = AColor.rgb(16, 24, 39) }
    val titlePaint = Paint().apply { color = AColor.WHITE; textSize = 20f; isFakeBoldText = true }
    val subPaint = Paint().apply { color = AColor.WHITE; textSize = 10f }
    val sectionPaint = Paint().apply { color = AColor.rgb(17, 24, 39); textSize = 13f; isFakeBoldText = true }
    val fieldLabelPaint = Paint().apply { color = AColor.rgb(62, 70, 84); textSize = 10f }
    val linePaint = Paint().apply { color = AColor.rgb(170, 177, 190) }
    val footerPaint = Paint().apply { color = AColor.rgb(75, 85, 99); textSize = 9f }

    canvas.drawRect(0f, 0f, 595f, 92f, headerBg)
    canvas.drawText("Loan Request Form", 48f, 48f, titlePaint)
    canvas.drawText("Katalian Banking - Generic Loan Application", 48f, 68f, subPaint)

    fun field(label: String, y: Float, x: Float = 48f, width: Float = 230f) {
        canvas.drawText(label, x, y, fieldLabelPaint)
        canvas.drawLine(x, y + 14, x + width, y + 14, linePaint)
    }

    canvas.drawText("Applicant Information", 48f, 132f, sectionPaint)
    field("Full Name", 154f, 48f, 240f)
    field("Date of Birth (MM/DD/YYYY)", 154f, 320f, 220f)
    field("Phone Number", 192f, 48f, 180f)
    field("Email Address", 192f, 248f, 280f)
    field("Street Address", 230f, 48f, 420f)
    field("City", 268f, 48f, 180f)
    field("State", 268f, 248f, 90f)
    field("ZIP Code", 268f, 360f, 110f)

    canvas.drawText("Loan Details", 48f, 320f, sectionPaint)
    field("Loan Type (Personal / Auto / Mortgage / Other)", 342f, 48f, 300f)
    field("Requested Amount (USD)", 380f, 48f, 190f)
    field("Requested Term (Months)", 380f, 270f, 190f)
    field("Purpose of Loan", 418f, 48f, 500f)

    canvas.drawText("Employment & Income", 48f, 470f, sectionPaint)
    field("Employer Name", 492f, 48f, 240f)
    field("Job Title", 492f, 320f, 240f)
    field("Annual Gross Income (USD)", 530f, 48f, 230f)

    canvas.drawText("Declarations", 48f, 580f, sectionPaint)
    canvas.drawText("[ ] I certify all information provided is accurate and complete.", 48f, 602f, subPaint.apply { color = AColor.BLACK })
    canvas.drawText("[ ] I authorize Katalian Banking to verify credit and employment records.", 48f, 620f, subPaint)

    canvas.drawText("Electronic Signature", 48f, 664f, sectionPaint)
    field("Borrower Electronic Signature (type full legal name)", 686f, 48f, 320f)
    field("Borrower Signature Date", 686f, 392f, 190f)
    field("Co-Borrower Electronic Signature (if applicable)", 724f, 48f, 320f)
    field("Co-Borrower Signature Date", 724f, 392f, 190f)

    canvas.drawText("System metadata: Signature IP, timestamp, and consent hash are recorded upon submission.", 48f, 778f, footerPaint)

    doc.finishPage(page)
    return doc
}

@Composable
fun DocumentLibraryScreen() {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            Text("ASSET MANAGEMENT", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Text("Document Library", color = Color.White, fontWeight = FontWeight.Black, fontSize = 28.sp)
            Text(
                "Secure document repository for standardized client forms. The Loan Request Form below includes generic lending fields and electronic signature sections.",
                color = Slate400, fontSize = 13.sp
            )
            Spacer(Modifier.height(20.dp))
            KButton(text = "Download Loan Request Form (PDF)", onClick = {
                try {
                    val doc = buildLoanRequestFormPdf()
                    val dir = File(context.cacheDir, "statements").apply { mkdirs() }
                    val file = File(dir, "Loan_Request_Form.pdf")
                    FileOutputStream(file).use { doc.writeTo(it) }
                    doc.close()
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Saved: ${file.name}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to generate document.", Toast.LENGTH_LONG).show()
                }
            }, variant = KButtonVariant.Secondary, fullWidth = true)
        }
    }
}
