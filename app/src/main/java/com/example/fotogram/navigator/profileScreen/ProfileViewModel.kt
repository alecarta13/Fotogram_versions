package com.example.fotogram.navigator.profileScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fotogram.api.PostDetail
import com.example.fotogram.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val _posts = MutableStateFlow<List<PostDetail>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadUserPosts(token: String, userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Chiediamo la lista ID dei post dell'utente
                val response = RetrofitClient.api.getUserPosts(userId, token)

                if (response.isSuccessful && response.body() != null) {
                    val postIds = response.body()!!
                    val downloadedPosts = mutableListOf<PostDetail>()

                    // 2. Scarichiamo i dettagli per ogni ID
                    for (id in postIds) {
                        val detailResponse = RetrofitClient.api.getPost(id, token)
                        if (detailResponse.isSuccessful && detailResponse.body() != null) {
                            downloadedPosts.add(detailResponse.body()!!)
                        }
                    }
                    _posts.value = downloadedPosts
                }
            } catch (e: Exception) {
                Log.e("PROFILE", "Errore: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}