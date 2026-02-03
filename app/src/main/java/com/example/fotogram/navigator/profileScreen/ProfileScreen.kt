package com.example.fotogram.navigator.profileScreen

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
import com.example.fotogram.navigator.NavigationBar

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onChangeScreen: (String) -> Unit,
    onChangeTab: (String) -> Unit,
    onPostClick: (Int) -> Unit
) {
    val viewModel: ProfileViewModel = viewModel()
    val userProfile by viewModel.userProfile.collectAsState()
    val userPosts by viewModel.userPosts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val token = remember { sessionManager.fetchSession() }
    val userId = remember { sessionManager.fetchUserId() } // Assicurati di avere fetchUserId in SessionManager

    LaunchedEffect(Unit) {
        if (token != null && userId != -1) {
            viewModel.loadUserProfile(userId, token)
        }
    }

    // USARE SCAFFOLD PER IL LAYOUT CORRETTO
    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                page = "Profile",
                onChangeScreen = onChangeScreen,
                onChangeTab = onChangeTab
            )
        }
    ) { innerPadding -> // Padding obbligatorio dello Scaffold

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding) // Applica il padding qui
        ) {
            // --- HEADER DEL PROFILO ---
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (userProfile != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Immagine Profilo
                    if (userProfile!!.profilePicture != null) {
                        val bitmap = remember(userProfile!!.profilePicture) {
                            try {
                                val bytes =
                                    Base64.decode(userProfile!!.profilePicture, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.Gray, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else DefaultProfileIcon()
                    } else DefaultProfileIcon()

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = userProfile?.username ?: "Utente",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Statistiche (Post, Follower, Seguiti)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStat(number = "${userPosts.size}", label = "Post")
                        ProfileStat(number = "0", label = "Follower") // Dati finti per ora
                        ProfileStat(number = "0", label = "Seguiti")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tasto Modifica Profilo
                    Button(onClick = { onChangeScreen("EditProfile") }) {
                        Text("Modifica Profilo")
                    }
                }

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                // --- GRIGLIA DEI POST ---
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(userPosts) { post ->
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
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Errore caricamento profilo")
                }
            }
        }
    }
}

@Composable
fun ProfileStat(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = number, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun DefaultProfileIcon() {
    Icon(
        imageVector = Icons.Default.AccountCircle,
        contentDescription = "Profile",
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .border(2.dp, Color.Gray, CircleShape),
        tint = Color.LightGray
    )
}