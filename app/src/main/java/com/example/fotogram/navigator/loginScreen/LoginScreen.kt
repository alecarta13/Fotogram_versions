package com.example.fotogram.navigator.loginScreen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fotogram.SessionManager
import com.example.fotogram.api.RetrofitClient
import com.example.fotogram.api.UpdateImageRequest
import com.example.fotogram.api.UpdateUserRequest
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onChangeScreen: (String) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Immagine
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var base64Image by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedImageBitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, true)
                val outputStream = ByteArrayOutputStream()
                selectedImageBitmap?.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val byteArray = outputStream.toByteArray()
                base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            } catch (e: Exception) {
                Toast.makeText(context, "Errore immagine", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Benvenuto su Fotogram", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
                .clickable { galleryLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageBitmap != null) {
                Image(
                    bitmap = selectedImageBitmap!!.asImageBitmap(), contentDescription = "Foto",
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.AccountCircle, "Foto", Modifier.size(60.dp), Color.LightGray)
            }
        }
        Text("Tocca per aggiungere foto", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = username,
            onValueChange = { if (it.length <= 15) username = it },
            label = { Text("Nome Utente") },
            modifier = Modifier.padding(16.dp),
            enabled = !isLoading,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (username.length < 3) {
                        Toast.makeText(context, "Nome troppo corto", Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                // --- STEP 1: REGISTRAZIONE (Crea utente vuoto) ---
                                val response = RetrofitClient.api.registerUser() // Nessun parametro!

                                if (response.isSuccessful && response.body() != null) {
                                    val body = response.body()!!
                                    val token = body.sessionId
                                    val id = body.userId

                                    Log.d("LOGIN", "Utente creato. ID: $id. Ora imposto il nome...")

                                    // --- STEP 2: IMPOSTO IL NOME UTENTE (Fondamentale!) ---
                                    val updateNameReq = UpdateUserRequest(username = username, bio = "Ciao, uso Fotogram!",dateOfBirth = null)
                                    val updateResponse = RetrofitClient.api.updateUser(token, updateNameReq)

                                    if (!updateResponse.isSuccessful) {
                                        Log.e("LOGIN", "Errore salvataggio nome: ${updateResponse.code()}")
                                    }

                                    // --- STEP 3: IMPOSTO LA FOTO (Se c'è) ---
                                    if (base64Image != null) {
                                        try {
                                            // Ora usiamo il campo 'base64' corretto
                                            val imageReq = UpdateImageRequest(base64 = base64Image!!)
                                            RetrofitClient.api.uploadProfileImage(token, imageReq)
                                        } catch (e: Exception) {
                                            Log.e("LOGIN", "Errore foto", e)
                                        }
                                    }

                                    // --- FINE: SALVA E ENTRA ---
                                    sessionManager.saveSession(token, id, username)
                                    onChangeScreen("Feed")

                                } else {
                                    Toast.makeText(context, "Errore Creazione: ${response.code()}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Errore connessione", Toast.LENGTH_LONG).show()
                                Log.e("LOGIN", "Crash", e)
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = username.isNotEmpty()
            ) {
                Text("Crea Profilo ed Entra")
            }
        }
    }
}