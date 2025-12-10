package com.example.fotogram.navigator.profileScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.fotogram.ScreenPlaceholder
import com.example.fotogram.navigator.GoBack
import com.example.fotogram.navigator.NavigationBar

@Composable
fun EditProfile( modifier: Modifier = Modifier, onChangeScreen: (String) -> Unit) {
    Column {
        GoBack(modifier = modifier, page = "EditProfile", goToPage = "Profile", onChangeScreen = onChangeScreen)
        ScreenPlaceholder("EditProfile", modifier = modifier.weight(1f), Color.Red)
    }

}