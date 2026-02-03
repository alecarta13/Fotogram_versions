package com.example.fotogram.navigator.detailPostScreen

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fotogram.SessionManager
import com.example.fotogram.api.PostDetail
import com.example.fotogram.api.RetrofitClient
import com.example.fotogram.navigator.GoBack
import com.example.fotogram.navigator.NavigationBar

@Composable
fun DetailsPostScreen(
    modifier: Modifier = Modifier,
    postId: Int,
    onChangeScreen: (String) -> Unit,
    tab: String,
    onChangeTab: (String) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var post by remember { mutableStateOf<PostDetail?>(null) }

    LaunchedEffect(postId) {
        val token = sessionManager.fetchSession()
        if (token != null) {
            try {
                val response = RetrofitClient.api.getPost(postId, token)
                if (response.isSuccessful) {
                    post = response.body()
                }
            } catch (e: Exception) { }
        }
    }

    val destination = when(tab) {
        "Feed" -> "Feed"
        "Profile" -> "Profile"
        "FriendProfile" -> "FriendProfile"
        else -> "Feed"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            GoBack(
                modifier = Modifier.statusBarsPadding(),
                page = "Dettaglio Post",
                goToPage = destination,
                onChangeScreen = onChangeScreen
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                page = "DetailsPost",
                onChangeScreen = onChangeScreen,
                onChangeTab = onChangeTab
            )
        }
    ) { innerPadding ->

        Box(
            modifier = modifier // <--- Il modifier esterno lo applichiamo QUI al contenuto
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (post != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top // Meglio Top se l'immagine è grande
                ) {
                    // Immagine
                    if (post!!.contentPicture != null) {
                        val bitmap = remember(post!!.contentPicture) {
                            try {
                                val bytes = Base64.decode(post!!.contentPicture, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                            } catch (e: Exception) { null }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Descrizione
                    Text(
                        text = post?.contentText ?: "",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Text("Caricamento post...")
            }
        }
    }
}