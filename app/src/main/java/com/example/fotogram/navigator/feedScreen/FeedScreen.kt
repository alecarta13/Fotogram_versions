package com.example.fotogram.navigator.feedScreen

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fotogram.ScreenPlaceholder
import com.example.fotogram.SessionManager
import com.example.fotogram.api.PostDetail
import com.example.fotogram.api.RetrofitClient
import com.example.fotogram.api.User
import com.example.fotogram.navigator.NavigationBar

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    onChangeScreen: (String) -> Unit,
    onChangeTab: (String) -> Unit,
    onUserClick: (Int) -> Unit,
    onPostClick: (Int) -> Unit
) {
    val viewModel: FeedViewModel = viewModel()
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    LaunchedEffect(Unit) {
        val token = sessionManager.fetchSession()
        if (token != null && posts.isEmpty()) { // <--- AGGIUNTA LA CONDIZIONE
            viewModel.loadPosts(token)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenPlaceholder("Bacheca", modifier = Modifier.height(50.dp), Color.Magenta)

        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (posts.isEmpty()) {
                Text(
                    text = "Nessun post trovato!\nSegui qualcuno per vedere i post.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn {
                    items(posts) { post ->
                        PostItem(
                            post = post,
                            onUserClick = onUserClick,
                            onPostClick = onPostClick
                        )
                    }
                }
            }
        }

        // BARRA DI NAVIGAZIONE IN BASSO
        NavigationBar(
            modifier = Modifier,
            page = "Feed",
            onChangeScreen = onChangeScreen,
            onChangeTab = onChangeTab
        )
    }
}

@Composable
fun PostItem(
    post: PostDetail,
    onUserClick: (Int) -> Unit,
    onPostClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // Stato per memorizzare i dati dell'autore (Nome e Foto)
    var authorUser by remember { mutableStateOf<User?>(null) }

    // APPENA IL POST APPARE, SCARICHIAMO I DATI DELL'AUTORE
    LaunchedEffect(post.authorId) {
        val token = sessionManager.fetchSession()
        if (token != null) {
            try {
                val response = RetrofitClient.api.getUser(post.authorId, token)
                if (response.isSuccessful) {
                    authorUser = response.body()
                }
            } catch (e: Exception) {
                // Gestione silenziosa dell'errore
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onPostClick(post.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // --- HEADER DEL POST (Avatar + Nome) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clickable { onUserClick(post.authorId) }, // Clicca per andare al profilo amico
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. FOTO PROFILO AUTORE
                if (authorUser?.profilePicture != null) {
                    val avatarBitmap = remember(authorUser!!.profilePicture) {
                        try {
                            val bytes = Base64.decode(authorUser!!.profilePicture, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                        } catch (e: Exception) { null }
                    }
                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.Gray, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        DefaultAvatar()
                    }
                } else {
                    DefaultAvatar()
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 2. NOME AUTORE
                Text(
                    text = authorUser?.username ?: "Utente #${post.authorId}",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 16.sp
                )
            }

            // --- IMMAGINE DEL POST ---
            if (post.contentPicture != null) {
                val postBitmap = remember(post.contentPicture) {
                    try {
                        val bytes = Base64.decode(post.contentPicture, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                    } catch (e: Exception) { null }
                }

                if (postBitmap != null) {
                    Image(
                        bitmap = postBitmap,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // --- AZIONI (Cuore, Commenti) ---
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                IconButton(onClick = { /* TODO: Like */ }) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Like", tint = Color.Black)
                }
                IconButton(onClick = { /* TODO: Share */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.Black)
                }
            }

            // --- DESCRIZIONE ---
            if (!post.contentText.isNullOrEmpty()) {
                Text(
                    text = post.contentText,
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun DefaultAvatar() {
    Icon(
        imageVector = Icons.Default.AccountCircle,
        contentDescription = null,
        modifier = Modifier.size(40.dp),
        tint = Color.Gray
    )
}