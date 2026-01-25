package com.example.fotogram.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface FotogramApi {
    @POST("user")
    suspend fun registerUser(@Body request: UserRequest): Response<UserResponse>

    @GET("feed")
    suspend fun getFeed(@Header("x-session-id") sessionId: String): Response<List<Int>>

    @GET("post/{postId}")
    suspend fun getPost(
        @Path("postId") postId: Int,
        @Header("x-session-id") sessionId: String
    ): Response<PostDetail>

    // CREA UN NUOVO POST
    @POST("post")
    suspend fun createPost(
        @Header("x-session-id") sessionId: String,
        @Body request: CreatePostRequest
    ): Response<PostDetail> // Il server ci risponde con il post appena creato

    // NUOVA: Scarica la lista ID dei post di un utente
    @GET("post/list/{authorId}")
    suspend fun getUserPosts(
        @Path("authorId") userId: Int,
        @Header("x-session-id") sessionId: String
    ): Response<List<Int>>
}