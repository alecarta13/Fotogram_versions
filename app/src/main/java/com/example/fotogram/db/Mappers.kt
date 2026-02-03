package com.example.fotogram.db

import com.example.fotogram.api.PostDetail

fun PostDetail.toEntity(): PostEntity {
    return PostEntity(
        id = this.id,
        authorId = this.authorId,
        contentText = this.contentText,
        contentPicture = this.contentPicture,
        createdAt = this.createdAt
    )
}

fun PostEntity.toPostDetail(): PostDetail {
    return PostDetail(
        id = this.id,
        authorId = this.authorId,
        contentText = this.contentText,
        contentPicture = this.contentPicture,
        createdAt = this.createdAt
    )
}