/*package com.example.fotogram.navigator.newPostScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.fotogram.ScreenPlaceholder
import com.example.fotogram.navigator.NavigationBar

@Composable
fun NewPost(
    modifier: Modifier = Modifier,
    onChangeScreen: (String) -> Unit,
    onChangeTab: (String) -> Unit,
) {

    Column {
        ScreenPlaceholder("NewPost", modifier = modifier.weight(1f), Color.Green)
        NavigationBar(modifier = modifier, page = "NewPost", onChangeScreen = onChangeScreen, onChangeTab = onChangeTab)
    }
}*/

package com.example.fotogram.navigator.newPostScreen

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fotogram.ScreenPlaceholder
import com.example.fotogram.SessionManager
import com.example.fotogram.api.CreatePostRequest
import com.example.fotogram.api.LocationData
import com.example.fotogram.api.RetrofitClient
import com.example.fotogram.navigator.NavigationBar
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Composable
fun NewPost(
    modifier: Modifier = Modifier,
    onChangeScreen: (String) -> Unit,
    onChangeTab: (String) -> Unit,
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var textDescription by remember { mutableStateOf("") }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var base64Image by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // GESTORE DELLA GALLERIA
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                // Ridimensioniamo per evitare file giganti
                selectedImageBitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true)

                // Convertiamo in Base64 per il server
                val outputStream = ByteArrayOutputStream()
                selectedImageBitmap?.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val byteArray = outputStream.toByteArray()
                base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            } catch (e: Exception) {
                Toast.makeText(context, "Errore immagine", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        ScreenPlaceholder("Crea Post", modifier = Modifier.height(50.dp), Color.Cyan)

        Spacer(modifier = Modifier.height(16.dp))

        // BOX IMMAGINE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .border(1.dp, Color.Gray)
                .clickable { galleryLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageBitmap != null) {
                Image(
                    bitmap = selectedImageBitmap!!.asImageBitmap(),
                    contentDescription = "Foto",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("Clicca per scegliere una foto")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BOX TESTO
        TextField(
            value = textDescription,
            onValueChange = { if (it.length <= 100) textDescription = it },
            label = { Text("Descrizione...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // BOTTONE PUBBLICA
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedImageBitmap != null && textDescription.isNotEmpty(),
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        val token = sessionManager.fetchSession()
                        if (token != null && base64Image != null) {
                            try {
                                // 1. PREPARIAMO I DATI
                                val request = CreatePostRequest(
                                    contentText = textDescription,
                                    contentPicture = base64Image!!,
                                    location = LocationData(45.0, 9.0) // Posizione fissa
                                )

                                // 2. CHIAMIAMO IL SERVER
                                val response = RetrofitClient.api.createPost(token, request)

                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Post Pubblicato!", Toast.LENGTH_LONG).show()
                                    // 3. TORNIAMO ALLA BACHECA
                                    onChangeScreen("Feed")
                                } else {
                                    Toast.makeText(context, "Errore: ${response.code()}", Toast.LENGTH_LONG).show()
                                    Log.e("NEWPOST", "Err: ${response.errorBody()?.string()}")
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Errore di rete", Toast.LENGTH_LONG).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                }
            ) {
                Text("PUBBLICA")
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        NavigationBar(modifier = Modifier, page = "NewPost", onChangeScreen = onChangeScreen, onChangeTab = onChangeTab)
    }
}