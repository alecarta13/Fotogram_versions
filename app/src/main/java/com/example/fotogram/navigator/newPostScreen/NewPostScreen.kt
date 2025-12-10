package com.example.fotogram.navigator.newPostScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.fotogram.ScreenPlaceholder
import com.example.fotogram.navigator.NavigationBar

@Composable
fun NewPost(
    modifier: Modifier = Modifier,
    onChangeScreen: (String) -> Unit,
    onChangeTab: (String) -> Unit,
) {

    Column {
        ScreenPlaceholder("NewPost", modifier = modifier.weight(1f), Color.Green)
        NavigationBar(modifier = modifier, page = "NewPost", onChangeScreen = onChangeScreen, onChangeTab = onChangeTab)
    }
}