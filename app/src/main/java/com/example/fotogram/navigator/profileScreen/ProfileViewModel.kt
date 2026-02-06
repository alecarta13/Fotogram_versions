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

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _userPosts = MutableStateFlow<List<PostDetail>>(emptyList())
    val userPosts = _userPosts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Stati derivati direttamente dall'User
    private val _isFollowing = MutableStateFlow(false)
    val isFollowing = _isFollowing.asStateFlow()

    private val _followerCount = MutableStateFlow(0)
    val followerCount = _followerCount.asStateFlow()

    private val _followingCount = MutableStateFlow(0)
    val followingCount = _followingCount.asStateFlow()

    private val _postsCount = MutableStateFlow(0)
    val postsCount = _postsCount.asStateFlow()


    fun loadUserProfile(userId: Int, sessionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. SCARICA PROFILO
                val userResponse = RetrofitClient.api.getUser(userId, sessionId)

                if (userResponse.isSuccessful && userResponse.body() != null) {
                    val user = userResponse.body()!!
                    _userProfile.value = user

                    // AGGIORNO I DATI DELLA UI
                    _followingCount.value = user.followingCount
                    _isFollowing.value = user.isYourFollowing
                    _postsCount.value = user.postsCount

                    // --- FIX FOLLOWER FANTASMA ---
                    // Se l'utente ha 0 post e il server dice 1 follower, forziamo a 0.
                    // Altrimenti usiamo il dato reale del server.
                    if (user.postsCount == 0 && user.followersCount == 1) {
                        _followerCount.value = 0
                    } else {
                        _followerCount.value = user.followersCount
                    }
                    // -----------------------------
                }

                // 2. SCARICA I POST
                val postsIdsResponse = RetrofitClient.api.getUserPosts(userId, sessionId)
                if (postsIdsResponse.isSuccessful && postsIdsResponse.body() != null) {
                    val postIds = postsIdsResponse.body()!!
                    val downloadedPosts = mutableListOf<PostDetail>()
                    for (id in postIds) {
                        val detail = RetrofitClient.api.getPost(id, sessionId)
                        if (detail.isSuccessful && detail.body() != null) {
                            downloadedPosts.add(detail.body()!!)
                        }
                    }
                    _userPosts.value = downloadedPosts
                }

            } catch (e: Exception) {
                Log.e("PROFILE", "Errore caricamento: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFollow(targetUserId: Int, sessionId: String) {
        viewModelScope.launch {
            try {
                if (_isFollowing.value) {
                    // UNFOLLOW
                    val response = RetrofitClient.api.unfollowUser(targetUserId, sessionId)
                    if (response.isSuccessful) {
                        _isFollowing.value = false
                        _followerCount.value = (_followerCount.value - 1).coerceAtLeast(0)
                    }
                } else {
                    // FOLLOW
                    val response = RetrofitClient.api.followUser(targetUserId, sessionId)
                    if (response.isSuccessful) {
                        _isFollowing.value = true
                        _followerCount.value += 1
                    }
                }
            } catch (e: Exception) {
                Log.e("PROFILE", "Errore toggle follow: ${e.message}")
            }
        }
    }
}