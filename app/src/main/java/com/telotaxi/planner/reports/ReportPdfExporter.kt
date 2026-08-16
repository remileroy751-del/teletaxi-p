package com.telotaxi.planner.reports

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.provider.MediaStore
import com.telotaxi.planner.data.Ride
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Génère un rapport PDF de chiffre d'affaires à partir d'une liste de courses terminées,
 * en utilisant uniquement l'API Android native (aucune librairie externe nécessaire).
 * Le PDF est enregistré dans le dossier "Téléchargements" du téléphone.
 */
object ReportPdfExporter {

    fun export(
        context: Context,
        rides: List<Ride>,
        periodLabel: String
    ): String? {
        val pageWidth = 595 // A4 largeur en points (72dpi)
        val pageHeight = 842 // A4 hauteur en points

        val document = PdfDocument()
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = Paint().apply { textSize = 20f; isFakeBoldText = true }
        val subtitlePaint = Paint().apply { textSize = 12f; color = 0xFF5C6672.toInt() }
        val headerPaint = Paint().apply { textSize = 12f; isFakeBoldText = true }
        val bodyPaint = Paint().apply { textSize = 11f }
        val totalPaint = Paint().apply { textSize = 16f; isFakeBoldText = true; color = 0xFF0F4C81.toInt() }

        var y = 50f
        val marginLeft = 40f
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)

        canvas.drawText("TéléTaxi Planner — Rapport de chiffre d'affaires", marginLeft, y, titlePaint)
        y += 22f
        canvas.drawText(periodLabel, marginLeft, y, subtitlePaint)
        y += 30f

        val completedRides = rides
        val total = completedRides.sumOf { it.price ?: 0.0 }

        canvas.drawText("Nombre de courses terminées : ${completedRides.size}", marginLeft, y, bodyPaint)
        y += 25f
        canvas.drawText("Chiffre d'affaires total : ${"%.2f".format(total)} FCFA", marginLeft, y, totalPaint)
        y += 35f

        // En-têtes de colonnes
        canvas.drawText("Date", marginLeft, y, headerPaint)
        canvas.drawText("Client", marginLeft + 130, y, headerPaint)
        canvas.drawText("Trajet", marginLeft + 280, y, headerPaint)
        canvas.drawText("Prix", marginLeft + 460, y, headerPaint)
        y += 8f
        canvas.drawLine(marginLeft, y, pageWidth - marginLeft, y, subtitlePaint)
        y += 16f

        for (ride in completedRides.sortedBy { it.dateTimeMillis }) {
            if (y > pageHeight - 60f) {
                // Nouvelle page si la page actuelle est pleine
                document.finishPage(page)
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.pages.size + 1).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }
            val trajet = listOfNotNull(
                ride.pickupAddress.takeIf { it.isNotBlank() },
                ride.destinationAddress.takeIf { it.isNotBlank() }
            ).joinToString(" → ").take(30)

            canvas.drawText(dateFormat.format(ride.dateTimeMillis), marginLeft, y, bodyPaint)
            canvas.drawText(ride.clientName.take(18), marginLeft + 130, y, bodyPaint)
            canvas.drawText(trajet, marginLeft + 280, y, bodyPaint)
            canvas.drawText(ride.price?.let { "%.0f".format(it) } ?: "-", marginLeft + 460, y, bodyPaint)
            y += 18f
        }

        document.finishPage(page)

        val fileName = "rapport_teletaxi_${System.currentTimeMillis()}.pdf"
        val resultPath = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val itemUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
                resolver.openOutputStream(itemUri)?.use { out -> document.writeTo(out) }
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)
                itemUri.toString()
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(downloadsDir, fileName)
                java.io.FileOutputStream(file).use { out -> document.writeTo(out) }
                file.absolutePath
            }
        } catch (e: Exception) {
            null
        } finally {
            document.close()
        }

        return resultPath
    }
}
