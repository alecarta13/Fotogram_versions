package com.example.fotogram.db

import com.example.fotogram.api.PostDetail
import com.example.fotogram.api.Location

fun PostDetail.toEntity(): PostEntity {
    return PostEntity(
        id = this.id,
        authorId = this.authorId,
        author = this.author,
        authorProfilePicture = this.authorProfilePicture,
        contentText = this.contentText,
        contentPicture = this.contentPicture,
        createdAt = this.createdAt,
        // Qui usiamo i getter "ponte" che abbiamo creato in PostDetail
        lat = this.lat,
        lng = this.lng
    )
}

fun PostEntity.toPostDetail(): PostDetail {
    // 1. Ricostruiamo l'oggetto Location se i dati esistono nel DB
    val locationData = if (this.lat != null && this.lng != null) {
        Location(this.lat, this.lng)
    } else {
        null
    }

    return PostDetail(
        id = this.id,
        authorId = this.authorId,
        author = this.author,
        authorProfilePicture = this.authorProfilePicture,
        contentText = this.contentText,
        contentPicture = this.contentPicture,
        createdAt = this.createdAt,
        // 2. Passiamo l'oggetto location al costruttore
        location = locationData
    )
}