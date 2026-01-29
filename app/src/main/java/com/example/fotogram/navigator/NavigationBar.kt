package com.example.fotogram.navigator

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    page: String, // Serve per colorare l'icona giusta
    onChangeScreen: (String) -> Unit, // Serve per logout o cambi pagina forzati
    onChangeTab: (String) -> Unit // Serve quando clicchi le icone in basso
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icona FEED
            NavIcon(Icons.Default.Home, isSelected = (page == "Feed")) { onChangeTab("Feed") }

            // Icona NEW POST
            NavIcon(Icons.Default.AddCircle, isSelected = (page == "NewPost")) { onChangeTab("NewPost") }

            // Icona PROFILE
            NavIcon(Icons.Default.Person, isSelected = (page == "Profile")) { onChangeTab("Profile") }
        }
    }
}

@Composable
fun NavIcon(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.Black else Color.LightGray,
            modifier = Modifier.size(32.dp)
        )
    }
}