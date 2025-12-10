package com.example.fotogram.navigator.feedScreen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.fotogram.ScreenPlaceholder
import com.example.fotogram.navigator.NavigationBar
import com.example.fotogram.navigator.Post
import com.example.fotogram.navigator.PostDataClass

@Composable
fun Feed(
    modifier: Modifier = Modifier,
    onChangeScreen: (String) -> Unit,
    onChangeTab: (String) -> Unit
) {
    Column {
        ScreenPlaceholder("Bacheca", modifier = modifier.weight(1f), Color.Magenta)
        Post(
            modifier = Modifier.weight(1f),
            page = "Feed",
            msg = "POST DELL'AMICO",
            postData = PostDataClass.Utente1,
            onChangeScreen = onChangeScreen)
        NavigationBar(modifier = modifier, page = "Feed", onChangeScreen = onChangeScreen, onChangeTab = onChangeTab)
    }
}

