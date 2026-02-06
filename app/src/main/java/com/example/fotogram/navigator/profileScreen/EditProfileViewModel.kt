package com.example.fotogram.navigator.profileScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fotogram.api.RetrofitClient
import com.example.fotogram.api.UpdateImageRequest
import com.example.fotogram.api.UpdateUserRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditProfileViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess = _updateSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // CAMPI MODIFICABILI
    var currentUsername = MutableStateFlow("")
    var currentBio = MutableStateFlow("")

    // Ora la data è modificabile dall'UI
    var currentDateOfBirth = MutableStateFlow<String?>(null)

    var newProfileImageBase64: String? = null

    fun loadCurrentProfile(userId: Int, sessionId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getUser(userId, sessionId)
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    currentUsername.value = user.username ?: ""
                    currentBio.value = user.bio ?: ""
                    // Carichiamo la data dal server
                    currentDateOfBirth.value = user.dateOfBirth
                }
            } catch (e: Exception) {
                Log.e("EDIT_PROFILE", "Errore caricamento", e)
            }
        }
    }

    fun saveChanges(sessionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // 1. UPDATE TESTI
                // Se la data è null/vuota, proviamo a mandare un default per evitare errori del server
                val dateToSend = if (currentDateOfBirth.value.isNullOrBlank()) "2000-01-01" else currentDateOfBirth.value

                val textRequest = UpdateUserRequest(
                    username = currentUsername.value,
                    bio = currentBio.value,
                    dateOfBirth = dateToSend
                )

                val textResponse = RetrofitClient.api.updateUser(sessionId, textRequest)

                // 2. UPDATE FOTO (Solo se c'è)
                var imageSuccess = true
                if (newProfileImageBase64 != null) {
                    val imageRequest = UpdateImageRequest(base64 = newProfileImageBase64!!)
                    val imgResp = RetrofitClient.api.uploadProfileImage(sessionId, imageRequest)
                    if (!imgResp.isSuccessful) imageSuccess = false
                }

                if (textResponse.isSuccessful && imageSuccess) {
                    _updateSuccess.value = true
                } else {
                    val errorBody = textResponse.errorBody()?.string() ?: "Errore sconosciuto"
                    _errorMessage.value = "Errore: ${textResponse.code()} - $errorBody"
                }

            } catch (e: Exception) {
                _errorMessage.value = "Errore di connessione: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _updateSuccess.value = false
        _errorMessage.value = null
        newProfileImageBase64 = null
    }
}