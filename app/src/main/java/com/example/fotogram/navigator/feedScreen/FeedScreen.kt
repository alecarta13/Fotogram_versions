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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star // Icona per indicare "Amico" (opzionale)
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
import com.example.fotogram.api.PostDetail
import com.example.fotogram.api.RetrofitClient
import com.example.fotogram.api.User
import com.example.fotogram.navigator.NavigationBar

@OptIn(ExperimentalMaterial3Api::class)
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
    // 1. Osserviamo la lista degli amici seguiti
    val followedIds by viewModel.followedIds.collectAsState()

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val token = remember { sessionManager.fetchSession() }
    // 2. Recuperiamo il NOSTRO ID per non seguirci da soli e per scaricare la lista giusta
    val myUserId = remember { sessionManager.fetchUserId() }

    // Caricamento iniziale
    LaunchedEffect(Unit) {
        if (token != null) {
            if (posts.isEmpty()) viewModel.loadPosts(token)

            // 3. Scarichiamo la lista degli amici se abbiamo l'ID valido
            if (myUserId != -1) {
                viewModel.loadFollowedUsers(myUserId, token)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fotogram") },
                actions = {
                    IconButton(onClick = {
                        if (token != null) {
                            viewModel.refreshPosts(token)
                            if (myUserId != -1) viewModel.loadFollowedUsers(myUserId, token)
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Ricarica")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier,
                page = "Feed",
                onChangeScreen = onChangeScreen,
                onChangeTab = onChangeTab
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            if (posts.isEmpty() && !isLoading) {
                Text(
                    text = "Nessun post.\nClicca l'icona in alto per aggiornare.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(posts) { post ->
                        PostItem(
                            post = post,
                            followedIds = followedIds, // Passiamo la lista amici
                            isMe = (post.authorId == myUserId), // Capiamo se siamo noi
                            onUserClick = onUserClick,
                            onPostClick = onPostClick
                        )
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun PostItem(
    post: PostDetail,
    followedIds: Set<Int>, // Parametro aggiunto
    isMe: Boolean,         // Parametro aggiunto
    onUserClick: (Int) -> Unit,
    onPostClick: (Int) -> Unit
) {
    val context = LocalContext.current
    // Nota: SessionManager qui non serve più per il token se non facciamo chiamate dirette
    // ma lo teniamo per recuperare l'autore se serve
    val sessionManager = remember { SessionManager(context) }
    var authorUser by remember { mutableStateOf<User?>(null) }

    // Logica Amico/Sconosciuto
    val isFriend = followedIds.contains(post.authorId)

    // 4. Colore del bordo: Blu se amico, Grigio chiaro se sconosciuto o me stesso
    val borderColor = if (isFriend) Color.Blue else Color.LightGray
    val borderThickness = if (isFriend) 3.dp else 1.dp

    LaunchedEffect(post.authorId) {
        val token = sessionManager.fetchSession()
        if (token != null) {
            try {
                val response = RetrofitClient.api.getUser(post.authorId, token)
                if (response.isSuccessful) authorUser = response.body()
            } catch (e: Exception) { }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onPostClick(post.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // --- HEADER (Autore) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clickable { onUserClick(post.authorId) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Foto Profilo con BORDO DINAMICO
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
                                .border(borderThickness, borderColor, CircleShape), // <--- QUI LA MAGIA
                            contentScale = ContentScale.Crop
                        )
                    } else DefaultAvatar()
                } else {
                    // Avatar di default con bordo
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(borderThickness, borderColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        DefaultAvatar()
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = authorUser?.username ?: "Utente ${post.authorId}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isFriend) Color.Blue else Color.Black // Colore testo nome (opzionale)
                        )
                        if (isFriend) {
                            Spacer(modifier = Modifier.width(4.dp))
                            // Stellina opzionale per far capire ancora meglio
                            // Icon(Icons.Default.Star, contentDescription = "Amico", modifier = Modifier.size(14.dp), tint = Color.Blue)
                        }
                    }

                    if (post.createdAt != null) {
                        Text(text = post.createdAt, fontSize = 12.sp, color = Color.Gray)
                    }
                }
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
                        contentDescription = "Post Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(Color.LightGray),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }

            // --- AZIONI ---
            Row(modifier = Modifier.padding(8.dp)) {
                IconButton(onClick = { /* Like */ }) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Like")
                }
                IconButton(onClick = { /* Share */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
            }

            // --- TESTO ---
            if (!post.contentText.isNullOrEmpty()) {
                Text(
                    text = post.contentText,
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    fontSize = 14.sp
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
        tint = Color.LightGray
    )
}