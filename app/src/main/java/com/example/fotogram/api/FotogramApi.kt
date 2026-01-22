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
}