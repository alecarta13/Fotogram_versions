package com.example.fotogram.navigator.newPostScreen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fotogram.SessionManager
import com.example.fotogram.api.CreatePostRequest
import com.example.fotogram.api.RetrofitClient
import com.example.fotogram.navigator.NavigationBar
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Composable
fun NewPost(
    modifier: Modifier = Modifier,
    onChangeScreen: (String) -> Unit,
    onChangeTab: (String) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var textDescription by remember { mutableStateOf("") }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var base64Image by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // GESTORE GALLERIA CON RIDIMENSIONAMENTO PROPORZIONALE
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)

                // --- LOGICA DI RIDIMENSIONAMENTO CORRETTA ---
                val maxDimension = 600 // Dimensione massima del lato più lungo
                val originalWidth = originalBitmap.width
                val originalHeight = originalBitmap.height
                var newWidth = originalWidth
                var newHeight = originalHeight

                // Calcola le nuove dimensioni mantenendo le proporzioni
                if (originalWidth > maxDimension || originalHeight > maxDimension) {
                    val ratio = originalWidth.toFloat() / originalHeight.toFloat()
                    if (ratio > 1) {
                        // Orizzontale (Landscape)
                        newWidth = maxDimension
                        newHeight = (maxDimension / ratio).toInt()
                    } else {
                        // Verticale (Portrait) o Quadrata
                        newHeight = maxDimension
                        newWidth = (maxDimension * ratio).toInt()
                    }
                }

                // Crea la nuova bitmap ridimensionata NON DEFORMATA
                selectedImageBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)

                // Compressione JPEG
                val outputStream = ByteArrayOutputStream()
                // Qualità 60 va bene se la dimensione è 600px
                selectedImageBitmap?.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                val byteArray = outputStream.toByteArray()
                base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)

            } catch (e: Exception) {
                Toast.makeText(context, "Errore nel caricamento dell'immagine", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize().background(Color.White).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crea Nuovo Post", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // --- BOX SELEZIONE IMMAGINE ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) // Altezza fissa per il box anteprima
                .background(Color(0xFFF0F0F0))
                .border(1.dp, Color.Gray)
                .clickable { galleryLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageBitmap != null) {
                Image(
                    bitmap = selectedImageBitmap!!.asImageBitmap(),
                    contentDescription = "Anteprima",
                    modifier = Modifier.fillMaxSize(),
                    // Usa ContentScale.Fit per vedere tutta l'immagine senza tagli nell'anteprima
                    contentScale = ContentScale.Fit
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(50.dp), tint = Color.Gray)
                    Text("Tocca per aggiungere foto", color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- DESCRIZIONE ---
        OutlinedTextField(
            value = textDescription,
            onValueChange = { textDescription = it },
            label = { Text("Scrivi una didascalia...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- BOTTONE PUBBLICA ---
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (base64Image == null) {
                        Toast.makeText(context, "Seleziona un'immagine", Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true
                        coroutineScope.launch {
                            val token = sessionManager.fetchSession()
                            if (token != null) {
                                try {
                                    val request = CreatePostRequest(
                                        contentText = textDescription,
                                        contentPicture = base64Image!!,
                                        location = null // Posizione per ora null
                                    )
                                    val response = RetrofitClient.api.createPost(token, request)
                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "Post pubblicato!", Toast.LENGTH_SHORT).show()
                                        onChangeTab("Feed") // Torna al feed dopo la pubblicazione
                                    } else {
                                        Toast.makeText(context, "Errore server: ${response.code()}", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Errore di connessione", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = base64Image != null
            ) {
                Text("PUBBLICA")
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        NavigationBar(modifier = Modifier, page = "NewPost", onChangeScreen = onChangeScreen, onChangeTab = onChangeTab)
    }
}