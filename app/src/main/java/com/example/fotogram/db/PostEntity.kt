package com.example.fotogram.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: Int,
    val authorId: Int,
    val contentText: String?,
    val contentPicture: String?,
    val createdAt: String? // Corretto: ora corrisponde al tuo PostDetail
)