package com.example.fotogram.navigator.detailPostScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.fotogram.ScreenPlaceholder
import com.example.fotogram.navigator.GoBack
import com.example.fotogram.navigator.NavigationBar

@Composable
fun DetailsPostScreen(modifier: Modifier = Modifier, onChangeScreen: (String) -> Unit, tab: String, onChangeTab: (String) -> Unit) {
    Column {
        if (tab == "Feed"){
            GoBack(modifier=modifier, page = "DetailsPost", goToPage = "FriendProfile", onChangeScreen = onChangeScreen)
        } else if (tab == "Profile"){
            GoBack(modifier=modifier, page = "DetailsPost", goToPage = "Profile", onChangeScreen = onChangeScreen)
        }
        ScreenPlaceholder("DetailsPost", modifier = modifier.weight(1f), Color.Blue)
        NavigationBar(modifier = modifier, page = "DetailsPost", onChangeScreen = onChangeScreen, onChangeTab = onChangeTab)
    }
}