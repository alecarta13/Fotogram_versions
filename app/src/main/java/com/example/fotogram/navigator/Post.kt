package com.example.fotogram.navigator

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.fotogram.ScreenPlaceholder

@Composable
fun Post(
    modifier : Modifier = Modifier,
    page: String,
    msg: String,
    postData: PostDataClass,
    onChangeScreen : (String) -> Unit
) {

    Column (
        modifier = modifier
            .background(color = Color.Blue)

    ){
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.Red)
                .clickable{
                    Log.d("$page", "Da $page a Post" )
                    onChangeScreen("FriendProfile")
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Immagine profilo
            Box (
                modifier = Modifier
                    .background(Color.Gray)
                    .size(50.dp)
            )

            // Spazio
            Spacer(modifier = Modifier.width(8.dp))

            // Nome utente
            Text(
                text = postData.userName,
            )
        }

        // Spazio tra header e immagine
        //Spacer(modifier = Modifier.size(8.dp))

        // Successiva immagine del post
        Box(
            modifier = Modifier // Usa Modifier nuovo
                .fillMaxWidth()
                .height(300.dp)
                .background(color = Color.Black)
                .clickable{
                    Log.d("$page", "Da $page a Post" )
                    onChangeScreen("DetailsPost")
                }
        ) {
            Text(
                text = msg,
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        // Descrizione del post
        Text(
            text = postData.description,
            modifier = Modifier
                .padding(16.dp),
            color = Color.White
        )
    }
}

@Composable
fun DetailsPost(modifier: Modifier = Modifier, onChangeScreen: (String) -> Unit, tab : String) {
    Column {
        if (tab == "Feed") {
            GoBack(modifier = modifier, page = "DetailsPost", goToPage = "FriendProfile", onChangeScreen = onChangeScreen)
        } else if (tab == "Profile") {
            GoBack(modifier = modifier, page = "DetailsPost", goToPage = "Profile", onChangeScreen = onChangeScreen)
        }
        ScreenPlaceholder("DetailsPost", modifier = modifier.weight(1f), Color.Blue)
    }
}