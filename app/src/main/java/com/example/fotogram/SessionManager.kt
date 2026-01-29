package com.example.fotogram

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fotogram_prefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
        const val USER_NAME = "user_name"
    }

    fun saveSession(token: String, userId: Int, username: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.putInt(USER_ID, userId)
        editor.putString(USER_NAME, username)
        editor.apply()
    }

    fun fetchSession(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    // NUOVA FUNZIONE
    fun fetchUserId(): Int {
        return prefs.getInt(USER_ID, -1) // Ritorna -1 se non trovato
    }

    // Funzione per recuperare il nome
    fun fetchUserName(): String? {
        return prefs.getString(USER_NAME, "Utente")
    }

    fun clearSession() {
        val editor = prefs.edit()
        editor.remove(USER_TOKEN)
        editor.remove(USER_ID)
        editor.apply()
    }
}