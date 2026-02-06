package com.example.fotogram

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.fotogram.navigator.ScreenNavigator
import com.example.fotogram.ui.theme.FotogramTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Configurazione Edge-to-Edge per barre trasparenti e icone scure
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
        )

        // 2. Controlliamo se c'è una sessione salvata
        val sessionManager = SessionManager(this)
        val sessionToken = sessionManager.fetchSession()

        // 3. Se il token è null, login. Altrimenti Feed.
        val startScreen = if (sessionToken == null) "Login" else "Feed"

        setContent {
            FotogramTheme {
                // Rimosso lo Scaffold esterno per evitare sfondi grigi indesiderati.
                // Le singole schermate gestiranno i loro spazi.
                ScreenNavigator(
                    modifier = Modifier.fillMaxSize(),
                    startDestination = startScreen
                )
            }
        }
    }
}

@Composable
fun ScreenPlaceholder(screenName: String, modifier: Modifier = Modifier, color : androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.LightGray) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color)
    ){
        Text(
            text = screenName,
            modifier = modifier.align(Alignment.Center),
            color = androidx.compose.ui.graphics.Color.Black
        )
    }
}