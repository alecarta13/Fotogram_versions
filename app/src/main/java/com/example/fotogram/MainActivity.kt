package com.example.fotogram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.fotogram.navigator.ScreenNavigator
import com.example.fotogram.ui.theme.FotogramTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Controlliamo se c'è una sessione salvata
        val sessionManager = SessionManager(this)
        val sessionToken = sessionManager.fetchSession()

        // 2. Se il token è null, deve fare Login. Se c'è, va al Feed.
        val startScreen = if (sessionToken == null) "Login" else "Feed"

        enableEdgeToEdge()
        setContent {
            FotogramTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // 3. Passiamo la schermata iniziale al navigatore
                    ScreenNavigator(
                        modifier = Modifier.padding(innerPadding),
                        startDestination = startScreen
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenPlaceholder(screenName: String, modifier: Modifier = Modifier, color : Color = Color.LightGray) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color)

    ){
        Text(
            text = screenName,
            modifier = modifier.align(Alignment.Center),
            color = Color.Black
        )
    }

}