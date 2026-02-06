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
    val viewModel: ProfileViewModel = viewModel()

    val posts by viewModel.userPosts.collectAsState()
    val friendUser by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()

    // --- DATI STATISTICI ---
    val followerCount by viewModel.followerCount.collectAsState()
    val followingCount by viewModel.followingCount.collectAsState()

    LaunchedEffect(userId) {
        val token = sessionManager.fetchSession()
        if (token != null) {
            // CORREZIONE: Basta chiamare loadUserProfile.
            // Questa funzione ora scarica tutto: info, post, statistiche e stato follow.
            viewModel.loadUserProfile(userId, token)
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 5.dp, end = 5.dp, top = 60.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(text = friendUser?.username ?: "Profilo", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        // --- INFO UTENTE ---
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
                        modifier = Modifier.size(90.dp).clip(CircleShape).border(1.dp, Color.Gray, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else DefaultFriendAvatar()
            } else DefaultFriendAvatar()

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = friendUser?.username ?: "Caricamento...",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- STATISTICHE (NUMERI REALI) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(count = posts.size.toString(), label = "Post")
                StatItem(count = followerCount.toString(), label = "Follower")
                StatItem(count = followingCount.toString(), label = "Seguiti")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tasto Segui
            val myToken = remember { sessionManager.fetchSession() }
            Button(
                onClick = {
                    if (myToken != null) viewModel.toggleFollow(userId, myToken)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) Color.Gray else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(if (isFollowing) "Smetti di seguire" else "Segui")
            }
        }

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        // --- GRIGLIA POST ---
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
                                    bitmap = bitmap, contentDescription = null,
                                    modifier = Modifier.aspectRatio(1f).background(Color.LightGray).clickable { onPostClick(post.id) },
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
fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun DefaultFriendAvatar() {
    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(90.dp), tint = Color.LightGray)
}