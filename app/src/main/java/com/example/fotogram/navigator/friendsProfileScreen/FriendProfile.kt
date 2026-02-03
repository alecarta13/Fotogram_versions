package com.example.fotogram.navigator.friendsProfileScreen

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
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
import com.example.fotogram.navigator.profileScreen.ProfileViewModel

@Composable
fun FriendProfile(
    modifier: Modifier = Modifier,
    userId: Int,
    onChangeScreen: (String) -> Unit,
    onBack: () -> Unit,
    onPostClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // Usiamo lo stesso ViewModel del profilo personale
    val viewModel: ProfileViewModel = viewModel()

    // 1. CORREZIONE: Usiamo i nomi nuovi del ViewModel
    val posts by viewModel.userPosts.collectAsState() // Era .posts
    val friendUser by viewModel.userProfile.collectAsState() // Ora prendiamo l'utente dal VM
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(userId) {
        val token = sessionManager.fetchSession()
        if (token != null) {
            // 2. CORREZIONE: Usiamo la funzione unica che scarica tutto (Profilo + Post)
            viewModel.loadUserProfile(userId, token)
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
                    } catch (e: Exception) {
                        null
                    }
                }
                if (avatarBitmap != null) {
                    Image(
                        bitmap = avatarBitmap, contentDescription = null,
                        modifier = Modifier.size(80.dp).clip(CircleShape)
                            .border(1.dp, Color.Gray, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    DefaultFriendAvatar()
                }
            } else {
                DefaultFriendAvatar()
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nome Utente
            Text(
                text = friendUser?.username ?: "Caricamento...",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tasto Segui (Disabilitato per ora o da implementare)
            Button(onClick = { /* TODO: Follow */ }) { Text("Segui") }
        }

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

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
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        ?.asImageBitmap()
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .background(Color.LightGray)
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