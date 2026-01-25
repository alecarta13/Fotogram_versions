package com.example.fotogram.api

data class UserRequest(
    val username: String,
    val picture: String = "R0lGODlhAQABAIAAAAUEBA=="
)

data class UserResponse(
    val sessionId: String,
    val userId: Int
)

// IL SINGOLO POST
data class PostDetail(
    val id: Int,
    val authorId: Int,       // Nello screenshot c'è solo authorId, non username
    val contentText: String?, // Nello screenshot si chiama contentText
    val contentPicture: String?, // Nello screenshot si chiama contentPicture (Base64)
    val createdAt: String?
)

// MODELLO PER CREARE UN NUOVO POST
data class CreatePostRequest(
    val contentText: String,
    val contentPicture: String, // Base64
    val location: LocationData? = null // Facoltativo, ma meglio metterlo
)

data class LocationData(
    val latitude: Double,
    val longitude: Double
)