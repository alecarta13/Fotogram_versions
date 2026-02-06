package com.example.fotogram.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostEntity>)

    // --- QUESTA È QUELLA CHE MANCAVA ---
    @Query("SELECT * FROM posts ORDER BY id DESC")
    suspend fun getAllPosts(): List<PostEntity>

    @Query("DELETE FROM posts")
    suspend fun clearAll()
}