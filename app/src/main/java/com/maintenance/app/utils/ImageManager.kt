package com.maintenance.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for managing images: saving, loading, and deleting.
 */
@Singleton
class ImageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val IMAGES_DIR = "images"
        private const val DATE_FORMAT = "yyyyMMdd_HHmmss"
        private const val IMAGE_PREFIX = "maintenance_"
        private const val IMAGE_EXTENSION = ".jpg"
        private const val QUALITY = 85
        private const val MAX_IMAGE_SIZE = 1024 // Max width/height in pixels
    }

    private val imagesDir: File by lazy {
        File(context.filesDir, IMAGES_DIR).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    /**
     * Creates a temporary file for capturing camera images.
     */
    fun createTempImageFile(): File {
        val timeStamp = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
        val imageFileName = "${IMAGE_PREFIX}${timeStamp}_temp"
        return File.createTempFile(imageFileName, IMAGE_EXTENSION, context.cacheDir)
    }

    /**
     * Gets the URI for a temporary image file using FileProvider.
     */
    fun getTempImageUri(): Uri {
        val tempFile = createTempImageFile()
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            tempFile
        )
    }

    /**
     * Saves an image from URI to internal storage with compression.
     * Returns the internal file path or null if failed.
     */
    suspend fun saveImageFromUri(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                
                if (bitmap != null) {
                    val compressedBitmap = resizeBitmap(bitmap, MAX_IMAGE_SIZE)
                    val fileName = generateUniqueFileName()
                    val file = File(imagesDir, fileName)
                    
                    FileOutputStream(file).use { out ->
                        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, out)
                    }
                    
                    bitmap.recycle()
                    compressedBitmap.recycle()
                    
                    return@withContext file.absolutePath
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Loads a bitmap from a file path.
     */
    suspend fun loadBitmapFromPath(path: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            if (File(path).exists()) {
                BitmapFactory.decodeFile(path)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Deletes an image file.
     */
    suspend fun deleteImage(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Deletes multiple images.
     */
    suspend fun deleteImages(paths: List<String>): Int = withContext(Dispatchers.IO) {
        var deletedCount = 0
        paths.forEach { path ->
            if (deleteImage(path)) {
                deletedCount++
            }
        }
        deletedCount
    }

    /**
     * Gets the file URI for an internal image path.
     */
    fun getImageUri(path: String): Uri? {
        return try {
            val file = File(path)
            if (file.exists()) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Cleans up temporary image files older than specified time.
     */
    suspend fun cleanupTempFiles(olderThanMillis: Long = 24 * 60 * 60 * 1000) = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            val cacheDir = context.cacheDir
            
            cacheDir.listFiles { file ->
                file.name.startsWith(IMAGE_PREFIX) && 
                file.name.contains("temp") &&
                file.name.endsWith(IMAGE_EXTENSION)
            }?.forEach { file ->
                if (currentTime - file.lastModified() > olderThanMillis) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Gets the total size of stored images in bytes.
     */
    suspend fun getImagesSize(): Long = withContext(Dispatchers.IO) {
        try {
            imagesDir.walkTopDown()
                .filter { it.isFile }
                .sumOf { it.length() }
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    private fun resizeBitmap(original: Bitmap, maxSize: Int): Bitmap {
        val width = original.width
        val height = original.height
        
        if (width <= maxSize && height <= maxSize) {
            return original
        }
        
        val ratio = width.toFloat() / height.toFloat()
        
        val newWidth: Int
        val newHeight: Int
        
        if (width > height) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }
        
        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true)
    }

    private fun generateUniqueFileName(): String {
        val timeStamp = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
        val random = (1000..9999).random()
        return "${IMAGE_PREFIX}${timeStamp}_${random}${IMAGE_EXTENSION}"
    }
}