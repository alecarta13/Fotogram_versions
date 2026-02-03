package com.example.fotogram.navigator.feedScreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fotogram.api.PostDetail
import com.example.fotogram.api.RetrofitClient
import com.example.fotogram.db.AppDatabase
import com.example.fotogram.db.toEntity
import com.example.fotogram.db.toPostDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.postDao()

    private val _posts = MutableStateFlow<List<PostDetail>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // 1. STATO PER GLI AMICI SEGUITI (Set è più veloce di List per cercare)
    private val _followedIds = MutableStateFlow<Set<Int>>(emptySet())
    val followedIds = _followedIds.asStateFlow()

    fun loadPosts(sessionId: String) {
        viewModelScope.launch {
            if (_isLoading.value) return@launch
            _isLoading.value = true
            val currentList = mutableListOf<PostDetail>()

            try {
                // Scarica il Feed (Lista di ID)
                val feedResponse = RetrofitClient.api.getFeed(sessionId)
                if (feedResponse.isSuccessful && feedResponse.body() != null) {
                    val postIds = feedResponse.body()!!
                    for (id in postIds) {
                        // A. Cerco nel DB Locale
                        val cachedPost = dao.getPostById(id)

                        if (cachedPost != null) {
                            currentList.add(cachedPost.toPostDetail())
                            _posts.value = currentList.toList()
                        } else {
                            // B. Scarico da Rete
                            val detailResponse = RetrofitClient.api.getPost(id, sessionId)
                            if (detailResponse.isSuccessful && detailResponse.body() != null) {
                                val newPost = detailResponse.body()!!
                                // C. Salvo nel DB
                                dao.insertPost(newPost.toEntity())
                                currentList.add(newPost)
                                _posts.value = currentList.toList()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FEED", "Errore: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshPosts(sessionId: String) {
        _posts.value = emptyList()
        loadPosts(sessionId)
    }

    // --- NUOVE FUNZIONI PER GESTIRE GLI AMICI ---

    // 2. SCARICA LISTA SEGUITI (Da chiamare all'avvio della schermata)
    fun loadFollowedUsers(userId: Int, sessionId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getFollowed(userId, sessionId)
                if (response.isSuccessful && response.body() != null) {
                    // Mappiamo la lista di utenti in un Set di soli ID
                    _followedIds.value = response.body()!!.map { it.id }.toSet()
                    Log.d("FEED", "Segui ${response.body()!!.size} utenti: ${_followedIds.value}")
                }
            } catch (e: Exception) {
                Log.e("FEED", "Errore scaricamento seguiti", e)
            }
        }
    }

    // 3. SEGUI / SMETTI DI SEGUIRE
    fun toggleFollow(authorId: Int, sessionId: String) {
        viewModelScope.launch {
            val currentList = _followedIds.value.toMutableSet()

            try {
                if (currentList.contains(authorId)) {
                    // SE LO SEGUO GIÀ -> SMETTI DI SEGUIRE (UNFOLLOW)
                    val response = RetrofitClient.api.unfollowUser(authorId, sessionId)
                    if (response.isSuccessful) {
                        currentList.remove(authorId)
                        Log.d("FEED", "Unfollowed user $authorId")
                    }
                } else {
                    // NON LO SEGUO -> INIZIA A SEGUIRE (FOLLOW)
                    val response = RetrofitClient.api.followUser(authorId, sessionId)
                    if (response.isSuccessful) {
                        currentList.add(authorId)
                        Log.d("FEED", "Followed user $authorId")
                    }
                }
                // Aggiorna lo stato per far cambiare il colore del bordo nella UI
                _followedIds.value = currentList

            } catch (e: Exception) {
                Log.e("FEED", "Errore toggle follow", e)
            }
        }
    }
}