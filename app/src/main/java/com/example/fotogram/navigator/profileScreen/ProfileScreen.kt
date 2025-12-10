package com.example.fotogram.navigator.profileScreen

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
fun Profile(
    modifier: Modifier = Modifier,
    onChangeScreen: (String) -> Unit,
    onChangeTab: (String) -> Unit
) {
    Column {
        ScreenPlaceholder("Profilo", modifier = modifier.weight(1f), Color.Cyan)
        Box(
            modifier = modifier
                .clickable{
                    Log.d("Feed", "Da Feed a EditProfile" )
                    onChangeScreen("EditProfile")
                }
        ){
            Text(
                text = "Clicca qui per modificare il profilo",
                modifier = modifier.align(Alignment.Center),
            )
        }
        Post(modifier = Modifier, page = "Profile", msg = "Visualizza mio post", postData = PostDataClass.Utente1, onChangeScreen = onChangeScreen, )
        NavigationBar( modifier = modifier, page = "Profile", onChangeScreen = onChangeScreen, onChangeTab = onChangeTab)
    }
}