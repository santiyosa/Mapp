package com.maintenance.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Environment
import androidx.core.content.FileProvider
import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.model.Record
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Utility class for generating shareable images from Record and Maintenance data.
 */
class ShareImageGenerator(private val context: Context) {

    companion object {
        private const val IMAGE_WIDTH = 1080
        private const val IMAGE_HEIGHT = 1920
        private const val PADDING = 40
        private const val LINE_HEIGHT = 60
        private const val TITLE_SIZE = 48f
        private const val LABEL_SIZE = 32f
        private const val VALUE_SIZE = 28f
        private const val MARGIN_BOTTOM = 30
    }

    fun generateRecordImage(record: Record): File? {
        return try {
            val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            canvas.drawColor(Color.WHITE)
            
            var yPosition = PADDING
            
            val headerPaint = Paint().apply {
                color = Color.parseColor("#2196F3")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, IMAGE_WIDTH.toFloat(), (yPosition + 100).toFloat(), headerPaint)
            
            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = TITLE_SIZE
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            yPosition += 80
            canvas.drawText("REGISTRO", PADDING.toFloat(), yPosition.toFloat(), titlePaint)
            
            yPosition += MARGIN_BOTTOM + 20
            
            val linePaint = Paint().apply {
                color = Color.parseColor("#CCCCCC")
                strokeWidth = 2f
            }
            canvas.drawLine(PADDING.toFloat(), yPosition.toFloat(), (IMAGE_WIDTH - PADDING).toFloat(), yPosition.toFloat(), linePaint)
            
            yPosition += MARGIN_BOTTOM
            
            val labelPaint = Paint().apply {
                color = Color.parseColor("#666666")
                textSize = LABEL_SIZE
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            
            val valuePaint = Paint().apply {
                color = Color.BLACK
                textSize = VALUE_SIZE
            }
            
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            yPosition = drawLabelValue(canvas, "Nombre:", record.name, yPosition, labelPaint, valuePaint)
            yPosition = drawLabelValue(canvas, "Categoría:", record.category ?: "N/A", yPosition, labelPaint, valuePaint)
            yPosition = drawLabelValue(canvas, "Descripción:", record.description ?: "N/A", yPosition, labelPaint, valuePaint)
            
            if (record.location != null) {
                yPosition = drawLabelValue(canvas, "Ubicación:", record.location, yPosition, labelPaint, valuePaint)
            }
            
            val createdDateStr = dateFormat.format(record.createdDate)
            yPosition = drawLabelValue(canvas, "Creado:", createdDateStr, yPosition, labelPaint, valuePaint)
            
            val updatedDateStr = dateFormat.format(record.updatedDate)
            yPosition = drawLabelValue(canvas, "Actualizado:", updatedDateStr, yPosition, labelPaint, valuePaint)
            
            val footerPaint = Paint().apply {
                color = Color.parseColor("#999999")
                textSize = 20f
            }
            canvas.drawText("Generado con Maintenance App", PADDING.toFloat(), (IMAGE_HEIGHT - 20).toFloat(), footerPaint)
            
            saveBitmap(bitmap, "record_${record.id}_${System.currentTimeMillis()}.jpg")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateMaintenanceImage(maintenance: Maintenance, recordName: String): File? {
        return try {
            val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            canvas.drawColor(Color.WHITE)
            
            var yPosition = PADDING
            
            val headerPaint = Paint().apply {
                color = Color.parseColor("#FF9800")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, IMAGE_WIDTH.toFloat(), (yPosition + 100).toFloat(), headerPaint)
            
            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = TITLE_SIZE
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            yPosition += 80
            canvas.drawText("MANTENIMIENTO", PADDING.toFloat(), yPosition.toFloat(), titlePaint)
            
            yPosition += MARGIN_BOTTOM + 20
            
            val linePaint = Paint().apply {
                color = Color.parseColor("#CCCCCC")
                strokeWidth = 2f
            }
            canvas.drawLine(PADDING.toFloat(), yPosition.toFloat(), (IMAGE_WIDTH - PADDING).toFloat(), yPosition.toFloat(), linePaint)
            
            yPosition += MARGIN_BOTTOM
            
            val labelPaint = Paint().apply {
                color = Color.parseColor("#666666")
                textSize = LABEL_SIZE
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            
            val valuePaint = Paint().apply {
                color = Color.BLACK
                textSize = VALUE_SIZE
            }
            
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            yPosition = drawLabelValue(canvas, "Registro:", recordName, yPosition, labelPaint, valuePaint)
            yPosition = drawLabelValue(canvas, "Descripción:", maintenance.description, yPosition, labelPaint, valuePaint)
            
            val costStr = if (maintenance.cost != null) "$${maintenance.cost}" else "N/A"
            yPosition = drawLabelValue(canvas, "Costo:", costStr, yPosition, labelPaint, valuePaint)
            
            val maintenanceDateStr = dateFormat.format(maintenance.maintenanceDate)
            yPosition = drawLabelValue(canvas, "Fecha:", maintenanceDateStr, yPosition, labelPaint, valuePaint)
            
            if (maintenance.nextMaintenanceDue != null) {
                val nextDateStr = dateFormat.format(maintenance.nextMaintenanceDue)
                yPosition = drawLabelValue(canvas, "Próximo:", nextDateStr, yPosition, labelPaint, valuePaint)
            }
            
            if (!maintenance.notes.isNullOrEmpty()) {
                yPosition = drawLabelValue(canvas, "Notas:", maintenance.notes, yPosition, labelPaint, valuePaint)
            }
            
            val footerPaint = Paint().apply {
                color = Color.parseColor("#999999")
                textSize = 20f
            }
            canvas.drawText("Generado con Maintenance App", PADDING.toFloat(), (IMAGE_HEIGHT - 20).toFloat(), footerPaint)
            
            saveBitmap(bitmap, "maintenance_${maintenance.id}_${System.currentTimeMillis()}.jpg")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawLabelValue(
        canvas: Canvas,
        label: String,
        value: String,
        startY: Int,
        labelPaint: Paint,
        valuePaint: Paint
    ): Int {
        var yPosition = startY + LINE_HEIGHT
        
        canvas.drawText(label, PADDING.toFloat(), yPosition.toFloat(), labelPaint)
        
        yPosition += (LINE_HEIGHT * 0.7).roundToInt()
        
        val maxWidth = IMAGE_WIDTH - (PADDING * 2)
        val wrappedLines = wrapText(value, valuePaint, maxWidth)
        
        for (line in wrappedLines) {
            canvas.drawText(line, (PADDING + 20).toFloat(), yPosition.toFloat(), valuePaint)
            yPosition += (LINE_HEIGHT * 0.6).roundToInt()
        }
        
        return yPosition + MARGIN_BOTTOM
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Int): List<String> {
        val lines = mutableListOf<String>()
        val words = text.split(" ")
        var currentLine = ""
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val textWidth = paint.measureText(testLine)
            
            if (textWidth > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        
        return lines
    }

    private fun saveBitmap(bitmap: Bitmap, filename: String): File? {
        return try {
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ?: context.filesDir
            
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            
            val imageFile = File(storageDir, filename)
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.close()
            
            imageFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getShareableUri(file: File): android.net.Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
