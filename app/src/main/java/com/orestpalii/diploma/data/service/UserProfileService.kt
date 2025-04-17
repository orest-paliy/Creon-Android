package com.orestpalii.diploma.data.service

import android.graphics.Bitmap
import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.orestpalii.diploma.data.model.User
import com.orestpalii.diploma.utils.URLFormater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

object UserProfileService {

    val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    val currentUserEmail: String?
        get() = FirebaseAuth.getInstance().currentUser?.email

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    suspend fun fetchUserProfile(uid: String): User = withContext(Dispatchers.IO) {
        val base = URLFormater.getURL("fetchuserprofile") ?: throw Exception("Bad URL")
        val url = "$base?uid=$uid"
        val response = URL(url).readText()
        JsonParser.fromJson(response)
    }

    suspend fun createUserProfile(
        email: String,
        interests: List<String>,
        avatarURL: String,
        embedding: List<Double>,
        subscriptions: List<String> = emptyList(),
        followers: List<String> = emptyList()
    ) = withContext(Dispatchers.IO) {
        val uid = currentUserId ?: throw Exception("Missing UID")
        val url = URL(URLFormater.getURL("createUserProfile") ?: throw Exception("Bad URL"))

        val body = JSONObject().apply {
            put("uid", uid)
            put("email", email)
            put("interests", JSONArray(interests))
            put("embedding", JSONArray(embedding))
            put("avatarURL", avatarURL)
            put("createdAt", System.currentTimeMillis())
            put("subscriptions", JSONArray(subscriptions))
            put("followers", JSONArray(followers))
        }

        postRequest(url, body)
    }

    suspend fun updateUserEmbedding(postEmbedding: List<Double>, alpha: Float = 0.1f) = withContext(Dispatchers.IO) {
        val uid = currentUserId ?: return@withContext
        val user = fetchUserProfile(uid)

        val updated = updatedEmbedding(
            userEmbedding = user.embedding.map { it.toFloat() },
            postEmbedding = postEmbedding.map { it.toFloat() },
            alpha
        ).map { it.toDouble() }

        createUserProfile(
            email = user.email,
            interests = user.interests,
            avatarURL = user.avatarURL,
            embedding = updated,
            subscriptions = user.subscriptions ?: emptyList(),
            followers = user.followers ?: emptyList()
        )
    }

    suspend fun checkIfUserProfileExists(uid: String): Boolean = withContext(Dispatchers.IO) {
        val base = URLFormater.getURL("checkifuserprofileexists") ?: return@withContext false
        val url = "$base?uid=$uid"
        val response = URL(url).readText()
        JSONObject(response).optBoolean("exists", false)
    }

    suspend fun uploadAvatarImage(image: Bitmap, uid: String): String = withContext(Dispatchers.IO) {
        val resized = Bitmap.createScaledBitmap(image, 256, 256, true)
        val stream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)

        val url = URL(URLFormater.getURL("uploadavatarimage") ?: throw Exception("Bad URL"))

        val body = JSONObject().apply {
            put("imageBase64", base64)
            put("uid", uid)
        }

        val response = postRequest(url, body)
        response.optString("imageUrl", "")
    }

    suspend fun subscribe(toUserId: String, fromUserId: String) = withContext(Dispatchers.IO) {
        val url = URL(URLFormater.getURL("subscribeToUser") ?: throw Exception("Bad URL"))
        val body = JSONObject().apply {
            put("currentUserId", fromUserId)
            put("userIdToSubscribe", toUserId)
        }
        postRequest(url, body)
    }

    suspend fun unsubscribe(fromUserId: String, toUserId: String) = withContext(Dispatchers.IO) {
        val url = URL(URLFormater.getURL("unsubscribeFromUser") ?: throw Exception("Bad URL"))
        val body = JSONObject().apply {
            put("currentUserId", fromUserId)
            put("userIdToUnsubscribe", toUserId)
        }
        postRequest(url, body)
    }

    suspend fun isSubscribed(toUserId: String, fromUserId: String): Boolean = withContext(Dispatchers.IO) {
        val base = URLFormater.getURL("isSubscribed") ?: return@withContext false
        val url = "$base?currentUserId=$fromUserId&targetUserId=$toUserId"
        val response = URL(url).readText()
        JSONObject(response).optBoolean("isSubscribed", false)
    }

    suspend fun fetchSubscriptions(userId: String): List<String> = withContext(Dispatchers.IO) {
        val base = URLFormater.getURL("fetchSubscriptions") ?: return@withContext emptyList()
        val url = "$base?userId=$userId"
        val response = URL(url).readText()
        val json = JSONObject(response).optJSONArray("subscriptions") ?: return@withContext emptyList()
        List(json.length()) { json.getString(it) }
    }

    suspend fun fetchFollowers(userId: String): List<String> = withContext(Dispatchers.IO) {
        val base = URLFormater.getURL("fetchFollowers") ?: return@withContext emptyList()
        val url = "$base?userId=$userId"
        val response = URL(url).readText()
        val json = JSONObject(response).optJSONArray("followers") ?: return@withContext emptyList()
        List(json.length()) { json.getString(it) }
    }

    // Вспоміжна логіка
    private fun updatedEmbedding(
        userEmbedding: List<Float>,
        postEmbedding: List<Float>,
        alpha: Float
    ): List<Float> {
        return userEmbedding.zip(postEmbedding) { u, p -> (1 - alpha) * u + alpha * p }
    }

    private fun postRequest(url: URL, body: JSONObject): JSONObject {
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.outputStream.write(body.toString().toByteArray())
        val response = conn.inputStream.bufferedReader().use { it.readText() }
        return JSONObject(response)
    }
}
