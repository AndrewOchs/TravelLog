package com.example.travellog.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.travellog.data.dao.PhotoDao
import com.example.travellog.data.entities.PhotoEntity
import com.example.travellog.data.models.StatePhotoCount
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PhotoRepository"

interface PhotoRepository {
    suspend fun savePhoto(
        photoUri: Uri,
        stateCode: String,
        stateName: String,
        cityName: String,
        capturedDate: Long
    ): Result<Long>

    fun getStatePhotoCounts(): Flow<List<StatePhotoCount>>
    fun getPhotosByState(stateCode: String): Flow<List<PhotoEntity>>
    suspend fun deletePhoto(photoId: Long)
}

@Singleton
class PhotoRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val photoDao: PhotoDao
) : PhotoRepository {

    override suspend fun savePhoto(
        photoUri: Uri,
        stateCode: String,
        stateName: String,
        cityName: String,
        capturedDate: Long
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // Normalize city name to prevent grouping issues
            val normalizedCityName = cityName.trim()

            Log.d(TAG, "═══════════════════════════════════")
            Log.d(TAG, "SAVING PHOTO TO DATABASE")
            Log.d(TAG, "Input URI: $photoUri")
            Log.d(TAG, "State: $stateCode - $stateName")
            Log.d(TAG, "City: '$cityName' -> Normalized: '$normalizedCityName'")
            Log.d(TAG, "Captured Date: $capturedDate")

            // Create directory structure: photos/{state}/{city}/
            val stateDir = File(context.filesDir, "photos/$stateCode")
            val cityDir = File(stateDir, normalizedCityName)

            if (!cityDir.exists()) {
                cityDir.mkdirs()
                Log.d(TAG, "Created directory: ${cityDir.absolutePath}")
            }

            // Generate unique filename with timestamp
            val timestamp = System.currentTimeMillis()
            val photoFile = File(cityDir, "$timestamp.jpg")
            val thumbnailFile = File(cityDir, "${timestamp}_thumb.jpg")

            Log.d(TAG, "Copying photo to: ${photoFile.absolutePath}")

            // Copy photo from picker URI to app storage
            context.contentResolver.openInputStream(photoUri)?.use { input ->
                FileOutputStream(photoFile).use { output ->
                    val bytesCopied = input.copyTo(output)
                    Log.d(TAG, "Copied $bytesCopied bytes")
                }
            } ?: run {
                Log.e(TAG, "Failed to open photo URI")
                return@withContext Result.failure(Exception("Failed to open photo URI"))
            }

            // Create thumbnail (for now, just copy the same file - can optimize later)
            photoFile.copyTo(thumbnailFile, overwrite = true)
            Log.d(TAG, "Created thumbnail: ${thumbnailFile.absolutePath}")

            // Create PhotoEntity
            val photoEntity = PhotoEntity(
                uri = photoFile.absolutePath,
                stateCode = stateCode,
                stateName = stateName,
                cityName = normalizedCityName,
                latitude = null, // Can extract from EXIF later
                longitude = null, // Can extract from EXIF later
                capturedDate = capturedDate,
                addedDate = timestamp,
                thumbnailUri = thumbnailFile.absolutePath
            )

            Log.d(TAG, "Inserting PhotoEntity into database...")
            // Insert into database
            val photoId = photoDao.insert(photoEntity)
            Log.d(TAG, "✓ SUCCESS! Photo saved with ID: $photoId")
            Log.d(TAG, "  - File: ${photoFile.absolutePath}")
            Log.d(TAG, "  - State: $stateCode")
            Log.d(TAG, "  - City: $normalizedCityName")
            Log.d(TAG, "═══════════════════════════════════")

            Result.success(photoId)
        } catch (e: Exception) {
            Log.e(TAG, "✗ FAILED to save photo", e)
            Log.e(TAG, "Error: ${e.message}")
            Log.e(TAG, "═══════════════════════════════════")
            Result.failure(e)
        }
    }

    override fun getStatePhotoCounts(): Flow<List<StatePhotoCount>> {
        return photoDao.getStatePhotoCounts()
    }

    override fun getPhotosByState(stateCode: String): Flow<List<PhotoEntity>> {
        return photoDao.getByState(stateCode)
    }

    override suspend fun deletePhoto(photoId: Long) = withContext(Dispatchers.IO) {
        photoDao.deleteById(photoId)
    }
}
