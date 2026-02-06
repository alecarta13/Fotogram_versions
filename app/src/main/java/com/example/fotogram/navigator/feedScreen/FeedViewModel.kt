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

    private val _followedIds = MutableStateFlow<Set<Int>>(emptySet())
    val followedIds = _followedIds.asStateFlow()

    // 1. CARICAMENTO POST (Con Caching, Fix Nomi e Fix Follow)
    fun loadPosts(sessionId: String) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // A. Scarica ID Bacheca
                val feedResponse = RetrofitClient.api.getFeed(sessionId)

                if (feedResponse.isSuccessful && feedResponse.body() != null) {
                    val serverIds = feedResponse.body()!!

                    // Mappa veloce per controllare la cache
                    val cachedPostsMap = dao.getAllPosts().associateBy { it.id }
                    val newPostsToAdd = mutableListOf<PostDetail>()
                    val authorsToCheck = mutableSetOf<Int>()

                    for (id in serverIds) {
                        // Salta se già visibile
                        if (_posts.value.any { it.id == id }) continue

                        var post: PostDetail? = null

                        // B. Cache vs Rete
                        if (cachedPostsMap.containsKey(id)) {
                            post = cachedPostsMap[id]!!.toPostDetail()
                        } else {
                            val detailResponse = RetrofitClient.api.getPost(id, sessionId)
                            if (detailResponse.isSuccessful && detailResponse.body() != null) {
                                post = detailResponse.body()!!
                                // C. FIX "UTENTE" (Nome mancante)
                                if (post.author.isNullOrEmpty()) {
                                    // Scarica info autore al volo
                                    val authorResp = RetrofitClient.api.getUser(post.authorId, sessionId)
                                    if (authorResp.isSuccessful && authorResp.body() != null) {
                                        val authorUser = authorResp.body()!!
                                        post = post.copy(author = authorUser.username)
                                    }
                                }
                                // Salva in DB
                                dao.insertPost(post.toEntity())
                            }
                        }

                        if (post != null) {
                            newPostsToAdd.add(post)
                            authorsToCheck.add(post.authorId)
                        }
                    }

                    // D. Aggiorna lista post (Paginazione)
                    if (newPostsToAdd.isNotEmpty()) {
                        _posts.value += newPostsToAdd
                    }

                    // E. FIX "SEGUI": Aggiorna lo stato dei follow per gli autori visti
                    // Scarichiamo i dati utente per ogni autore unico per sapere se lo seguiamo
                    val currentFollowed = _followedIds.value.toMutableSet()
                    for (authorId in authorsToCheck) {
                        // Piccola ottimizzazione: se è l'utente loggato, ignoriamo
                        val myId = com.example.fotogram.SessionManager(getApplication()).fetchUserId()
                        if (authorId == myId) continue

                        try {
                            val userResp = RetrofitClient.api.getUser(authorId, sessionId)
                            if (userResp.isSuccessful && userResp.body() != null) {
                                val user = userResp.body()!!
                                if (user.isYourFollowing) {
                                    currentFollowed.add(authorId)
                                } else {
                                    currentFollowed.remove(authorId)
                                }
                            }
                        } catch (e: Exception) { /* Ignora errori rete secondari */ }
                    }
                    _followedIds.value = currentFollowed
                }
            } catch (e: Exception) {
                Log.e("FEED", "Errore feed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshPosts(sessionId: String) {
        _posts.value = emptyList()
        loadPosts(sessionId)
    }

    fun toggleFollow(authorId: Int, sessionId: String) {
        viewModelScope.launch {
            val currentList = _followedIds.value.toMutableSet()
            try {
                if (currentList.contains(authorId)) {
                    val response = RetrofitClient.api.unfollowUser(authorId, sessionId)
                    if (response.isSuccessful) currentList.remove(authorId)
                } else {
                    val response = RetrofitClient.api.followUser(authorId, sessionId)
                    if (response.isSuccessful) currentList.add(authorId)
                }
                _followedIds.value = currentList
            } catch (e: Exception) {
                Log.e("FEED", "Errore toggle follow", e)
            }
        }
    }
}