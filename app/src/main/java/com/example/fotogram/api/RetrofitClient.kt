package com.example.fotogram.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // L'indirizzo base del server del progetto
    private const val BASE_URL = "https://develop.ewlab.di.unimi.it/mc/2526/"

    val api: FotogramApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FotogramApi::class.java)
    }
}