package com.example.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ProfilePhotoUtils {

    /**
     * Creates a temporary file in cache directory and returns a content URI via FileProvider
     * for camera capture.
     */
    fun createCameraImageUri(context: Context): Uri {
        val cacheFolder = File(context.cacheDir, "camera_photos").apply {
            if (!exists()) mkdirs()
        }
        val photoFile = File(cacheFolder, "camera_profile_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }

    /**
     * Persists the captured image into internal files directory so the URI remains permanently
     * accessible and valid across app restarts.
     */
    fun persistProfileImage(context: Context, sourceUri: Uri): Uri {
        return try {
            val profileFolder = File(context.filesDir, "profile_photos").apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(profileFolder, "user_avatar_${System.currentTimeMillis()}.jpg")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destinationFile)
        } catch (e: Exception) {
            Log.e("ProfilePhotoUtils", "Error persisting profile photo, falling back to original URI: ${e.message}")
            sourceUri
        }
    }
}
