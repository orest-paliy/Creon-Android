package com.orestpalii.diploma.ui.screens.createpost

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orestpalii.diploma.data.model.Post
import com.orestpalii.diploma.data.service.ChatGPTService
import com.orestpalii.diploma.data.service.PublicationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

class CreatePostViewModel : ViewModel() {
    var title by mutableStateOf("")
        private set
    var description by mutableStateOf("")
        private set
    var prompt by mutableStateOf("")
        private set

    var imageBitmap by mutableStateOf<Bitmap?>(null)
        private set
    var imageUri by mutableStateOf<Uri?>(null)
        private set

    var isUploading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var didFinishPosting by mutableStateOf(false)
        private set

    private val IOS_REFERENCE_INTERVAL = 978_307_200.0
    private val TAG = "CreatePostVM"

    /** Змінює title/description/prompt */
    fun onTitleChange(new: String)       { title = new }
    fun onDescriptionChange(new: String) { description = new }
    fun onPromptChange(new: String)      { prompt = new }

    /** Коли галерея чи камера повертає Uri */
    fun onImageUriSelected(uri: Uri) {
        imageUri    = uri
        imageBitmap = null
        errorMessage = null
    }

    /** Коли AI‑генерація повернула Bitmap */
    fun onImagePicked(bitmap: Bitmap) {
        imageBitmap = bitmap
        imageUri    = null
        errorMessage = null
    }

    /** Очищаємо всі стани */
    fun reset() {
        title          = ""
        description    = ""
        prompt         = ""
        imageBitmap    = null
        imageUri       = null
        errorMessage   = null
        isUploading    = false
        didFinishPosting = false
    }

    fun resetFinishFlag() {
        didFinishPosting = false
    }

    fun provideImageUri(context: Context): Uri {
        val cacheDir = File(context.cacheDir, "camera_images").apply { if (!exists()) mkdirs() }
        val file     = File(cacheDir, "${UUID.randomUUID()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateImageFromPrompt() {
        Log.d(TAG, "generateImageFromPrompt() called with prompt='$prompt'")
        isUploading = true
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    ChatGPTService.generateImageBase64(
                        tags = emptyList(),
                        customPrompt = prompt
                    )
                }
                isUploading = false
                if (result != null) onImagePicked(result)
                else {
                    errorMessage = "Не вдалося згенерувати зображення"
                    Log.e(TAG, "generateImageFromPrompt: result is null")
                }
            } catch (e: Exception) {
                isUploading = false
                errorMessage = e.localizedMessage ?: "Помилка генерації"
                Log.e(TAG, "generateImageFromPrompt exception", e)
            }
        }
    }

    /** Публікує пост. Не стискаємо зображення з Uri — читаємо сирі байти */
    fun createPost(authorId: String, context: Context) {
        if (imageUri == null && imageBitmap == null) {
            errorMessage = "Будь ласка, виберіть або згенеруйте зображення."
            return
        }
        isUploading = true
        viewModelScope.launch {
            try {
                // 1) Зчитуємо сирі байти
                val bytes = withContext(Dispatchers.IO) {
                    imageUri?.let { uri ->
                        context.contentResolver.openInputStream(uri)
                            ?.readBytes()
                            ?: throw IllegalStateException("Cannot read image data")
                    } ?: ByteArrayOutputStream().use { baos ->
                        imageBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, baos)
                        baos.toByteArray()
                    }
                }
                // Base64
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

                // 2) Завантажуємо фото
                val imageUrl = PublicationService.uploadImageToServer(base64)

                // 3) GPT‑кроки
                val wasGen = prompt.trim().isNotEmpty()
                val tags   = ChatGPTService.generateTagString(imageUrl)
                val (conf, emb) = coroutineScope {
                    val c = async { ChatGPTService.aiConfidenceLevel(imageUrl) }
                    val e = async { ChatGPTService.generateEmbedding(tags) }
                    c.await() to e.await()
                }

                // 4) Формуємо пост
                val createdAt = (System.currentTimeMillis() / 1000.0) - IOS_REFERENCE_INTERVAL
                val post = Post(
                    id            = UUID.randomUUID().toString(),
                    authorId      = authorId,
                    title         = title,
                    description   = description,
                    imageUrl      = imageUrl,
                    isAIgenerated = wasGen || conf >= 50,
                    aiConfidence  = if (wasGen) 100 else conf,
                    tags          = tags,
                    embedding     = emb,
                    comments      = emptyList(),
                    likesCount    = 0,
                    likedBy       = emptyList(),
                    createdAt     = createdAt,
                    updatedAt     = null
                )
                PublicationService.uploadPost(post)

                isUploading      = false
                didFinishPosting = true
            } catch (e: Exception) {
                isUploading  = false
                errorMessage   = e.localizedMessage ?: "Помилка створення публікації"
            }
        }
    }
}
