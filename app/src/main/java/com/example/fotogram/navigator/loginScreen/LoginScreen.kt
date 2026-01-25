package com.example.fotogram.navigator.loginScreen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fotogram.ScreenPlaceholder
import com.example.fotogram.SessionManager
import com.example.fotogram.api.RetrofitClient
import com.example.fotogram.api.UserRequest
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onChangeScreen: (String) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope() // Serve per lanciare cose in background

    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenPlaceholder("BENVENUTO", modifier = Modifier.height(150.dp), Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        Text("Scegli il tuo nome utente:")

        TextField(
            value = username,
            onValueChange = { if (it.length <= 15) username = it },
            modifier = Modifier.padding(16.dp),
            enabled = !isLoading
        )

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (username.length < 3) {
                        Toast.makeText(context, "Nome troppo corto", Toast.LENGTH_SHORT).show()
                    } else {
                        // INIZIO LA CHIAMATA AL SERVER
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                // 1. Preparo i dati (nome + immagine finta per ora)
                                val request = UserRequest(username = username)

                                // 2. Chiamo il server
                                val response = RetrofitClient.api.registerUser(request)

                                if (response.isSuccessful && response.body() != null) {
                                    val token = response.body()!!.sessionId
                                    val id = response.body()!!.userId

                                    Log.d("LOGIN", "Registrato! Token: $token - ID: $id")

                                    // MODIFICA QUESTA RIGA: Passa anche 'id'
                                    sessionManager.saveSession(token, id)

                                    onChangeScreen("Feed")
                                } else {
                                    // ERRORE DEL SERVER (es. nome duplicato o dati sbagliati)
                                    Log.e("LOGIN", "Errore Server: ${response.code()}")
                                    Toast.makeText(context, "Errore: ${response.code()}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                // ERRORE DI CONNESSIONE (es. niente internet)
                                Log.e("LOGIN", "Errore Rete: ${e.message}")
                                Toast.makeText(context, "Errore di connessione", Toast.LENGTH_LONG).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = username.isNotEmpty()
            ) {
                Text("Entra")
            }
        }
    }
}