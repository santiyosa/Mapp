package com.maintenance.app.domain.usecases.images

import android.net.Uri
import com.maintenance.app.domain.usecases.base.UseCase
import com.maintenance.app.utils.ImageManager
import com.maintenance.app.utils.Result
import javax.inject.Inject

/**
 * Use case for handling image capture and gallery selection.
 */
class ImageCaptureUseCase @Inject constructor(
    private val imageManager: ImageManager
) : UseCase<ImageCaptureUseCase.Params, String>() {

    override suspend fun execute(parameters: Params): String {
        return when (parameters.source) {
            ImageSource.CAMERA -> {
                // For camera, the image is already captured to the URI
                // We need to save it to internal storage
                imageManager.saveImageFromUri(parameters.imageUri)
                    ?: throw Exception("Failed to save camera image")
            }
            ImageSource.GALLERY -> {
                // For gallery, we need to copy the selected image to internal storage
                imageManager.saveImageFromUri(parameters.imageUri)
                    ?: throw Exception("Failed to save gallery image")
            }
        }
    }

    data class Params(
        val source: ImageSource,
        val imageUri: Uri
    )

    enum class ImageSource {
        CAMERA,
        GALLERY
    }
}

/**
 * Use case for deleting images.
 */
class DeleteImageUseCase @Inject constructor(
    private val imageManager: ImageManager
) : UseCase<DeleteImageUseCase.Params, Boolean>() {

    override suspend fun execute(parameters: Params): Boolean {
        return imageManager.deleteImage(parameters.imagePath)
    }

    data class Params(
        val imagePath: String
    )
}

/**
 * Use case for getting image URI from internal path.
 */
class GetImageUriUseCase @Inject constructor(
    private val imageManager: ImageManager
) {
    operator fun invoke(imagePath: String): Uri? {
        return imageManager.getImageUri(imagePath)
    }
}

/**
 * Use case for creating temporary image file for camera capture.
 */
class CreateTempImageUseCase @Inject constructor(
    private val imageManager: ImageManager
) {
    operator fun invoke(): Uri {
        return imageManager.getTempImageUri()
    }
}