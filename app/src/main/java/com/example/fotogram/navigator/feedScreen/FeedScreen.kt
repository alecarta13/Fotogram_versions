package com.example.fotogram.navigator.feedScreen

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fotogram.SessionManager
import com.example.fotogram.api.PostDetail
import com.example.fotogram.navigator.NavigationBar

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    listState: LazyListState, // Per mantenere la posizione
    onChangeScreen: (String) -> Unit,
    onChangeTab: (String) -> Unit,
    onPostClick: (Int) -> Unit,
    onUserClick: (Int) -> Unit,
    onMapClick: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: FeedViewModel = viewModel()
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val followedIds by viewModel.followedIds.collectAsState()

    val token = remember { sessionManager.fetchSession() }

    // Caricamento iniziale
    LaunchedEffect(Unit) {
        if (token != null) viewModel.loadPosts(token)
    }

    // Paginazione: carica altri post quando arrivi in fondo
    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) false
            else visibleItemsInfo.last().index >= layoutInfo.totalItemsCount - 1
        }
    }
    LaunchedEffect(isAtBottom) {
        if (isAtBottom && !isLoading && token != null) viewModel.loadPosts(token)
    }

    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        // HEADER ORIGINALE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Text("Fotogram", fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterStart))
            IconButton(
                onClick = { if (token != null) viewModel.refreshPosts(token) },
                modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Aggiorna", tint = Color.Black)
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (posts.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nessun post nel feed.") }
            }

            LazyColumn(
                state = listState, // Memoria scroll
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(posts) { post ->
                    val isFollowing = followedIds.contains(post.authorId)
                    val isMe = (post.authorId == sessionManager.fetchUserId())
                    PostItem(post, isFollowing, isMe, { onPostClick(post.id) }, { onUserClick(it) }, {
                        if (token != null) viewModel.toggleFollow(post.authorId, token)
                    }, onMapClick)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Spinner discreto per caricamento nuovi post
                if (isLoading && posts.isNotEmpty()) {
                    item { Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(24.dp)) } }
                }
            }
            if (isLoading && posts.isEmpty()) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        NavigationBar(page = "Feed", onChangeScreen = onChangeScreen, onChangeTab = onChangeTab)
    }
}

@Composable
fun PostItem(
    post: PostDetail, isFollowing: Boolean, isMe: Boolean,
    onPostClick: () -> Unit, onProfileClick: (Int) -> Unit, onFollowClick: () -> Unit, onMapClick: (Double, Double) -> Unit
) {
    val isSuggested = !isFollowing && !isMe
    // Grafica originale per distinguere suggeriti
    val cardColor = if (isSuggested) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.White

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onPostClick() },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            if (isSuggested) {
                Text("★ POST SUGGERITO", Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(4.dp), Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            // Riga Autore
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.clickable { onProfileClick(post.authorId) }) {
                    if (post.authorProfilePicture != null) {
                        val bitmap = remember(post.authorProfilePicture) { try { val b = Base64.decode(post.authorProfilePicture, Base64.DEFAULT); BitmapFactory.decodeByteArray(b, 0, b.size)?.asImageBitmap() } catch (e: Exception) { null } }
                        if (bitmap != null) Image(bitmap, null, Modifier.size(40.dp).clip(CircleShape).border(1.dp, Color.Gray, CircleShape), contentScale = ContentScale.Crop) else DefaultAvatar()
                    } else DefaultAvatar()
                }
                Spacer(Modifier.width(8.dp))
                Text(post.author ?: "Utente", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f).clickable { onProfileClick(post.authorId) })
                if (!isMe) TextButton(onClick = onFollowClick) { Text(if (isFollowing) "Segui già" else "Segui", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }

            // Immagine Post
            if (post.contentPicture != null) {
                val bitmap = remember(post.contentPicture) { try { val b = Base64.decode(post.contentPicture, Base64.DEFAULT); BitmapFactory.decodeByteArray(b, 0, b.size)?.asImageBitmap() } catch (e: Exception) { null } }
                if (bitmap != null) Image(bitmap, "Post", Modifier.fillMaxWidth().heightIn(max = 500.dp).background(Color.LightGray), contentScale = ContentScale.Crop)
            }

            // Icone e Posizione (Grafica Originale + Tasto Posizione integrato)
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {

                // Tasto Posizione (Integrato senza rompere il layout)
                if (post.lat != null && post.lng != null) {

                    Row(
                        modifier = Modifier
                            .clickable { onMapClick(post.lat!!, post.lng!!) }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, "Mappa", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Vedi Posizione", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Descrizione
            if (!post.contentText.isNullOrEmpty()) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(post.author ?: "Utente") }
                        append(" ")
                        append(post.contentText)
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable fun DefaultAvatar() { Icon(Icons.Default.AccountCircle, null, Modifier.size(40.dp), Color.LightGray) }