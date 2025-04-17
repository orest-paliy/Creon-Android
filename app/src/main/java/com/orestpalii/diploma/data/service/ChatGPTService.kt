package com.orestpalii.diploma.data.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.orestpalii.diploma.utils.URLFormater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64

object ChatGPTService {
    private const val TAG = "ChatGPTService"

    suspend fun generateTagString(imageUrl: String): String = withContext(Dispatchers.IO) {
        val endpoint = URLFormater.getURL("generateTagString") ?: return@withContext ""
        Log.d(TAG, "generateTagString → POST $endpoint { imageUrl: $imageUrl }")
        val response = postRequest(endpoint, JSONObject().put("imageUrl", imageUrl))
        val desc = response?.optString("description")?.trim() ?: ""
        Log.d(TAG, "generateTagString response description='$desc'")
        desc
    }

    suspend fun aiConfidenceLevel(imageUrl: String): Int = withContext(Dispatchers.IO) {
        val endpoint = URLFormater.getURL("aiConfidenceLevel") ?: return@withContext 0
        Log.d(TAG, "aiConfidenceLevel → POST $endpoint { imageUrl: $imageUrl }")
        val response = postRequest(endpoint, JSONObject().put("imageUrl", imageUrl))
        val conf = response?.optInt("confidence", 0) ?: 0
        Log.d(TAG, "aiConfidenceLevel response confidence=$conf")
        conf
    }

    suspend fun generateEmbedding(text: String): List<Double> = withContext(Dispatchers.IO) {
        val endpoint = URLFormater.getURL("generateTextEmbedding") ?: return@withContext emptyList()
        Log.d(TAG, "generateEmbedding → POST $endpoint { text: '$text' }")
        val response = postRequest(endpoint, JSONObject().put("text", text))
        val jsonArray = response?.optJSONArray("embedding")
        val emb = if (jsonArray != null) List(jsonArray.length()) { i -> jsonArray.getDouble(i) } else emptyList()
        Log.d(TAG, "generateEmbedding response embedding size=${emb.size}")
        emb
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun generateImageBase64(
        tags: List<String> = emptyList(),
        customPrompt: String? = null
    ): Bitmap? = withContext(Dispatchers.IO) {
        val endpoint = URLFormater.getURL("generateAvatarImageBase64")
        if (endpoint == null) {
            Log.e(TAG, "generateImageBase64: endpoint URL is null")
            return@withContext null
        }
        val body = JSONObject().apply {
            put("tags", JSONArray(tags))
            put("customPrompt", customPrompt ?: "")
        }
        Log.d(TAG, "generateImageBase64 → POST $endpoint body=$body")

        val response = postRequest(endpoint, body)
        if (response == null) {
            Log.e(TAG, "generateImageBase64: postRequest returned null")
            return@withContext null
        }

        val base64 = response.optString("base64Image")
        if (base64.isBlank()) {
            Log.e(TAG, "generateImageBase64: empty base64Image in response $response")
            return@withContext null
        }
        Log.d(TAG, "generateImageBase64: received base64 length=${base64.length}")

        return@withContext try {
            val bytes = Base64.getDecoder().decode(base64)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "generateImageBase64: failed to decode bitmap", e)
            null
        }
    }

    private fun postRequest(urlString: String, body: JSONObject): JSONObject? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                outputStream.use { it.write(body.toString().toByteArray()) }
            }

            val code = conn.responseCode
            // Обираємо потік: якщо 2xx — inputStream, інакше — errorStream
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream.bufferedReader().use { it.readText() }
            Log.d("ChatGPTService", "POST $urlString → HTTP $code, body:\n$text")

            if (code !in 200..299) {
                // Якщо сервер повернув помилку — викидаємо, щоб корутина пролічила це як виключення
                throw IllegalStateException("HTTP $code: $text")
            }

            JSONObject(text)
        } catch (e: Exception) {
            Log.e("ChatGPTService", "postRequest failed for $urlString with body=$body", e)
            null
        } finally {
            conn?.disconnect()
        }
    }
}
