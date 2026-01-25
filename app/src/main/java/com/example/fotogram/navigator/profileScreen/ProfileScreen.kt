package com.example.fotogram.navigator.profileScreen

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fotogram.ScreenPlaceholder
import com.example.fotogram.SessionManager
import com.example.fotogram.navigator.NavigationBar

@Composable
fun Profile(
    modifier: Modifier = Modifier,
    onChangeScreen: (String) -> Unit,
    onChangeTab: (String) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: ProfileViewModel = viewModel()

    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Carichiamo i dati all'avvio
    LaunchedEffect(Unit) {
        val token = sessionManager.fetchSession()
        val myUserId = sessionManager.fetchUserId()

        if (token != null && myUserId != -1) {
            viewModel.loadUserPosts(token, myUserId)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenPlaceholder("Il Mio Profilo", modifier = Modifier.height(50.dp), Color.Blue)

        // TASTO LOGOUT
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.CenterEnd) {
            Button(onClick = {
                sessionManager.clearSession()
                onChangeScreen("Login")
            }) {
                Text("Logout")
            }
        }

        // GRIGLIA FOTO
        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (posts.isEmpty()) {
                Text("Non hai ancora pubblicato nulla.", Modifier.align(Alignment.Center))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // 2 Colonne
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(posts) { post ->
                        if (post.contentPicture != null) {
                            val bitmap = remember(post.contentPicture) {
                                try {
                                    val bytes = Base64.decode(post.contentPicture, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                                } catch (e: Exception) { null }
                            }

                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .aspectRatio(1f) // Quadrato
                                        .border(1.dp, Color.LightGray),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }

        NavigationBar(modifier = Modifier, page = "Profile", onChangeScreen = onChangeScreen, onChangeTab = onChangeTab)
    }
}