package com.example.fotogram.navigator

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.fotogram.navigator.loginScreen.LoginScreen
import com.example.fotogram.navigator.feedScreen.FeedScreen
import com.example.fotogram.navigator.newPostScreen.NewPost
import com.example.fotogram.navigator.profileScreen.ProfileScreen
import com.example.fotogram.navigator.profileScreen.EditProfileScreen
import com.example.fotogram.navigator.friendsProfileScreen.FriendProfile
import com.example.fotogram.navigator.detailPostScreen.DetailsPostScreen
// import com.example.fotogram.navigator.PostMapViewerScreen // Rimuovi se ti da errore, la funzione è in MapScreen.kt

@Composable
fun ScreenNavigator(
    modifier: Modifier = Modifier,
    startDestination: String
) {
    var currentScreen by remember { mutableStateOf(startDestination) }

    // Memoria Scroll Feed
    val feedScrollState = rememberLazyListState()

    var selectedUserId by remember { mutableStateOf(-1) }
    var selectedPostId by remember { mutableStateOf(-1) }

    // Stati Creazione Post
    var newPostDescription by remember { mutableStateOf("") }
    var newPostImageBase64 by remember { mutableStateOf<String?>(null) }
    var newPostLat by remember { mutableStateOf<Double?>(null) }
    var newPostLng by remember { mutableStateOf<Double?>(null) }

    // Dati Mappa
    var tempMapLat by remember { mutableStateOf<Double?>(null) }
    var tempMapLng by remember { mutableStateOf<Double?>(null) }

    // Variabile che ricorda chi ha aperto il Dettaglio (Feed o Profilo)
    var previousScreen by remember { mutableStateOf("Feed") }

    // Variabile per ricordare chi ha aperto il Profilo Amico
    var friendOrigin by remember { mutableStateOf("Feed") }

    when (currentScreen) {
        "Login" -> {
            LoginScreen(modifier = modifier, onChangeScreen = { currentScreen = it })
        }

        "Feed" -> {
            FeedScreen(
                modifier = modifier,
                listState = feedScrollState,
                onChangeScreen = { dest -> currentScreen = dest },
                onChangeTab = { tab -> currentScreen = tab },
                onUserClick = { userId ->
                    selectedUserId = userId
                    friendOrigin = "Feed"
                    currentScreen = "FriendProfile"
                },
                onPostClick = { postId ->
                    selectedPostId = postId
                    previousScreen = "Feed" // Arrivo dal Feed
                    currentScreen = "DetailsPost"
                },
                onMapClick = { lat, lng ->
                    tempMapLat = lat
                    tempMapLng = lng
                    // Se apro la mappa dal Feed (dalla card), quando torno indietro voglio tornare al Feed
                    previousScreen = "Feed"
                    currentScreen = "PostMapFromFeed" // Caso speciale (opzionale) o gestito dopo
                    // Nota: Se la mappa nel feed apre direttamente la mappa, ok.
                    // Se intendevi aprire il dettaglio, la logica sopra è giusta.
                    // Assumiamo che dal Feed tu apra direttamente la mappa:
                    currentScreen = "PostMap"
                }
            )
        }

        "NewPost" -> {
            NewPost(
                modifier = modifier,
                onChangeScreen = { currentScreen = it },
                onChangeTab = { tab -> currentScreen = tab },
                currentDescription = newPostDescription,
                onDescriptionChange = { newPostDescription = it },
                currentImage = newPostImageBase64,
                onImageChange = { newPostImageBase64 = it },
                selectedLat = newPostLat,
                selectedLng = newPostLng,
                onOpenMapSelector = { currentScreen = "MapSelector" },
                onPostSuccess = {
                    newPostDescription = ""
                    newPostImageBase64 = null
                    newPostLat = null
                    newPostLng = null
                    currentScreen = "Feed"
                }
            )
        }

        "MapSelector" -> {
            MapSelectorScreen(
                onLocationSelected = { lat, lng ->
                    newPostLat = lat
                    newPostLng = lng
                    currentScreen = "NewPost"
                },
                onCancel = { currentScreen = "NewPost" }
            )
        }

        "PostMap" -> {
            // Mappa a schermo intero
            if (tempMapLat != null && tempMapLng != null) {
                PostMapViewerScreen(
                    postLat = tempMapLat!!,
                    postLng = tempMapLng!!,
                    // CORREZIONE QUI:
                    // Se previousScreen era "Feed" (clic diretto da card), torniamo al Feed.
                    // Se eravamo in "DetailsPost", torniamo a "DetailsPost".
                    // Ma aspetta: Quando apri la mappa DA DENTRO il dettaglio, non cambiamo 'previousScreen'.
                    // Quindi dobbiamo tornare a "DetailsPost" manualmente se veniamo da lì.

                    onBack = {
                        // Se stavo guardando un dettaglio, torno al dettaglio.
                        // Altrimenti (es. clic dalla card del feed) torno a previousScreen.
                        // Per semplicità nel tuo flusso attuale (Mappa si apre da Dettaglio):
                        currentScreen = "DetailsPost"
                    }
                )
            } else {
                currentScreen = "DetailsPost"
            }
        }

        "Profile" -> {
            ProfileScreen(
                modifier = modifier,
                onChangeScreen = { dest -> currentScreen = dest },
                onChangeTab = { tab -> currentScreen = tab },
                onPostClick = { postId ->
                    selectedPostId = postId
                    previousScreen = "Profile" // Arrivo dal Profilo
                    currentScreen = "DetailsPost"
                }
            )
        }

        "FriendProfile" -> {
            FriendProfile(
                modifier = modifier,
                userId = selectedUserId,
                onChangeScreen = { currentScreen = it },
                onBack = { currentScreen = friendOrigin },
                onPostClick = { postId ->
                    selectedPostId = postId
                    previousScreen = "FriendProfile" // Arrivo da Profilo Amico
                    currentScreen = "DetailsPost"
                }
            )
        }

        "DetailsPost" -> {
            DetailsPostScreen(
                modifier = modifier,
                postId = selectedPostId,
                onChangeScreen = { currentScreen = it },
                tab = previousScreen, // Il tasto indietro userà "Profile" o "Feed" correttamente
                onChangeTab = { tab -> currentScreen = tab },

                // CORREZIONE QUI:
                onMapClick = { lat, lng ->
                    tempMapLat = lat
                    tempMapLng = lng

                    // NON sovrascrivere previousScreen con "Feed"!
                    // Lascialo com'è (es. "Profile"), così DetailPost ricorda da dove viene.

                    currentScreen = "PostMap"
                }
            )
        }

        "EditProfile" -> {
            EditProfileScreen(modifier = modifier, onChangeScreen = { currentScreen = it })
        }

        else -> LoginScreen(modifier = modifier, onChangeScreen = { currentScreen = it })
    }
}