package com.orestpalii.diploma.data.model

data class Comment(
    val id: String = "",
    val userId: String = "",
    val text: String = "",
    val createdAt: Double = 0.0,
    val likedBy: List<String> = emptyList()
)