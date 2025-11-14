package com.maintenance.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.maintenance.app.domain.model.Maintenance
import com.maintenance.app.domain.model.Record
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Manager for sharing records and maintenances via WhatsApp and other apps.
 */
class ShareManager(private val context: Context) {

    private val imageGenerator = ShareImageGenerator(context)

    fun shareRecordViaWhatsApp(record: Record): Boolean {
        return try {
            val imageFile = imageGenerator.generateRecordImage(record) ?: return false
            val uri = imageGenerator.getShareableUri(imageFile)
            
            val message = buildRecordShareMessage(record)
            shareViaWhatsApp(message, uri)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun shareMaintenanceViaWhatsApp(maintenance: Maintenance, recordName: String): Boolean {
        return try {
            val imageFile = imageGenerator.generateMaintenanceImage(maintenance, recordName) ?: return false
            val uri = imageGenerator.getShareableUri(imageFile)
            
            val message = buildMaintenanceShareMessage(maintenance, recordName)
            shareViaWhatsApp(message, uri)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun shareRecordGeneric(record: Record): Boolean {
        return try {
            val imageFile = imageGenerator.generateRecordImage(record) ?: return false
            val uri = imageGenerator.getShareableUri(imageFile)
            
            val message = buildRecordShareMessage(record)
            shareGeneric(message, uri, "Compartir Registro")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun shareMaintenanceGeneric(maintenance: Maintenance, recordName: String): Boolean {
        return try {
            val imageFile = imageGenerator.generateMaintenanceImage(maintenance, recordName) ?: return false
            val uri = imageGenerator.getShareableUri(imageFile)
            
            val message = buildMaintenanceShareMessage(maintenance, recordName)
            shareGeneric(message, uri, "Compartir Mantenimiento")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun shareViaWhatsApp(message: String, imageUri: Uri) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_STREAM, imageUri)
            type = "image/*"
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            shareGeneric(message, imageUri, "Compartir")
        }
    }

    private fun shareGeneric(message: String, imageUri: Uri, title: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_STREAM, imageUri)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, title))
    }

    private fun buildRecordShareMessage(record: Record): String {
        return """🔧 *REGISTRO DE MANTENIMIENTO*

*Nombre:* ${record.name}
*Categoría:* ${record.category ?: "N/A"}
*Descripción:* ${record.description ?: "N/A"}

📱 Compartido desde Maintenance App"""
    }

    private fun buildMaintenanceShareMessage(maintenance: Maintenance, recordName: String): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        val nextMaintenanceText = if (maintenance.nextMaintenanceDue != null) {
            "\n*Próximo Mantenimiento:* ${dateFormat.format(maintenance.nextMaintenanceDue)}"
        } else {
            ""
        }
        
        val notesText = if (!maintenance.notes.isNullOrEmpty()) {
            "\n*Notas:* ${maintenance.notes}"
        } else {
            ""
        }
        
        val costText = if (maintenance.cost != null) {
            "\n*Costo:* $$${maintenance.cost}"
        } else {
            ""
        }
        
        return """🔧 *MANTENIMIENTO REALIZADO*

*Registro:* $recordName
*Descripción:* ${maintenance.description}
*Fecha:* ${dateFormat.format(maintenance.maintenanceDate)}$costText$nextMaintenanceText$notesText

📱 Compartido desde Maintenance App"""
    }

    fun isWhatsAppInstalled(): Boolean {
        return try {
            val pm = context.packageManager
            pm.getApplicationInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
