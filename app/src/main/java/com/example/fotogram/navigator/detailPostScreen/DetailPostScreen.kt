package com.example.fotogram.navigator.detailPostScreen

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
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
import com.example.fotogram.api.PostDetail
import com.example.fotogram.api.RetrofitClient
import com.example.fotogram.navigator.GoBack
import com.example.fotogram.navigator.NavigationBar

@Composable
fun DetailsPostScreen(
    modifier: Modifier = Modifier,
    postId: Int, // L'ID del post da mostrare
    onChangeScreen: (String) -> Unit,
    tab: String, // "Feed" o "Profile" o "FriendProfile"
    onChangeTab: (String) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var post by remember { mutableStateOf<PostDetail?>(null) }

    // Scarichiamo i dettagli del singolo post
    LaunchedEffect(postId) {
        val token = sessionManager.fetchSession()
        if (token != null) {
            try {
                val response = RetrofitClient.api.getPost(postId, token)
                if (response.isSuccessful) {
                    post = response.body()
                }
            } catch (e: Exception) { }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        // LOGICA DI NAVIGAZIONE CORRETTA
        // Se vengo dal Feed -> Torno al Feed
        // Se vengo dal Profilo -> Torno al Profilo
        val destination = when(tab) {
            "Feed" -> "Feed"
            "Profile" -> "Profile"
            "FriendProfile" -> "FriendProfile" // Ora gestiamo anche questo caso
            else -> "Feed"
        }

        GoBack(
            modifier = Modifier,
            page = "Dettaglio Post",
            goToPage = destination,
            onChangeScreen = onChangeScreen
        )

        // CONTENUTO DEL POST
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            if (post != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Immagine
                    if (post!!.contentPicture != null) {
                        val bitmap = remember(post!!.contentPicture) {
                            try {
                                val bytes = Base64.decode(post!!.contentPicture, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                            } catch (e: Exception) { null }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f), // Quadrata
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Descrizione
                    Text(
                        text = post!!.contentText ?: "",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Text("Caricamento post...")
            }
        }

        NavigationBar(modifier = Modifier, page = "DetailsPost", onChangeScreen = onChangeScreen, onChangeTab = onChangeTab)
    }
}