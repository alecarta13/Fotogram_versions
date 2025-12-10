package com.example.fotogram.navigator

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.fotogram.navigator.feedScreen.*
import com.example.fotogram.navigator.friendProfile.*
import com.example.fotogram.navigator.newPostScreen.*
import com.example.fotogram.navigator.profileScreen.*

@Composable
fun ScreenNavigator(modifier : Modifier = Modifier) {

    var screen by remember { mutableStateOf( "Feed" ) }
    var tab by remember { mutableStateOf( "Feed" ) }

    fun changeScreen(nextScreen : String) {
        screen = nextScreen
    }

    fun changeTab(nextTab : String) {
        tab = nextTab
    }

    when (screen) {
        "Feed" -> Feed(modifier = modifier, onChangeScreen = ::changeScreen, onChangeTab = ::changeTab)
        "Profile" -> Profile(modifier = modifier, onChangeScreen = ::changeScreen, onChangeTab = ::changeTab)
        "NewPost" -> NewPost(modifier = modifier, onChangeScreen = ::changeScreen, onChangeTab = ::changeTab)
        "FriendProfile" -> FriendProfile(modifier = modifier, onChangeScreen = ::changeScreen)
        "EditProfile" -> EditProfile(modifier = modifier, onChangeScreen = ::changeScreen)
        "DetailsPost" -> DetailsPost(modifier = modifier, onChangeScreen = ::changeScreen, tab = tab)
        else -> Log.d("MyApp", "Schermata sbagliata")
    }

}

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    page: String,
    onChangeScreen: (String) -> Unit,
    onChangeTab: (String) -> Unit ){
    Row {
        Button(
            onClick = {
                Log.d("$page", "Da $page a NewPost")
                onChangeScreen("NewPost")
                onChangeTab("NewPost")
            }
        ) {
            Text(
                text = "NewPost",
            )
        }
        Button(
            onClick = {
                Log.d("$page", "Da $page a Feed")
                onChangeScreen("Feed")
                onChangeTab("Feed")
            }
        ) {
            Text(
                text = "Feed",
            )
        }
        Button(
            onClick = {
                Log.d("$page", "Da $page a Profile")
                onChangeScreen("Profile")
                onChangeTab("Profile")
            }
        ) {
            Text(
                text = "Profile",
            )
        }
    }
}

@Composable
fun GoBack(modifier : Modifier = Modifier, page: String, goToPage : String, onChangeScreen: (String) -> Unit){
    Box(
        modifier = modifier
            .clickable{
                Log.d("$page", "Da $page a $goToPage" )
                onChangeScreen(goToPage)
            }
            .background(color = Color.Gray)
    ){
        Text(
            text = "Torna indietro",
            modifier = modifier.align(Alignment.CenterStart),
            color = Color.White
        )
    }

}
