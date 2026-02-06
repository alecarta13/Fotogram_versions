package com.example.fotogram.navigator

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
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
    page: String,
    onChangeScreen: (String) -> Unit,
    onChangeTab: (String) -> Unit
) {
    Surface(modifier = modifier.fillMaxWidth(), shadowElevation = 0.dp, color = Color.White) {
        Column {
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 30.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavIcon(Icons.Default.Home, (page == "Feed")) { onChangeTab("Feed") }
                NavIcon(Icons.Default.AddCircle, (page == "NewPost")) { onChangeTab("NewPost") }
                NavIcon(Icons.Default.Person, (page == "Profile")) { onChangeTab("Profile") }
            }
        }
    }
}
@Composable fun NavIcon(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) { IconButton(onClick) { Icon(icon, null, tint = if (isSelected) Color.Black else Color.LightGray, modifier = Modifier.size(28.dp)) } }