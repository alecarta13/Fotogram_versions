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
import androidx.compose.ui.unit.dp
import com.example.fotogram.ScreenPlaceholder
import com.example.fotogram.SessionManager
import com.example.fotogram.api.RetrofitClient
import com.example.fotogram.api.UpdateImageRequest
import com.example.fotogram.api.UserRequest
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

    // Variabili per l'immagine
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var base64Image by remember { mutableStateOf<String?>(null) }

    // Gestore Galleria (Uguale a NewPost)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                // Ridimensioniamo
                selectedImageBitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, true)

                // Convertiamo in Base64
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
        Text("Benvenuto su Fotogram", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        // --- CERCHIO FOTO PROFILO ---
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
                .clickable { galleryLauncher.launch("image/*") }, // Clicca per cambiare
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageBitmap != null) {
                Image(
                    bitmap = selectedImageBitmap!!.asImageBitmap(),
                    contentDescription = "Foto scelta",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Icona di default se non c'è foto
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Aggiungi foto",
                    modifier = Modifier.size(50.dp),
                    tint = Color.Gray
                )
            }
        }
        Text("Tocca per aggiungere una foto", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.height(32.dp))

        // --- INPUT NOME ---
        TextField(
            value = username,
            onValueChange = { if (it.length <= 15) username = it },
            label = { Text("Nome Utente") },
            modifier = Modifier.padding(16.dp),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- BOTTONE REGISTRAZIONE ---
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
                                // 1. REGISTRAZIONE UTENTE
                                val request = UserRequest(username = username)
                                val response = RetrofitClient.api.registerUser(request)

                                if (response.isSuccessful && response.body() != null) {
                                    val token = response.body()!!.sessionId
                                    val id = response.body()!!.userId
                                    Log.d("LOGIN", "Registrato: $token")

                                    // 2. SE HAI SCELTO UNA FOTO, CARICALA SUBITO
                                    if (base64Image != null) {
                                        try {
                                            val imageRequest = UpdateImageRequest(base64 = base64Image!!)
                                            val imgResponse = RetrofitClient.api.uploadProfileImage(token, imageRequest)
                                            if (!imgResponse.isSuccessful) {
                                                Log.e("LOGIN", "Errore upload foto: ${imgResponse.code()}")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("LOGIN", "Errore crash foto: ${e.message}")
                                        }
                                    }

                                    // 3. SALVA E VAI AL FEED
                                    sessionManager.saveSession(token, id, username)
                                    onChangeScreen("Feed")

                                } else {
                                    Toast.makeText(context, "Errore Reg: ${response.code()}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Errore di connessione", Toast.LENGTH_LONG).show()
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