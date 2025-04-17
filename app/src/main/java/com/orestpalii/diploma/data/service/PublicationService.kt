package com.orestpalii.diploma.data.service

import android.util.Log
import com.orestpalii.diploma.data.model.Post
import com.orestpalii.diploma.utils.URLFormater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object PublicationService {

    private suspend inline fun <reified T> getRequest(url: String): T = withContext(Dispatchers.IO) {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"

        val response = conn.inputStream.bufferedReader().use { it.readText() }
        JsonParser.fromJson<T>(response)
    }

    private suspend inline fun <reified T> postRequest(url: String, body: JSONObject): T = withContext(Dispatchers.IO) {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.outputStream.write(body.toString().toByteArray())

        val response = conn.inputStream.bufferedReader().use { it.readText() }
        JsonParser.fromJson<T>(response)
    }

    suspend fun fetchPostById(postId: String): Post? = withContext(Dispatchers.IO) {
        val base = URLFormater.getURL("fetchPostById") ?: return@withContext null
        val url = "$base?postId=$postId"
        getRequest<Post>(url)
    }

    suspend fun fetchUserPosts(userId: String): List<Post> = withContext(Dispatchers.IO) {
        val base = URLFormater.getURL("fetchUserPosts") ?: throw Exception("Invalid URL")
        val url = "$base?userId=$userId"
        getRequest(url)
    }

    suspend fun fetchAllPostsSortedByDate(): List<Post> = withContext(Dispatchers.IO) {
        val url = URLFormater.getURL("fetchAllPostsSortedByDate") ?: throw Exception("Invalid URL")
        getRequest(url)
    }

    suspend fun fetchLikedPosts(userId: String): List<Post> = withContext(Dispatchers.IO) {
        val base = URLFormater.getURL("fetchLikedPosts") ?: throw Exception("Invalid URL")
        val url = "$base?userId=$userId"
        getRequest(url)
    }

    suspend fun fetchRecommendedPosts(
        userEmbedding: List<Double>,
        limit: Int = 10,
        similarityThreshold: Double = 0.4
    ): List<Post> = withContext(Dispatchers.IO) {
        val url = URLFormater.getURL("fetchRecommendedPosts") ?: throw Exception("Invalid URL")
        val body = JSONObject().apply {
            put("embedding", JSONArray(userEmbedding))
            put("limit", limit)
            put("similarityThreshold", similarityThreshold)
        }
        postRequest(url, body)
    }

    suspend fun fetchSimilarPostsByText(
        query: String,
        limit: Int = 10,
        similarityThreshold: Double = 0.4
    ): List<Post> = withContext(Dispatchers.IO) {
        val url = URLFormater.getURL("fetchSimilarPostsByText") ?: throw Exception("Invalid URL")
        val body = JSONObject().apply {
            put("query", query)
            put("threshold", similarityThreshold)
            put("limit", limit)
        }
        postRequest(url, body)
    }

    suspend fun fetchPostsFromSubscriptions(userId: String): List<Post> = withContext(Dispatchers.IO) {
        val base = URLFormater.getURL("fetchpostsfromsubscriptions") ?: throw Exception("Invalid URL")
        val url = "$base?userId=$userId"
        getRequest(url)
    }

    suspend fun uploadImageToServer(imageBase64: String): String = withContext(Dispatchers.IO) {
        val endpoint = URLFormater.getURL("uploadImage")
            ?: throw IllegalStateException("Invalid URL for uploadImage")
        val body = JSONObject().put("imageBase64", imageBase64)

        val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            outputStream.use { it.write(body.toString().toByteArray()) }
        }

        val responseCode = conn.responseCode
        val responseText = conn.inputStream.bufferedReader().use { it.readText() }
        Log.d("PublicationService", "HTTP $responseCode → $responseText")
        conn.disconnect()

        val json = JSONObject(responseText)
        val imageUrl = json.optString("imageUrl")
        if (imageUrl.isBlank()) {
            throw IllegalStateException("uploadImageToServer: empty imageUrl")
        }
        imageUrl
    }




    suspend fun uploadPost(post: Post) = withContext(Dispatchers.IO) {
        val url = URLFormater.getURL("uploadPost") ?: throw Exception("Invalid URL")
        val postJson = JsonParser.toJson(post)
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.outputStream.write(postJson.toByteArray())
        conn.inputStream.close() // just to trigger the call
    }
}
