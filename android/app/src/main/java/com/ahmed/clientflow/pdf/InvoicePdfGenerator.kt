package com.ahmed.clientflow.pdf

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.ahmed.clientflow.data.Client
import com.ahmed.clientflow.data.FreelancerInfo
import com.ahmed.clientflow.data.Invoice
import com.ahmed.clientflow.data.Payment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InvoicePdfGenerator(private val context: Context) {

    fun generatePdf(
        invoice: Invoice,
        client: Client,
        payment: Payment?,
        freelancerInfo: FreelancerInfo
    ): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val brandColor = parseAccentColor(freelancerInfo.accentColor)
        val darkText = 0xFF1E2430.toInt()
        val mutedText = 0xFF667085.toInt()
        val borderColor = 0xFFE5E7EB.toInt()
        val softFill = 0xFFF8FAFC.toInt()
        val successColor = 0xFF16A34A.toInt()
        val warningColor = 0xFFF59E0B.toInt()
        val dangerColor = 0xFFDC2626.toInt()

        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 28f
            isFakeBoldText = true
            color = brandColor
        }
        val sectionPaint = Paint().apply {
            isAntiAlias = true
            textSize = 11f
            isFakeBoldText = true
            color = mutedText
        }
        val labelPaint = Paint().apply {
            isAntiAlias = true
            textSize = 10f
            color = mutedText
        }
        val bodyPaint = Paint().apply {
            isAntiAlias = true
            textSize = 12f
            color = darkText
        }
        val strongPaint = Paint().apply {
            isAntiAlias = true
            textSize = 12f
            isFakeBoldText = true
            color = darkText
        }
        val amountPaint = Paint().apply {
            isAntiAlias = true
            textSize = 15f
            isFakeBoldText = true
            color = brandColor
            textAlign = Paint.Align.RIGHT
        }
        val footerPaint = Paint().apply {
            isAntiAlias = true
            textSize = 10f
            color = mutedText
            textAlign = Paint.Align.CENTER
        }
        val rightBodyPaint = Paint(bodyPaint).apply { textAlign = Paint.Align.RIGHT }
        val rightStrongPaint = Paint(strongPaint).apply { textAlign = Paint.Align.RIGHT }
        val cardFillPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = softFill
        }
        val borderPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = borderColor
        }
        val headerFillPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = brandColor
        }

        val status = when {
            payment == null -> "PENDING"
            payment.status.toString() == "Paid" -> "PAID"
            payment.status.toString() == "Partial" -> "PARTIAL"
            else -> "UNPAID"
        }
        val statusColor = when (status) {
            "PAID" -> successColor
            "PARTIAL" -> warningColor
            else -> dangerColor
        }
        val statusPaint = Paint().apply {
            isAntiAlias = true
            textSize = 11f
            isFakeBoldText = true
            color = statusColor
        }

        var y = 56f

        drawLogo(canvas, freelancerInfo.logoUri, MARGIN, 36f)

        val titleX = if (freelancerInfo.logoUri.isBlank()) MARGIN else 136f
        canvas.drawText("INVOICE", titleX, y, titlePaint)
        canvas.drawText("Invoice # ${invoice.id.take(8).uppercase()}", PAGE_WIDTH - MARGIN, y - 4f, rightStrongPaint)
        canvas.drawText(formatDate(invoice.createdAt), PAGE_WIDTH - MARGIN, y + 14f, rightBodyPaint)
        y += 28f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, Paint().apply {
            isAntiAlias = true
            color = borderColor
            strokeWidth = 1f
        })
        y += 24f

        val leftCardTop = y
        val cardHeight = 132f
        drawCard(canvas, MARGIN, leftCardTop, 250f, cardHeight, cardFillPaint, borderPaint)
        drawCard(canvas, 305f, leftCardTop, PAGE_WIDTH - MARGIN, cardHeight, cardFillPaint, borderPaint)

        var sellerY = leftCardTop + 22f
        canvas.drawText("FROM", MARGIN + 16f, sellerY, sectionPaint)
        sellerY += 18f
        sellerY = drawWrappedText(
            canvas = canvas,
            text = freelancerInfo.companyName.ifBlank { "Your company name" },
            x = MARGIN + 16f,
            startY = sellerY,
            maxWidth = 218f,
            paint = strongPaint,
            lineHeight = 16f
        )
        sellerY = drawInfoLine(canvas, freelancerInfo.address, MARGIN + 16f, sellerY, 218f, bodyPaint)
        sellerY = drawInfoLine(canvas, freelancerInfo.phone.takeIf { it.isNotBlank() }?.let { "Phone: $it" }, MARGIN + 16f, sellerY, 218f, bodyPaint)
        sellerY = drawInfoLine(canvas, freelancerInfo.email.takeIf { it.isNotBlank() }?.let { "Email: $it" }, MARGIN + 16f, sellerY, 218f, bodyPaint)
        drawInfoLine(canvas, freelancerInfo.ice.takeIf { it.isNotBlank() }?.let { "ICE: $it" }, MARGIN + 16f, sellerY, 218f, bodyPaint)

        var clientY = leftCardTop + 22f
        canvas.drawText("BILL TO", 321f, clientY, sectionPaint)
        clientY += 18f
        clientY = drawWrappedText(
            canvas = canvas,
            text = client.name,
            x = 321f,
            startY = clientY,
            maxWidth = 218f,
            paint = strongPaint,
            lineHeight = 16f
        )
        clientY = drawInfoLine(canvas, client.phone.takeIf { it.isNotBlank() }?.let { "Phone: $it" }, 321f, clientY, 218f, bodyPaint)
        drawInfoLine(canvas, client.serviceType.takeIf { it.isNotBlank() }?.let { "Service: $it" }, 321f, clientY, 218f, bodyPaint)

        y += cardHeight + 26f

        drawCard(canvas, MARGIN, y, PAGE_WIDTH - MARGIN, y + 34f, headerFillPaint, null)
        canvas.drawText("Description", MARGIN + 16f, y + 22f, Paint(strongPaint).apply { color = 0xFFFFFFFF.toInt() })
        canvas.drawText("Amount", PAGE_WIDTH - MARGIN - 16f, y + 22f, Paint(strongPaint).apply {
            color = 0xFFFFFFFF.toInt()
            textAlign = Paint.Align.RIGHT
        })
        y += 34f

        val descriptionLines = wrapText(invoice.description.ifBlank { "Invoice item" }, bodyPaint, 330f)
        val rowHeight = maxOf(54f, 20f + descriptionLines.size * 16f)
        drawCard(canvas, MARGIN, y, PAGE_WIDTH - MARGIN, y + rowHeight, cardFillPaint, borderPaint)

        var descY = y + 22f
        descriptionLines.forEach { line ->
            canvas.drawText(line, MARGIN + 16f, descY, bodyPaint)
            descY += 16f
        }
        canvas.drawText("$${formatAmount(invoice.amount)}", PAGE_WIDTH - MARGIN - 16f, y + 28f, amountPaint)
        y += rowHeight + 24f

        val totalsLeft = 335f
        val totalsRight = PAGE_WIDTH - MARGIN - 16f
        drawCard(canvas, totalsLeft, y, PAGE_WIDTH - MARGIN, y + 96f, cardFillPaint, borderPaint)

        var totalsY = y + 24f
        if (payment != null) {
            canvas.drawText("Total", totalsLeft + 16f, totalsY, bodyPaint)
            canvas.drawText("$${formatAmount(payment.totalAmount)}", totalsRight, totalsY, rightBodyPaint)
            totalsY += 22f

            canvas.drawText("Paid", totalsLeft + 16f, totalsY, bodyPaint)
            canvas.drawText("$${formatAmount(payment.paidAmount)}", totalsRight, totalsY, rightBodyPaint)
            totalsY += 28f

            val balance = payment.totalAmount - payment.paidAmount
            canvas.drawText("Balance due", totalsLeft + 16f, totalsY, strongPaint)
            canvas.drawText("$${formatAmount(balance)}", totalsRight, totalsY, rightStrongPaint)
        } else {
            canvas.drawText("Amount due", totalsLeft + 16f, totalsY + 22f, strongPaint)
            canvas.drawText("$${formatAmount(invoice.amount)}", totalsRight, totalsY + 22f, rightStrongPaint)
        }

        canvas.drawText("Status: $status", MARGIN, y + 24f, statusPaint)
        if (freelancerInfo.invoiceNote.isNotBlank()) {
            val noteTop = y + 56f
            val noteLines = wrapText(freelancerInfo.invoiceNote, bodyPaint, PAGE_WIDTH - MARGIN * 2 - 32f)
            val noteHeight = 28f + noteLines.size * 16f
            drawCard(canvas, MARGIN, noteTop, PAGE_WIDTH - MARGIN, noteTop + noteHeight, cardFillPaint, borderPaint)
            canvas.drawText("Note", MARGIN + 16f, noteTop + 20f, sectionPaint)
            var noteY = noteTop + 40f
            noteLines.forEach { line ->
                canvas.drawText(line, MARGIN + 16f, noteY, bodyPaint)
                noteY += 16f
            }
        }
        canvas.drawText("Thank you for your business.", PAGE_WIDTH / 2f, PAGE_HEIGHT - 44f, footerPaint)
        canvas.drawText("Generated by ClientFlow", PAGE_WIDTH / 2f, PAGE_HEIGHT - 28f, footerPaint)

        pdfDocument.finishPage(page)

        val fileName = "invoice_${invoice.id.take(8)}.pdf"
        val file = File(context.cacheDir, fileName)

        return try {
            FileOutputStream(file).use { output ->
                pdfDocument.writeTo(output)
            }
            pdfDocument.close()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun drawCard(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        fillPaint: Paint,
        strokePaint: Paint?
    ) {
        canvas.drawRoundRect(left, top, right, bottom, 14f, 14f, fillPaint)
        strokePaint?.let { canvas.drawRoundRect(left, top, right, bottom, 14f, 14f, it) }
    }

    private fun drawLogo(canvas: Canvas, logoUri: String, left: Float, top: Float) {
        if (logoUri.isBlank()) return
        val bitmap = runCatching {
            context.contentResolver.openInputStream(Uri.parse(logoUri))?.use { BitmapFactory.decodeStream(it) }
        }.getOrNull() ?: return

        val maxWidth = 72f
        val maxHeight = 72f
        val scale = minOf(maxWidth / bitmap.width, maxHeight / bitmap.height)
        val width = bitmap.width * scale
        val height = bitmap.height * scale
        val dest = RectF(left, top, left + width, top + height)
        canvas.drawBitmap(bitmap, null, dest, null)
    }

    private fun drawInfoLine(
        canvas: Canvas,
        text: String?,
        x: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint
    ): Float {
        if (text.isNullOrBlank()) return startY
        return drawWrappedText(canvas, text, x, startY, maxWidth, paint, 15f)
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
        lineHeight: Float
    ): Float {
        var y = startY
        wrapText(text, paint, maxWidth).forEach { line ->
            canvas.drawText(line, x, y, paint)
            y += lineHeight
        }
        return y
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return listOf("")
        val words = text.trim().split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var current = ""

        for (word in words) {
            val candidate = if (current.isBlank()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current = candidate
            } else {
                if (current.isNotBlank()) lines += current
                current = word
            }
        }

        if (current.isNotBlank()) lines += current
        return lines
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(timestamp))
    }

    private fun formatAmount(amount: Double): String {
        return if (amount == amount.toLong().toDouble()) "%.0f".format(amount) else "%.2f".format(amount)
    }

    private fun parseAccentColor(value: String): Int {
        return runCatching { android.graphics.Color.parseColor(value) }.getOrDefault(0xFF1F4B99.toInt())
    }

    private companion object {
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val MARGIN = 40f
    }
}
