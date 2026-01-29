package com.example.fotogram.navigator

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.fotogram.navigator.loginScreen.LoginScreen
import com.example.fotogram.navigator.feedScreen.FeedScreen
import com.example.fotogram.navigator.newPostScreen.NewPost
import com.example.fotogram.navigator.profileScreen.Profile
import com.example.fotogram.navigator.profileScreen.EditProfile
import com.example.fotogram.navigator.friendProfile.FriendProfile
import com.example.fotogram.navigator.detailPostScreen.DetailsPostScreen

@Composable
fun ScreenNavigator(
    modifier: Modifier = Modifier,
    startDestination: String
) {
    var currentScreen by remember { mutableStateOf(startDestination) }

    // VARIABILI DI STATO
    var selectedUserId by remember { mutableStateOf(-1) }
    var selectedPostId by remember { mutableStateOf(-1) }

    // previousScreen: serve per i DettagliPost (per sapere se tornare a Feed, Profile o FriendProfile)
    var previousScreen by remember { mutableStateOf("Feed") }

    // NUOVA VARIABILE: Serve per ricordare da dove siamo entrati nel profilo amico (es. dal Feed)
    var friendOrigin by remember { mutableStateOf("Feed") }

    when (currentScreen) {
        "Login" -> {
            LoginScreen(modifier = modifier, onChangeScreen = { currentScreen = it })
        }

        "Feed" -> {
            FeedScreen(
                modifier = modifier,
                onChangeScreen = { dest -> currentScreen = dest },
                onChangeTab = { tab -> currentScreen = tab },

                // QUANDO CLICCO UN UTENTE DAL FEED
                onUserClick = { userId ->
                    selectedUserId = userId
                    friendOrigin = "Feed" // <--- 1. Memorizzo che vengo dal Feed
                    currentScreen = "FriendProfile"
                },

                onPostClick = { postId ->
                    selectedPostId = postId
                    previousScreen = "Feed"
                    currentScreen = "DetailsPost"
                }
            )
        }

        "NewPost" -> {
            NewPost(
                modifier = modifier,
                onChangeScreen = { dest -> currentScreen = dest },
                onChangeTab = { tab -> currentScreen = tab }
            )
        }

        "Profile" -> {
            Profile(
                modifier = modifier,
                onChangeScreen = { dest -> currentScreen = dest },
                onChangeTab = { tab -> currentScreen = tab },
                onPostClick = { postId ->
                    selectedPostId = postId
                    previousScreen = "Profile"
                    currentScreen = "DetailsPost"
                }
            )
        }

        "FriendProfile" -> {
            FriendProfile(
                modifier = modifier,
                userId = selectedUserId,
                onChangeScreen = { currentScreen = it },

                // <--- 2. USO LA MEMORIA SICURA
                // Invece di 'previousScreen' (che cambia), uso 'friendOrigin' che è fisso
                onBack = { currentScreen = friendOrigin },

                // Gestione click sul post dell'amico
                onPostClick = { postId ->
                    selectedPostId = postId
                    previousScreen = "FriendProfile" // Dico al post di tornare qui
                    currentScreen = "DetailsPost"
                }
            )
        }

        "DetailsPost" -> {
            DetailsPostScreen(
                modifier = modifier,
                postId = selectedPostId,
                onChangeScreen = { currentScreen = it },
                tab = previousScreen, // Torna al padre corretto (Feed, Profile o FriendProfile)
                onChangeTab = { tab -> currentScreen = tab }
            )
        }

        "EditProfile" -> {
            EditProfile(modifier = modifier, onChangeScreen = { currentScreen = it })
        }

        else -> LoginScreen(modifier = modifier, onChangeScreen = { currentScreen = it })
    }
}