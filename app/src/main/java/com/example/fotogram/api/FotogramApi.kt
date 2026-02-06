package com.example.fotogram.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface FotogramApi {

    // 1. REGISTRAZIONE: Swagger dice "No parameters", quindi mandiamo una mappa vuota
    @POST("user")
    suspend fun registerUser(@Body empty: Map<String, String> = emptyMap()): Response<UserResponse>

    // 2. RECUPERO UTENTE
    @GET("user/{userId}")
    suspend fun getUser(
        @Path("userId") userId: Int,
        @Header("x-session-id") sessionId: String
    ): Response<User>

    // 3. FEED
    @GET("feed")
    suspend fun getFeed(@Header("x-session-id") sessionId: String): Response<List<Int>>

    @GET("post/{postId}")
    suspend fun getPost(
        @Path("postId") postId: Int,
        @Header("x-session-id") sessionId: String
    ): Response<PostDetail>

    @POST("post")
    suspend fun createPost(
        @Header("x-session-id") sessionId: String,
        @Body request: CreatePostRequest
    ): Response<PostDetail>

    // 4. UPLOAD FOTO (Campo 'base64')
    @PUT("user/image")
    suspend fun uploadProfileImage(
        @Header("x-session-id") sessionId: String,
        @Body request: UpdateImageRequest
    ): Response<Any>

    // 5. AGGIORNAMENTO NOME (Fondamentale dopo la registrazione)
    @PUT("user")
    suspend fun updateUser(
        @Header("x-session-id") sessionId: String,
        @Body request: UpdateUserRequest
    ): Response<User>

    @GET("post/list/{authorId}")
    suspend fun getUserPosts(
        @Path("authorId") userId: Int,
        @Header("x-session-id") sessionId: String
    ): Response<List<Int>>

    @PUT("follow/{targetId}")
    suspend fun followUser(
        @Path("targetId") id: Int,
        @Header("x-session-id") sessionId: String
    ): Response<User>

    @DELETE("follow/{targetId}")
    suspend fun unfollowUser(
        @Path("targetId") id: Int,
        @Header("x-session-id") sessionId: String
    ): Response<User>
}