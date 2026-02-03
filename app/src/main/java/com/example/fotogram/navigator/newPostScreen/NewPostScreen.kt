package com.example.fotogram.navigator.newPostScreen

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    val token = remember { sessionManager.fetchSession() }
    val scope = rememberCoroutineScope()

    var description by remember { mutableStateOf("") }
    var base64Image by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // In NewPostScreen.kt

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val inputStream = context.contentResolver.openInputStream(uri)
            // 1. Carica solo le dimensioni per capire quanto è grande
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // 2. Calcola quanto rimpicciolire (es. max 1024 pixel)
            val MAX_SIZE = 1024
            var scale = 1
            while (options.outWidth / scale > MAX_SIZE || options.outHeight / scale > MAX_SIZE) {
                scale *= 2
            }

            // 3. Carica l'immagine ridimensionata
            val options2 = BitmapFactory.Options().apply { inSampleSize = scale }
            val inputStream2 = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream2, null, options2)
            inputStream2?.close()

            // 4. Comprimi e Converti in Base64
            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
                val byteArray = outputStream.toByteArray()
                base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            }
        }
    }

    // USIAMO SCAFFOLD PER POSIZIONARE LA BARRA CORRETTAMENTE
    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier, // Niente padding qui!
                page = "NewPost",
                onChangeScreen = onChangeScreen,
                onChangeTab = onChangeTab
            )
        }
    ) { innerPadding -> // Questo padding serve a non finire sotto la barra

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding) // Applichiamo il padding dello Scaffold
                .padding(16.dp),       // Applichiamo il padding estetico (16dp) SOLO al contenuto
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Crea un nuovo post", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(24.dp))

            // Box per l'immagine (Placeholder o Immagine selezionata)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color.LightGray)
                    .run {
                        // Se non c'è immagine, rendiamo cliccabile tutto il box per aggiungerla
                        if (base64Image == null) this // oppure aggiungi .clickable { launcher.launch("image/*") }
                        else this
                    },
                contentAlignment = Alignment.Center
            ) {
                if (base64Image != null) {
                    val bytes = Base64.decode(base64Image, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Button(onClick = { launcher.launch("image/*") }) {
                        Text("Seleziona Foto")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo di testo per la descrizione
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Scrivi una didascalia...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bottone Pubblica
            Button(
                onClick = {
                    if (token != null && base64Image != null) {
                        scope.launch {
                            isLoading = true
                            try {
                                val request = CreatePostRequest(
                                    contentText = description,
                                    contentPicture = base64Image!!,
                                    location = null
                                )
                                val response = RetrofitClient.api.createPost(token, request)
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Post pubblicato!", Toast.LENGTH_SHORT).show()
                                    onChangeTab("Feed")
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
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = base64Image != null && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("PUBBLICA")
                }
            }
        }
    }
}