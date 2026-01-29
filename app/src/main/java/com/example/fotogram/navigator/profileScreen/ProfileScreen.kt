package com.example.fotogram.navigator.profileScreen

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable // <--- Importante: serve per il click
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.example.fotogram.SessionManager
import com.example.fotogram.api.RetrofitClient
import com.example.fotogram.api.User
import com.example.fotogram.navigator.NavigationBar

@Composable
fun Profile(
    modifier: Modifier = Modifier,
    onChangeScreen: (String) -> Unit,
    onChangeTab: (String) -> Unit,
    onPostClick: (Int) -> Unit // <--- 1. NUOVO PARAMETRO: Funzione per gestire il click
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: ProfileViewModel = viewModel()

    // STATO PER I MIEI DATI (Nome e Foto)
    var myUser by remember { mutableStateOf<User?>(null) }

    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        val token = sessionManager.fetchSession()
        val myUserId = sessionManager.fetchUserId()

        if (token != null && myUserId != -1) {
            viewModel.loadUserPosts(token, myUserId)

            try {
                // Scarica i dati utente (Nome e Foto)
                val userResponse = RetrofitClient.api.getUser(myUserId, token)
                if (userResponse.isSuccessful) {
                    myUser = userResponse.body()
                }
            } catch (e: Exception) { }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        // --- HEADER DEL PROFILO ---
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // FOTO PROFILO
            if (myUser?.profilePicture != null) {
                val myBitmap = remember(myUser!!.profilePicture) {
                    try {
                        val bytes = Base64.decode(myUser!!.profilePicture, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                    } catch (e: Exception) { null }
                }

                if (myBitmap != null) {
                    Image(
                        bitmap = myBitmap,
                        contentDescription = "Me",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Gray, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    DefaultProfileIcon()
                }
            } else {
                DefaultProfileIcon()
            }

            Spacer(modifier = Modifier.height(8.dp))

            // NOME UTENTE
            Text(
                text = myUser?.username ?: sessionManager.fetchUserName() ?: "Profilo",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // STATISTICHE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(number = posts.size.toString(), label = "Post")
                ProfileStat(number = "0", label = "Follower")
                ProfileStat(number = "0", label = "Seguiti")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TASTO LOGOUT
            Button(
                onClick = {
                    sessionManager.clearSession()
                    onChangeScreen("Login")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }

        // --- GRIGLIA FOTO ---
        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (posts.isEmpty()) {
                Text("Nessuna foto pubblicata", Modifier.align(Alignment.Center), color = Color.Gray)
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

        NavigationBar(modifier = Modifier, page = "Profile", onChangeScreen = onChangeScreen, onChangeTab = onChangeTab)
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