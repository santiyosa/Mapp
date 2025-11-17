package com.maintenance.app.presentation.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.maintenance.app.R
import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.model.Record
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Utility for generating shareable images from maintenance records
 */
object ImageGenerationUtils {

    private const val IMAGE_WIDTH = 1080
    private const val IMAGE_HEIGHT = 1440
    private const val PADDING = 40
    private const val LINE_HEIGHT = 50

    /**
     * Generate image for a single maintenance record
     */
    fun generateRecordImage(
        context: Context,
        record: Record,
        maintenance: Maintenance? = null
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Background
        canvas.drawColor(Color.WHITE)

        // Paint objects
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1F51BA") // Primary blue
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#455A64") // Dark gray
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#212121") // Black
            textSize = 24f
        }

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#757575") // Gray
            textSize = 20f
        }

        val dividerPaint = Paint().apply {
            color = Color.parseColor("#E0E0E0") // Light gray
            strokeWidth = 2f
        }

        var yPosition = PADDING.toFloat()

        // Title
        canvas.drawText("🔧 MAINTENANCE RECORD", PADDING.toFloat(), yPosition + 40, titlePaint)
        yPosition += 80f

        // Divider
        canvas.drawLine(PADDING.toFloat(), yPosition, (IMAGE_WIDTH - PADDING).toFloat(), yPosition, dividerPaint)
        yPosition += 40f

        // Device name
        canvas.drawText("Device:", PADDING.toFloat(), yPosition, headerPaint)
        canvas.drawText(record.name, PADDING + 250f, yPosition, textPaint)
        yPosition += LINE_HEIGHT

        // Category
        canvas.drawText("Category:", PADDING.toFloat(), yPosition, headerPaint)
        canvas.drawText(record.category ?: "", PADDING + 250f, yPosition, textPaint)
        yPosition += LINE_HEIGHT

        // Type
        if (maintenance != null) {
            canvas.drawText("Type:", PADDING.toFloat(), yPosition, headerPaint)
            canvas.drawText(maintenance.type, PADDING + 250f, yPosition, textPaint)
            yPosition += LINE_HEIGHT
        }

        // Description
        canvas.drawText("Description:", PADDING.toFloat(), yPosition, headerPaint)
        yPosition += 40f

        val description = maintenance?.description ?: (record.description ?: "No description")
        val descriptionLines = wrapText(description, 35)
        for (line in descriptionLines) {
            canvas.drawText(line, PADDING + 30f, yPosition, textPaint)
            yPosition += LINE_HEIGHT
        }
        yPosition += 20f

        // Divider
        canvas.drawLine(PADDING.toFloat(), yPosition, (IMAGE_WIDTH - PADDING).toFloat(), yPosition, dividerPaint)
        yPosition += 40f

        // Cost
        if (maintenance != null) {
            canvas.drawText("Cost:", PADDING.toFloat(), yPosition, headerPaint)
            canvas.drawText("$${maintenance.cost ?: "0"}", PADDING + 250f, yPosition, textPaint)
            yPosition += LINE_HEIGHT

            // Date
            canvas.drawText("Date:", PADDING.toFloat(), yPosition, headerPaint)
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val dateStr = dateFormat.format(java.util.Date.from(maintenance.maintenanceDate.atZone(java.time.ZoneId.systemDefault()).toInstant()))
            canvas.drawText(dateStr, PADDING + 250f, yPosition, textPaint)
            yPosition += LINE_HEIGHT

            // Next maintenance date
            if (maintenance.nextMaintenanceDue != null) {
                canvas.drawText("Next:", PADDING.toFloat(), yPosition, headerPaint)
                val nextDateStr = dateFormat.format(java.util.Date.from(maintenance.nextMaintenanceDue!!.atZone(java.time.ZoneId.systemDefault()).toInstant()))
                canvas.drawText(nextDateStr, PADDING + 250f, yPosition, textPaint)
                yPosition += LINE_HEIGHT
            }
        }

        yPosition += 40f

        // Footer
        canvas.drawLine(PADDING.toFloat(), yPosition, (IMAGE_WIDTH - PADDING).toFloat(), yPosition, dividerPaint)
        yPosition += 40f

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#9E9E9E") // Medium gray
            textSize = 18f
        }

        val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generated by MaintenanceApp • $currentDate", PADDING.toFloat(), yPosition, footerPaint)

        return bitmap
    }

    /**
     * Generate summary image for a device
     */
    fun generateDeviceSummaryImage(
        context: Context,
        deviceName: String,
        totalMaintenance: Int,
        totalCost: String,
        recentMaintenances: List<Pair<String, String>>
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.WHITE)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1F51BA")
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#455A64")
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#212121")
            textSize = 24f
        }

        val dividerPaint = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            strokeWidth = 2f
        }

        var yPosition = PADDING.toFloat()

        // Title
        canvas.drawText("📋 MAINTENANCE SUMMARY", PADDING.toFloat(), yPosition + 40, titlePaint)
        yPosition += 80f

        // Divider
        canvas.drawLine(PADDING.toFloat(), yPosition, (IMAGE_WIDTH - PADDING).toFloat(), yPosition, dividerPaint)
        yPosition += 40f

        // Device name
        canvas.drawText("Device:", PADDING.toFloat(), yPosition, headerPaint)
        canvas.drawText(deviceName, PADDING + 250f, yPosition, textPaint)
        yPosition += LINE_HEIGHT

        // Total maintenance
        canvas.drawText("Total Maintenance:", PADDING.toFloat(), yPosition, headerPaint)
        canvas.drawText(totalMaintenance.toString(), PADDING + 250f, yPosition, textPaint)
        yPosition += LINE_HEIGHT

        // Total cost
        canvas.drawText("Total Cost:", PADDING.toFloat(), yPosition, headerPaint)
        canvas.drawText(totalCost, PADDING + 250f, yPosition, textPaint)
        yPosition += LINE_HEIGHT + 20f

        // Recent maintenance header
        canvas.drawText("Recent Maintenance:", PADDING.toFloat(), yPosition, headerPaint)
        yPosition += LINE_HEIGHT

        // Recent maintenance list
        val bulletPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#212121")
            textSize = 22f
        }

        for ((item, cost) in recentMaintenances.take(5)) {
            canvas.drawText("• $item - $cost", PADDING + 30f, yPosition, bulletPaint)
            yPosition += LINE_HEIGHT - 10
        }

        yPosition += 40f

        // Divider
        canvas.drawLine(PADDING.toFloat(), yPosition, (IMAGE_WIDTH - PADDING).toFloat(), yPosition, dividerPaint)
        yPosition += 40f

        // Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#9E9E9E")
            textSize = 18f
        }

        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        canvas.drawText("Generated by MaintenanceApp • $currentDate", PADDING.toFloat(), yPosition, footerPaint)

        return bitmap
    }

    /**
     * Wrap text to fit width
     */
    private fun wrapText(text: String, maxCharsPerLine: Int): List<String> {
        val lines = mutableListOf<String>()
        var remaining = text
        
        while (remaining.isNotEmpty()) {
            val line = if (remaining.length > maxCharsPerLine) {
                remaining.substring(0, maxCharsPerLine) + "-"
            } else {
                remaining
            }
            lines.add(line)
            remaining = if (remaining.length > maxCharsPerLine) {
                remaining.substring(maxCharsPerLine)
            } else {
                ""
            }
        }
        
        return lines
    }

    /**
     * Save bitmap to file
     */
    fun saveBitmapToFile(bitmap: Bitmap, context: Context, filename: String = "maintenance_${System.currentTimeMillis()}.jpg"): File {
        val cacheDir = context.cacheDir
        val file = File(cacheDir, filename)

        try {
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
                fos.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return file
    }

    /**
     * Compress bitmap
     */
    fun compressBitmap(bitmap: Bitmap, quality: Int = 85): Bitmap {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val compressedData = outputStream.toByteArray()

        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * 0.9).roundToInt(),
            (bitmap.height * 0.9).roundToInt(),
            true
        )
    }

    /**
     * Get file size in MB
     */
    fun getFileSizeMB(file: File): Double {
        return file.length() / (1024.0 * 1024.0)
    }
}
