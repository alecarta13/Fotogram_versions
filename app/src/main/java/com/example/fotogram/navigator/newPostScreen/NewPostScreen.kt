package com.example.fotogram.navigator.newPostScreen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fotogram.SessionManager
import com.example.fotogram.api.CreatePostRequest
import com.example.fotogram.api.Location
import com.example.fotogram.api.RetrofitClient
import com.example.fotogram.navigator.NavigationBar
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun NewPost(
    modifier: Modifier = Modifier,
    onChangeScreen: (String) -> Unit,
    onChangeTab: (String) -> Unit,
    currentDescription: String,
    onDescriptionChange: (String) -> Unit,
    currentImage: String?,
    onImageChange: (String) -> Unit,
    selectedLat: Double?,
    selectedLng: Double?,
    onOpenMapSelector: () -> Unit,
    onPostSuccess: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val token = remember { sessionManager.fetchSession() }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                // Ridimensionamento immagine (requisito 80KB max base64 circa)
                val maxDimension = 600
                val ratio = Math.min(
                    maxDimension.toDouble() / originalBitmap.width,
                    maxDimension.toDouble() / originalBitmap.height
                )
                val width = (ratio * originalBitmap.width).toInt()
                val height = (ratio * originalBitmap.height).toInt()
                val scaled = Bitmap.createScaledBitmap(originalBitmap, width, height, true)
                val outputStream = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                onImageChange(Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP))
            } catch (e: Exception) {
                Toast.makeText(context, "Errore immagine", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        // HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Text("Nuovo Post", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // SELETTORE IMMAGINE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color(0xFFF5F5F5))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (currentImage != null) {
                    val bitmap = remember(currentImage) {
                        try {
                            val bytes = Base64.decode(currentImage, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                        } catch (e: Exception) { null }
                    }
                    if (bitmap != null) {
                        Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tocca per aggiungere foto", color = Color.Gray)
                        Text("(Max 600px)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // CAMPO TESTO CON LIMITE 100 CARATTERI
            OutlinedTextField(
                value = currentDescription,
                onValueChange = {
                    // Blocco input se supera 100 caratteri
                    if (it.length <= 100) {
                        onDescriptionChange(it)
                    }
                },
                label = { Text("Scrivi una didascalia...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                // Mostra il contatore "0/100" in modo elegante e integrato (no modifiche estetiche invasive)
                supportingText = {
                    Text(
                        text = "${currentDescription.length}/100",
                        color = if (currentDescription.length == 100) Color.Red else Color.Gray
                    )
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // SELETTORE POSIZIONE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenMapSelector() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                if (selectedLat != null && selectedLng != null) {
                    val latStr = String.format("%.4f", selectedLat)
                    val lngStr = String.format("%.4f", selectedLng)
                    Text("Posizione: $latStr, $lngStr", fontWeight = FontWeight.Bold)
                } else {
                    Text("Aggiungi Posizione (Opzionale)", color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // BOTTONE PUBBLICA
            Button(
                onClick = {
                    if (token != null && currentImage != null) {
                        scope.launch {
                            isLoading = true
                            try {
                                val locationData = if (selectedLat != null && selectedLng != null) {
                                    Location(selectedLat, selectedLng)
                                } else null

                                val request = CreatePostRequest(
                                    contentText = currentDescription,
                                    contentPicture = currentImage,
                                    location = locationData
                                )
                                val response = RetrofitClient.api.createPost(token, request)
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Post pubblicato!", Toast.LENGTH_SHORT).show()
                                    onPostSuccess()
                                } else {
                                    val errorMsg = response.errorBody()?.string() ?: "Errore sconosciuto"
                                    if (errorMsg.contains("too large")) {
                                        Toast.makeText(context, "Immagine troppo grande!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Errore: $errorMsg", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Errore connessione", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = currentImage != null && !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("PUBBLICA", fontWeight = FontWeight.Bold)
            }
        }
        NavigationBar(page = "NewPost", onChangeScreen = onChangeScreen, onChangeTab = onChangeTab)
    }
}