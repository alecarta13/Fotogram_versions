package com.example.fotogram

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fotogram_prefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
    }

    // Salva il token
    fun saveSession(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    // Legge il token (torna null se non c'è)
    fun fetchSession(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    // Cancella il token (Logout)
    fun clearSession() {
        val editor = prefs.edit()
        editor.remove(USER_TOKEN)
        editor.apply()
    }
}