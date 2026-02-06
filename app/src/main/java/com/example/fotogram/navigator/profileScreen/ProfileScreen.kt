package com.example.fotogram.navigator.profileScreen

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fotogram.SessionManager
import com.example.fotogram.api.PostDetail
import com.example.fotogram.navigator.NavigationBar

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onChangeScreen: (String) -> Unit,
    onChangeTab: (String) -> Unit,
    onPostClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: ProfileViewModel = viewModel()
    val user by viewModel.userProfile.collectAsState()
    val posts by viewModel.userPosts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val followerCount by viewModel.followerCount.collectAsState()
    val followingCount by viewModel.followingCount.collectAsState()

    LaunchedEffect(Unit) {
        val token = sessionManager.fetchSession()
        val userId = sessionManager.fetchUserId()
        if (token != null && userId != -1) viewModel.loadUserProfile(userId, token)
    }

    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        Box(modifier = Modifier.fillMaxWidth().padding(top = 70.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)) {
            Text(user?.username ?: "Profilo", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterStart))
            IconButton({ onChangeScreen("EditProfile") }, Modifier.align(Alignment.CenterEnd).size(24.dp)) { Icon(Icons.Default.Settings, "Modifica", tint = Color.Black) }
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

        Column(Modifier.weight(1f).fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(top = 16.dp, start = 16.dp, end = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (user?.profilePicture != null) {
                    val b = remember(user!!.profilePicture) { try { val bytes = Base64.decode(user!!.profilePicture, Base64.DEFAULT); BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap() } catch (e: Exception) { null } }
                    if (b != null) Image(b, null, Modifier.size(90.dp).clip(CircleShape).border(1.dp, Color.Gray, CircleShape), contentScale = ContentScale.Crop) else DefaultProfileIcon()
                } else DefaultProfileIcon()
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ProfileStat(posts.size.toString(), "Post")
                    ProfileStat(followerCount.toString(), "Follower")
                    ProfileStat(followingCount.toString(), "Seguiti")
                }
                Spacer(Modifier.height(16.dp))
                if (!user?.bio.isNullOrEmpty()) Text(user!!.bio!!, fontSize = 14.sp)
                if (!user?.dateOfBirth.isNullOrEmpty()) { Spacer(Modifier.height(4.dp)); Text("🎂 ${user!!.dateOfBirth}", fontSize = 12.sp, color = Color.Gray) }
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFEEEEEE))
            Box(Modifier.fillMaxSize()) {
                if (isLoading) CircularProgressIndicator(Modifier.align(Alignment.Center)) else if (posts.isEmpty()) Text("Nessun post ancora.", Modifier.align(Alignment.Center), color = Color.Gray) else {
                    LazyVerticalGrid(GridCells.Fixed(3), contentPadding = PaddingValues(1.dp), horizontalArrangement = Arrangement.spacedBy(1.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        items(posts) { post -> PostThumbnail(post, onPostClick) }
                    }
                }
            }
        }
        NavigationBar(page = "Profile", onChangeScreen = onChangeScreen, onChangeTab = onChangeTab)
    }
}
@Composable fun PostThumbnail(post: PostDetail, onPostClick: (Int) -> Unit) { if (post.contentPicture != null) { val b = remember(post.contentPicture) { try { val bytes = Base64.decode(post.contentPicture, Base64.DEFAULT); BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap() } catch (e: Exception) { null } }; if (b != null) Image(b, null, Modifier.aspectRatio(1f).background(Color.LightGray).clickable { onPostClick(post.id) }, contentScale = ContentScale.Crop) } }
@Composable fun ProfileStat(number: String, label: String) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(number, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text(label, fontSize = 12.sp, color = Color.Gray) } }
@Composable fun DefaultProfileIcon() { Icon(Icons.Default.AccountCircle, "Profile", Modifier.size(90.dp).clip(CircleShape).border(1.dp, Color.Gray, CircleShape), tint = Color.LightGray) }