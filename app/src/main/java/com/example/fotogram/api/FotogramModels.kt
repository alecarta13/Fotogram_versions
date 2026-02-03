package com.example.fotogram.api

data class UserRequest(
    val username: String,
    val profilePicture: String = "R0lGODlhAQABAIAAAAUEBA=="
)

data class UserResponse(
    val sessionId: String,
    val userId: Int
)


data class PostDetail(
    val id: Int,
    val authorId: Int,
    val contentText: String?,
    val contentPicture: String?,
    val createdAt: String?
)

// MODELLO PER CREARE UN NUOVO POST
data class CreatePostRequest(
    val contentText: String,
    val contentPicture: String, // Base64
    val location: LocationData? = null
)

data class LocationData(
    val latitude: Double,
    val longitude: Double
)

data class UpdateImageRequest(
    val base64: String // Base64
)

data class User(
    val id: Int,
    val username: String,
    val profilePicture: String? // La foto profilo in Base64
)