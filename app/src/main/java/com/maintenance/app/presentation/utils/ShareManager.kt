package com.maintenance.app.presentation.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Manager for sharing content to WhatsApp and other apps
 */
object ShareManager {

    /**
     * Check if WhatsApp is installed
     */
    fun isWhatsAppInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if WhatsApp Business is installed
     */
    fun isWhatsAppBusinessInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.whatsapp.w4b", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Share text to WhatsApp
     */
    fun shareToWhatsApp(context: Context, text: String): Boolean {
        return if (isWhatsAppInstalled(context)) {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
                setPackage("com.whatsapp")
            }
            try {
                context.startActivity(intent)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else {
            false
        }
    }

    /**
     * Share text and image to WhatsApp
     */
    fun shareToWhatsAppWithImage(
        context: Context,
        text: String,
        imageFile: File
    ): Boolean {
        return if (isWhatsAppInstalled(context)) {
            try {
                val imageUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )

                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text)
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    type = "image/jpeg"
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(intent)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else {
            false
        }
    }

    /**
     * Share text to other apps (generic share)
     */
    fun shareToOtherApps(context: Context, text: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

    /**
     * Share text and image to other apps (generic share)
     */
    fun shareToOtherAppsWithImage(
        context: Context,
        text: String,
        imageFile: File
    ) {
        try {
            val imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_STREAM, imageUri)
                type = "image/jpeg"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Share via"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Copy text to clipboard
     */
    fun copyToClipboard(context: Context, text: String): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Maintenance Record", text)
            clipboard.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Share multiple files
     */
    fun shareMultipleFiles(
        context: Context,
        text: String,
        files: List<File>
    ) {
        try {
            val uris = files.map {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    it
                )
            }

            val intent = Intent().apply {
                action = Intent.ACTION_SEND_MULTIPLE
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                putExtra(Intent.EXTRA_TEXT, text)
                type = "image/jpeg"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Share via"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Open WhatsApp directly (text only)
     */
    fun openWhatsAppDirect(context: Context, phoneNumber: String? = null, text: String? = null): Boolean {
        return try {
            val message = text?.replace(" ", "%20") ?: ""
            val url = if (phoneNumber != null) {
                "https://api.whatsapp.com/send?phone=$phoneNumber&text=$message"
            } else {
                "https://wa.me/?text=$message"
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }

            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get file size for display
     */
    fun getFileSizeString(file: File): String {
        val sizeInBytes = file.length()
        return when {
            sizeInBytes < 1024 -> "$sizeInBytes B"
            sizeInBytes < 1024 * 1024 -> String.format("%.2f KB", sizeInBytes / 1024.0)
            else -> String.format("%.2f MB", sizeInBytes / (1024.0 * 1024.0))
        }
    }

    /**
     * Delete file safely
     */
    fun deleteFile(file: File): Boolean {
        return try {
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Clean up old cache files
     */
    fun cleanupOldCacheFiles(context: Context, ageInHours: Int = 24) {
        try {
            val cacheDir = context.cacheDir
            val currentTime = System.currentTimeMillis()
            val ageInMillis = ageInHours * 60 * 60 * 1000L

            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.startsWith("maintenance_")) {
                    if (currentTime - file.lastModified() > ageInMillis) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
