package com.example.fotogram.navigator.profileScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fotogram.api.PostDetail
import com.example.fotogram.api.RetrofitClient
import com.example.fotogram.api.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    // 1. I dati che mancano al ProfileScreen
    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _userPosts = MutableStateFlow<List<PostDetail>>(emptyList())
    val userPosts = _userPosts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // 2. La funzione che ProfileScreen sta cercando di chiamare
    fun loadUserProfile(userId: Int, sessionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // A. Scarica Dati Utente (Nome, Foto...)
                // Assicurati di aver aggiunto getUser in FotogramApi!
                val userResponse = RetrofitClient.api.getUser(userId, sessionId)
                if (userResponse.isSuccessful) {
                    _userProfile.value = userResponse.body()
                }

                // B. Scarica i Post dell'Utente (usando la tua chiamata esistente)
                val postsIdsResponse = RetrofitClient.api.getUserPosts(userId, sessionId)

                if (postsIdsResponse.isSuccessful && postsIdsResponse.body() != null) {
                    val postIds = postsIdsResponse.body()!!
                    val downloadedPosts = mutableListOf<PostDetail>()

                    // Scarica i dettagli di ogni post trovato
                    for (id in postIds) {
                        val detail = RetrofitClient.api.getPost(id, sessionId)
                        if (detail.isSuccessful && detail.body() != null) {
                            downloadedPosts.add(detail.body()!!)
                        }
                    }
                    _userPosts.value = downloadedPosts
                }

            } catch (e: Exception) {
                Log.e("PROFILE", "Errore: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}