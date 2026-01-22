package com.example.fotogram.navigator.profileScreen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fotogram.ScreenPlaceholder
import com.example.fotogram.SessionManager
import com.example.fotogram.navigator.NavigationBar
import com.example.fotogram.navigator.Post
import com.example.fotogram.navigator.PostDataClass

@Composable
fun Profile(
    modifier: Modifier = Modifier,
    onChangeScreen: (String) -> Unit,
    onChangeTab: (String) -> Unit
) {
    // 1. Otteniamo il contesto e inizializziamo il SessionManager
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    Column {
        ScreenPlaceholder("Profilo", modifier = modifier.weight(1f), Color.Cyan)

        // Link Modifica Profilo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable{
                    Log.d("Feed", "Da Feed a EditProfile" )
                    onChangeScreen("EditProfile")
                }
        ){
            Text(
                text = "Clicca qui per modificare il profilo",
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // --- TASTO LOGOUT (NUOVO) ---
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    // 1. Cancella il token dalla memoria
                    sessionManager.clearSession()
                    // 2. Torna alla schermata di Login
                    onChangeScreen("Login")
                },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Logout (Reset Sessione)")
            }
        }
        // -----------------------------

        Post(modifier = Modifier, page = "Profile", msg = "Visualizza mio post", postData = PostDataClass.Utente1, onChangeScreen = onChangeScreen, )

        NavigationBar( modifier = modifier, page = "Profile", onChangeScreen = onChangeScreen, onChangeTab = onChangeTab)
    }
}