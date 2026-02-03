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
    // Crea un nuovo utente
    @POST("user")
    suspend fun registerUser(@Body request: UserRequest): Response<UserResponse>

    @GET("user/{userId}")
    suspend fun getUser(
        @Path("userId") userId: Int,
        @Header("x-session-id") sessionId: String
    ): Response<User>

    // Scarica la lista ID dei post dell'utente loggato
    @GET("feed")
    suspend fun getFeed(@Header("x-session-id") sessionId: String): Response<List<Int>>

    // Scarica i dettagli di un post
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

    @PUT("user/image")
    suspend fun uploadProfileImage(
        @Header("x-session-id") sessionId: String,
        @Body request: UpdateImageRequest
    ): Response<Any> // Mettiamo Any perché spesso risponde vuoto o con messaggio

    //Scarica la lista ID dei post di un utente
    @GET("post/list/{authorId}")
    suspend fun getUserPosts(
        @Path("authorId") userId: Int,
        @Header("x-session-id") sessionId: String
    ): Response<List<Int>>


    @PUT("users/{id}/follow")
    suspend fun followUser(@Path("id") id: Int, @Header("x-session-id") sessionId: String): Response<User>

    @DELETE("users/{id}/follow")
    suspend fun unfollowUser(@Path("id") id: Int, @Header("x-session-id") sessionId: String): Response<User> // A volte ritorna Void o User

    // Spesso c'è una chiamata per vedere chi segui, es:
    @GET("users/{id}/followed")
    suspend fun getFollowed(@Path("id") id: Int, @Header("x-session-id") sessionId: String): Response<List<User>>
}

