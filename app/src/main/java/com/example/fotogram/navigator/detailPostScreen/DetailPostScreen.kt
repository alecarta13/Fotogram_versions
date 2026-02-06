package com.example.fotogram.navigator.detailPostScreen

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.fotogram.SessionManager
import com.example.fotogram.api.PostDetail
import com.example.fotogram.api.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun DetailsPostScreen(
    modifier: Modifier = Modifier,
    postId: Int,
    onChangeScreen: (String) -> Unit,
    tab: String,
    onChangeTab: (String) -> Unit,
    onMapClick: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var post by remember { mutableStateOf<PostDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isFullScreenImage by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        val token = sessionManager.fetchSession()
        if (token != null) {
            try {
                val response = RetrofitClient.api.getPost(postId, token)
                if (response.isSuccessful) {
                    var p = response.body()
                    if (p != null) {
                        val authorResp = RetrofitClient.api.getUser(p.authorId, token)
                        if (authorResp.isSuccessful && authorResp.body() != null) {
                            val author = authorResp.body()!!
                            p = p.copy(author = author.username, authorProfilePicture = author.profilePicture)
                        }
                        post = p
                    }
                }
            } catch (e: Exception) {} finally { isLoading = false }
        }
    }

    // --- STRUTTURA MANUALE (Uniformata a Feed e Profilo) ---
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // HEADER MANUALE (Col padding per la status bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 12.dp, start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onChangeScreen(tab) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Dettaglio Post", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (post != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // AUTORE
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (post!!.authorProfilePicture != null) {
                        val b = remember(post!!.authorProfilePicture) {
                            try {
                                val bytes = Base64.decode(post!!.authorProfilePicture, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                            } catch (e: Exception) { null }
                        }
                        if (b != null) Image(b, null, Modifier.size(44.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        else Icon(Icons.Default.AccountCircle, null, Modifier.size(44.dp))
                    } else Icon(Icons.Default.AccountCircle, null, Modifier.size(44.dp), Color.Gray)

                    Spacer(Modifier.width(12.dp))
                    Text(post!!.author ?: "Utente", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(16.dp))

                // FOTO
                if (post!!.contentPicture != null) {
                    val b = remember(post!!.contentPicture) {
                        try {
                            val bytes = Base64.decode(post!!.contentPicture, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                        } catch (e: Exception) { null }
                    }
                    if (b != null) {
                        Image(
                            bitmap = b, contentDescription = "Post",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 500.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray)
                                .clickable { isFullScreenImage = true },
                            contentScale = ContentScale.Crop
                        )
                        Text("Tocca l'immagine per ingrandire", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))

                // METADATI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(post!!.createdAt?.take(10) ?: "Data sconosciuta", fontSize = 13.sp, color = Color.Gray)
                    }
                    // CLICK MAPPA
                    if (post!!.lat != null && post!!.lng != null) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onMapClick(post!!.lat!!, post!!.lng!!) }
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Vedi Mappa", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                // DESCRIZIONE
                if (!post!!.contentText.isNullOrEmpty()) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(post!!.author ?: "Utente") }
                            append(" ")
                            append(post!!.contentText)
                        },
                        fontSize = 15.sp, lineHeight = 22.sp
                    )
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Post non trovato") }
        }
    }

    if (isFullScreenImage && post?.contentPicture != null) {
        Box(Modifier.fillMaxSize().background(Color.Black).clickable { }, contentAlignment = Alignment.Center) {
            val b = remember(post!!.contentPicture) { try { val bytes = Base64.decode(post!!.contentPicture, Base64.DEFAULT); BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap() } catch (e: Exception) { null } }
            if (b != null) Image(b, null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            IconButton({ isFullScreenImage = false }, Modifier.align(Alignment.TopEnd).padding(30.dp, 40.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)) { Icon(Icons.Default.Close, "Chiudi", tint = Color.White) }
        }
    }
}