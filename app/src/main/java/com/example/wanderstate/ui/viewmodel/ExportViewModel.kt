package com.example.wanderstate.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderstate.R
import com.example.wanderstate.data.dao.JournalEntryDao
import com.example.wanderstate.data.dao.PhotoDao
import com.example.wanderstate.data.entities.PhotoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Export states for tracking progress and results.
 */
sealed class ExportState {
    data object Idle : ExportState()
    data object Loading : ExportState()
    data class Success(val fileUri: Uri) : ExportState()
    data class Error(val message: String) : ExportState()
}

/**
 * ViewModel for handling photo and data exports.
 */
@HiltViewModel
class ExportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val photoDao: PhotoDao,
    private val journalEntryDao: JournalEntryDao
) : ViewModel() {

    companion object {
        private const val TAG = "ExportViewModel"
    }

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    /**
     * Generate preview bitmap for export without saving to file.
     * Returns the generated Polaroid bitmap for preview purposes.
     */
    suspend fun generateExportPreview(photoId: Long): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val photo = photoDao.getPhotoById(photoId)
                if (photo == null) {
                    Log.e(TAG, "Photo not found for preview: $photoId")
                    return@withContext null
                }

                val journal = journalEntryDao.getJournalByPhotoId(photoId)

                // Generate the bitmap but don't save it
                generatePolaroidBitmap(photo, journal?.entryText)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate preview", e)
                null
            }
        }
    }

    /**
     * Save a bitmap to cache and share it.
     */
    fun saveAndShareBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading

            try {
                withContext(Dispatchers.IO) {
                    val exportFile = File(context.cacheDir, "export_${System.currentTimeMillis()}.png")
                    FileOutputStream(exportFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        out.flush()
                    }

                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        exportFile
                    )

                    _exportState.value = ExportState.Success(uri)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save and share bitmap", e)
                _exportState.value = ExportState.Error("Failed to save: ${e.message}")
            }
        }
    }

    /**
     * Export a single photo with location, date, and journal overlay.
     */
    fun exportPhotoWithJournal(photoId: Long) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading

            try {
                // Add 30 second timeout for the entire export operation
                withTimeout(30000) {
                    // Get photo and journal data
                    val photo = withContext(Dispatchers.IO) {
                        photoDao.getPhotoById(photoId)
                    }

                    if (photo == null) {
                        Log.e(TAG, "Photo not found for ID: $photoId")
                        _exportState.value = ExportState.Error("Photo not found")
                        return@withTimeout
                    }

                    val journal = withContext(Dispatchers.IO) {
                        journalEntryDao.getJournalByPhotoId(photoId)
                    }

                    // Generate the bitmap
                    val bitmap = withContext(Dispatchers.IO) {
                        generatePolaroidBitmap(photo, journal?.entryText)
                    }

                    // Save bitmap to file
                    val exportedFile = withContext(Dispatchers.IO) {
                        val file = File(context.cacheDir, "export_${System.currentTimeMillis()}.png")
                        FileOutputStream(file).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            out.flush()
                        }
                        bitmap.recycle()
                        file
                    }

                    if (!exportedFile.exists()) {
                        Log.e(TAG, "Export file was not created: ${exportedFile.absolutePath}")
                        _exportState.value = ExportState.Error("Failed to create export file")
                        return@withTimeout
                    }

                    // Get shareable URI
                    val uri = try{
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            exportedFile
                        )
                    } catch (e: IllegalArgumentException) {
                        Log.e(TAG, "FileProvider error", e)
                        _exportState.value = ExportState.Error("FileProvider error: ${e.message}")
                        return@withTimeout
                    }

                    _exportState.value = ExportState.Success(uri)
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Export timed out after 30 seconds", e)
                _exportState.value = ExportState.Error("Export timed out - image may be too large")
            } catch (e: OutOfMemoryError) {
                Log.e(TAG, "Out of memory during export", e)
                _exportState.value = ExportState.Error("Image too large - out of memory")
            } catch (e: Exception) {
                Log.e(TAG, "Export failed with exception", e)
                e.printStackTrace()
                _exportState.value = ExportState.Error("Export failed: ${e.message}")
            }
        }
    }

    /**
     * Generate a Polaroid-style bitmap with photo and text overlays.
     * Returns the bitmap without saving to file.
     */
    private fun generatePolaroidBitmap(
        photo: PhotoEntity,
        journalText: String?
    ): Bitmap {
        // Load original photo from file path
        var bitmap = BitmapFactory.decodeFile(photo.uri)

        if (bitmap == null) {
            Log.e(TAG, "generatePhotoWithOverlay: Failed to decode bitmap from ${photo.uri}")
            throw IllegalStateException("Failed to load photo bitmap from: ${photo.uri}")
        }

        // Handle EXIF orientation to fix rotation issues
        try {
            val exif = ExifInterface(photo.uri)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            bitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: Exception) {
            Log.w(TAG, "generatePhotoWithOverlay: Failed to read EXIF data, using original orientation", e)
        }

        // SCALE UP the bitmap to ensure high resolution for sharp text
        val targetWidth = 2000  // High resolution target for crisp text (increased from 2000)
        val scale = targetWidth.toFloat() / bitmap.width.toFloat()
        val scaledHeight = (bitmap.height * scale).toInt()

        val highResBitmap = Bitmap.createScaledBitmap(
            bitmap,
            targetWidth,
            scaledHeight,
            true  // Use filtering for quality
        )

        // Recycle the original bitmap since we now have the high-res version
        if (highResBitmap != bitmap) {
            bitmap.recycle()
        }

        // Calculate dimensions using high-res bitmap
        val width = highResBitmap.width
        val height = highResBitmap.height

        // Check if image is too large
        val megapixels = (width * height) / 1_000_000f

        if (megapixels > 20) {
            Log.w(TAG, "generatePhotoWithOverlay: Large image detected, may cause memory issues")
        }

        // Polaroid dimensions
        val photoBorderSize = 40  // White border on top/sides
        val bottomBarHeight = 400  // Thick bottom bar (Polaroid style)

        // Calculate final dimensions
        val polaroidWidth = width + (photoBorderSize * 2)
        val polaroidHeight = height + photoBorderSize + bottomBarHeight

        val polaroidBitmap = try {
            Bitmap.createBitmap(polaroidWidth, polaroidHeight, Bitmap.Config.ARGB_8888)
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "generatePhotoWithOverlay: Out of memory creating output bitmap", e)
            highResBitmap.recycle()
            throw e
        }

        val canvas = Canvas(polaroidBitmap)

        // Fill entire canvas with white (Polaroid background)
        canvas.drawColor(android.graphics.Color.WHITE)

        // Draw high-res photo inset from borders
        canvas.drawBitmap(
            highResBitmap,
            photoBorderSize.toFloat(),
            photoBorderSize.toFloat(),
            null
        )

        // Format date
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
        val formattedDate = dateFormat.format(Date(photo.capturedDate))
        val city = photo.cityName.trim()
        val state = photo.stateCode

        // Text configuration - MORE spacing from photo
        val textStartY = highResBitmap.height + (photoBorderSize * 3.5f)
        val leftMargin = photoBorderSize + 40f
        val centerX = polaroidWidth / 2f

        // Load custom fonts with fallbacks
        val playfairFont = try {
            ResourcesCompat.getFont(context, R.font.playfair_display_bold)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load Playfair Display font, using fallback", e)
            Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val journalFont = try {
            ResourcesCompat.getFont(context, R.font.caveat)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load Caveat font, using fallback", e)
            Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        // Location paint (left side, top line, larger/bold) - Playfair Display font
        val locationPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 90f
            isAntiAlias = true
            isSubpixelText = true  // Improves text sharpness
            isFilterBitmap = true   // Improves quality
            isDither = true         // Reduces banding
            typeface = playfairFont  // Custom Playfair Display font
            textAlign = Paint.Align.LEFT
        }

        // Date paint (left side, below location, smaller) - Lato regular
        val latoPaint = try {
            ResourcesCompat.getFont(context, R.font.lato)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load Lato font, using fallback", e)
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val datePaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 50f
            isAntiAlias = true
            isSubpixelText = true  // Improves text sharpness
            isFilterBitmap = true   // Improves quality
            isDither = true         // Reduces banding
            typeface = latoPaint
            textAlign = Paint.Align.LEFT
        }

        // Journal paint (right side) - Caveat handwritten font
        val journalPaint = Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 60f  // Slightly larger for handwritten font
            isAntiAlias = true
            isSubpixelText = true  // Improves text sharpness
            isFilterBitmap = true   // Improves quality
            isDither = true         // Reduces banding
            typeface = journalFont  // Custom Caveat handwritten font
            textAlign = Paint.Align.LEFT
        }

        // LEFT SIDE: Location FIRST, then Date below
        canvas.drawText("$city, $state", leftMargin, textStartY, locationPaint)  // Location first
        canvas.drawText(formattedDate, leftMargin, textStartY + 85f, datePaint)  // Date below

        // RIGHT SIDE: Journal entry (with text wrapping)
        if (journalText != null && journalText.isNotEmpty()) {
            val journalX = centerX
            val maxJournalWidth = polaroidWidth - centerX - 40f

            // Wrap text to fit width
            val words = journalText.split(" ")
            var currentLine = ""
            var lineY = textStartY

            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val testWidth = journalPaint.measureText(testLine)

                if (testWidth > maxJournalWidth && currentLine.isNotEmpty()) {
                    // Draw current line and start new one
                    canvas.drawText(currentLine, journalX, lineY, journalPaint)
                    currentLine = word
                    lineY += 60f

                    // Stop if we run out of vertical space
                    if (lineY > height + photoBorderSize + bottomBarHeight - 20f) {
                        currentLine += "..."
                        canvas.drawText(currentLine, journalX, lineY, journalPaint)
                        break
                    }
                } else {
                    currentLine = testLine
                }
            }

            // Draw last line
            if (currentLine.isNotEmpty() && lineY < height + photoBorderSize + bottomBarHeight - 20f) {
                canvas.drawText(currentLine, journalX, lineY, journalPaint)
            }
        }

        // Clean up high-res bitmap (but not polaroid - we're returning it)
        highResBitmap.recycle()

        return polaroidBitmap
    }

    /**
     * Rotate a bitmap by the specified degrees.
     */
    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees)
        }
        val rotated = Bitmap.createBitmap(
            source, 0, 0,
            source.width, source.height,
            matrix, true
        )
        // Recycle the source bitmap if it's different from the rotated one
        if (rotated != source) {
            source.recycle()
        }
        return rotated
    }

    /**
     * Wrap text to fit within a specified width.
     */
    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val bounds = Rect()
            paint.getTextBounds(testLine, 0, testLine.length, bounds)

            if (bounds.width() <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                currentLine = word
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines
    }

    /**
     * Reset export state to idle.
     */
    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }
}
