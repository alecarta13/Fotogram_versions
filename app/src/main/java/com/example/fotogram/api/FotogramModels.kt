package com.example.fotogram.api

import com.google.gson.annotations.SerializedName

// 1. Definiamo la scatola "Location"
data class Location(
    val latitude: Double,
    val longitude: Double
)

data class User(
    val id: Int,
    val username: String?,
    val bio: String?,
    @SerializedName("profilePicture", alternate = ["profilePictureBase64Image"])
    val profilePicture: String?,
    val dateOfBirth: String? = null,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val isYourFollowing: Boolean = false,
    val isYourFollower: Boolean = false
)

data class UserResponse(
    val sessionId: String,
    val userId: Int
)

data class PostDetail(
    val id: Int,
    val authorId: Int,
    val author: String?,
    val authorProfilePicture: String?,
    val contentText: String?,
    val contentPicture: String?,
    val createdAt: String?,
    val likeCount: Int = 0,
    val userLiked: Boolean = false,

    // 2. Mappiamo l'oggetto JSON annidato "location"
    val location: Location? = null
) {
    // 3. Getter comodi per mantenere compatibilità col resto del codice
    val lat: Double? get() = location?.latitude
    val lng: Double? get() = location?.longitude
}

data class CreatePostRequest(
    val contentText: String?,
    val contentPicture: String?,

    // 4. Inviamo l'oggetto location annidato
    val location: Location? = null
)

data class UpdateUserRequest(
    val username: String?,
    val bio: String?,
    val dateOfBirth: String? = null
)

data class UpdateImageRequest(
    val base64: String
)

class EmptyRequest()