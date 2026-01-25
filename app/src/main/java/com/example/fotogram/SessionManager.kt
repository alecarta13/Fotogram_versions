package com.example.fotogram

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fotogram_prefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id" // <--- NUOVO
    }

    fun saveSession(token: String, userId: Int) { // <--- MODIFICATA
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.putInt(USER_ID, userId) // <--- SALVIAMO L'ID
        editor.apply()
    }

    fun fetchSession(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    // NUOVA FUNZIONE
    fun fetchUserId(): Int {
        return prefs.getInt(USER_ID, -1) // Ritorna -1 se non trovato
    }

    fun clearSession() {
        val editor = prefs.edit()
        editor.remove(USER_TOKEN)
        editor.remove(USER_ID) // <--- PULIAMO ANCHE L'ID
        editor.apply()
    }
}