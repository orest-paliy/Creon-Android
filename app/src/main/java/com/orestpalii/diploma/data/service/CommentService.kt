package com.orestpalii.diploma.data.service

import com.orestpalii.diploma.data.model.Comment
import com.orestpalii.diploma.utils.URLFormater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object CommentService {

    suspend fun fetchComments(postId: String): List<Comment> = withContext(Dispatchers.IO) {
        val baseUrl = URLFormater.getURL("fetchcomments") ?: throw IllegalArgumentException("Bad URL")
        val fullUrl = "$baseUrl?postId=$postId"
        val url = URL(fullUrl)

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
        }

        val response = conn.inputStream.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(response)

        return@withContext List(jsonArray.length()) { i ->
            val obj = jsonArray.getJSONObject(i)
            Comment(
                id = obj.optString("id"),
                userId = obj.optString("userId"),
                text = obj.optString("text"),
                createdAt = obj.optDouble("createdAt"),
                likedBy = List(obj.optJSONArray("likedBy")?.length() ?: 0) { j ->
                    obj.optJSONArray("likedBy")?.getString(j) ?: ""
                }
            )
        }
    }

    suspend fun saveComments(comments: List<Comment>, postId: String) = withContext(Dispatchers.IO) {
        val url = URL(URLFormater.getURL("savecomments") ?: throw IllegalArgumentException("Bad URL"))
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
        }

        val jsonArray = JSONArray().apply {
            comments.forEach { comment ->
                val obj = JSONObject().apply {
                    put("id", comment.id)
                    put("userId", comment.userId)
                    put("text", comment.text)
                    put("createdAt", comment.createdAt)
                    put("likedBy", JSONArray(comment.likedBy))
                }
                put(obj)
            }
        }

        val payload = JSONObject().apply {
            put("postId", postId)
            put("comments", jsonArray)
        }

        BufferedOutputStream(conn.outputStream).use { output ->
            OutputStreamWriter(output).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }
        }

        conn.inputStream.close()
    }
}
