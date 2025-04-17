package com.orestpalii.diploma.data.model

import java.util.Date

data class Post(
    val id: String = "",
    val authorId: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val isAIgenerated: Boolean = false,
    val aiConfidence: Int = 0,
    val tags: String = "",
    val embedding: List<Double>? = null,
    val comments: List<Comment>? = null,
    val likesCount: Int = 0,
    val likedBy: List<String>? = null,
    val createdAt: Double = 0.0,
    val updatedAt: Double? = null
)