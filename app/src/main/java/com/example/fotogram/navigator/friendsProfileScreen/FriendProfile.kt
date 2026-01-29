package com.example.fotogram.navigator.friendProfile

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable // <--- IMPORTANTE PER IL CLICK
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fotogram.SessionManager
import com.example.fotogram.api.RetrofitClient
import com.example.fotogram.api.User
import com.example.fotogram.navigator.profileScreen.ProfileViewModel

@Composable
fun FriendProfile(
    modifier: Modifier = Modifier,
    userId: Int,
    onChangeScreen: (String) -> Unit,
    onBack: () -> Unit,
    onPostClick: (Int) -> Unit // <--- 1. NUOVO PARAMETRO: Per gestire il click sul post
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: ProfileViewModel = viewModel()

    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var friendUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(userId) {
        val token = sessionManager.fetchSession()
        if (token != null) {
            viewModel.loadUserPosts(token, userId)
            try {
                val response = RetrofitClient.api.getUser(userId, token)
                if (response.isSuccessful) {
                    friendUser = response.body()
                }
            } catch (e: Exception) { }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(text = "Profilo Utente", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        // --- INFO AMICO ---
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Foto Profilo
            if (friendUser?.profilePicture != null) {
                val avatarBitmap = remember(friendUser!!.profilePicture) {
                    try {
                        val bytes = Base64.decode(friendUser!!.profilePicture, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                    } catch (e: Exception) { null }
                }
                if (avatarBitmap != null) {
                    Image(
                        bitmap = avatarBitmap, contentDescription = null,
                        modifier = Modifier.size(80.dp).clip(CircleShape).border(1.dp, Color.Gray, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else { DefaultFriendAvatar() }
            } else { DefaultFriendAvatar() }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = friendUser?.username ?: "Utente #$userId", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* TODO: Follow */ }) { Text("Segui") }
        }

        Divider()

        // --- GRIGLIA POST AMICO ---
        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (posts.isEmpty()) {
                Text("Nessun post.", Modifier.align(Alignment.Center), color = Color.Gray)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
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
                                        .aspectRatio(1f)
                                        .background(Color.LightGray)
                                        // 2. RENDIAMO L'IMMAGINE CLICCABILE
                                        .clickable { onPostClick(post.id) },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DefaultFriendAvatar() {
    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
}