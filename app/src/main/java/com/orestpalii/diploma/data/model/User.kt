package com.orestpalii.diploma.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val interests: List<String> = emptyList(),
    val embedding: List<Double> = emptyList(),
    val avatarURL: String = "",
    val createdAt: Double = 0.0,
    val subscriptions: List<String>? = null,
    val followers: List<String>? = null
)