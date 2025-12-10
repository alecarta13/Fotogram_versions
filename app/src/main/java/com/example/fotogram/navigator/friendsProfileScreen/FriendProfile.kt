package com.example.fotogram.navigator.friendProfile

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.fotogram.ScreenPlaceholder
import com.example.fotogram.navigator.GoBack
import com.example.fotogram.navigator.NavigationBar
import com.example.fotogram.navigator.Post
import com.example.fotogram.navigator.PostDataClass

@Composable
fun FriendProfile(
    modifier: Modifier = Modifier,
    onChangeScreen: (String) -> Unit,
) {

    Column {
        GoBack(modifier = modifier, page = "FriendProfile", goToPage = "Feed", onChangeScreen = onChangeScreen)
        ScreenPlaceholder("Profilo Amico", modifier = modifier.weight(1f), Color.Yellow)
        Post(
            modifier = Modifier,
            page = "FriendProfile",
            msg = "Visualizza post",
            postData = PostDataClass.Utente1,
            onChangeScreen = onChangeScreen)
    }

}
