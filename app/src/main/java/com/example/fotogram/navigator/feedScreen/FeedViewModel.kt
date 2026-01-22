package com.example.fotogram.navigator.feedScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fotogram.api.PostDetail
import com.example.fotogram.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {
    // La lista dei post da mostrare
    private val _posts = MutableStateFlow<List<PostDetail>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadPosts(sessionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("FEED", "1. Chiedo gli ID alla bacheca...")
                // 1. Scarica la lista di ID
                val feedResponse = RetrofitClient.api.getFeed(sessionId)

                if (feedResponse.isSuccessful && feedResponse.body() != null) {
                    //val postIds = feedResponse.body()!! // Es: [12, 15, 20]
                    // Fai una copia modificabile della lista
                    val postIds = feedResponse.body()!!.toMutableList()

                    // --- TRUCCO PER IL TEST ---
                    // Se la lista è vuota (o sempre), aggiungiamo noi dei numeri a mano
                    // così vediamo se scarica le immagini.
                    if (postIds.isEmpty()) {
                        Log.d("FEED", "Lista vuota! Aggiungo ID di test...")
                        postIds.add(1)   // ID 1 esiste quasi sempre
                        postIds.add(2)
                        postIds.add(100)
                        postIds.add(155)
                    }

                    Log.d("FEED", "Scarico post ID: $postIds")
                    Log.d("FEED", "ID ricevuti: $postIds")

                    val downloadedPosts = mutableListOf<PostDetail>()

                    // 2. Scarica i dettagli per ogni ID
                    for (id in postIds) {
                        Log.d("FEED", "Scarico post numero $id...")
                        val detailResponse = RetrofitClient.api.getPost(id, sessionId)
                        if (detailResponse.isSuccessful && detailResponse.body() != null) {
                            downloadedPosts.add(detailResponse.body()!!)
                        }
                    }

                    _posts.value = downloadedPosts
                } else {
                    Log.e("FEED", "Errore nel feed: ${feedResponse.code()}")
                }
            } catch (e: Exception) {
                Log.e("FEED", "Errore di connessione: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}