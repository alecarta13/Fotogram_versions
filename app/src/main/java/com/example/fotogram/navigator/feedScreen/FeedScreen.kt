package com.example.fotogram.navigator.feedScreen

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.fotogram.api.PostDetail
import com.example.fotogram.navigator.NavigationBar

@Composable
fun Feed(
    modifier: Modifier = Modifier,
    onChangeScreen: (String) -> Unit,
    onChangeTab: (String) -> Unit
) {
    val viewModel: FeedViewModel = viewModel()
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // Carica i dati all'avvio
    LaunchedEffect(Unit) {
        val token = sessionManager.fetchSession()
        if (token != null) {
            viewModel.loadPosts(token)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenPlaceholder("Bacheca", modifier = Modifier.height(50.dp), Color.Magenta)

        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (posts.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Nessun post trovato!")
                    Text("Segui qualcuno per vedere i post.")
                }
            } else {
                LazyColumn {
                    items(posts) { post ->
                        PostItem(post = post)
                    }
                }
            }
        }
        NavigationBar(modifier = Modifier, page = "Feed", onChangeScreen = onChangeScreen, onChangeTab = onChangeTab)
    }
}

@Composable
fun PostItem(post: PostDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White)
    ) {
        // Autore (Per ora mostriamo l'ID perché il nome non c'è nel post)
        Text(
            text = "Autore ID: ${post.authorId}",
            modifier = Modifier.padding(16.dp)
        )

        // Immagine
        if (post.contentPicture != null) {
            // Decodifica Base64
            val bitmap = remember(post.contentPicture) {
                try {
                    val decodedBytes = Base64.decode(post.contentPicture, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
                } catch (e: Exception) { null }
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Descrizione
        Text(
            text = post.contentText ?: "",
            modifier = Modifier.padding(16.dp)
        )
    }
}