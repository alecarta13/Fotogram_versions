package com.example.fotogram.navigator.profileScreen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fotogram.SessionManager
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    onChangeScreen: (String) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: EditProfileViewModel = viewModel()

    val isLoading by viewModel.isLoading.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val username by viewModel.currentUsername.collectAsState()
    val bio by viewModel.currentBio.collectAsState()
    val dateOfBirth by viewModel.currentDateOfBirth.collectAsState()

    // Gestione Immagine
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val scaled = Bitmap.createScaledBitmap(bitmap, 500, 500, true)
                selectedImageBitmap = scaled
                val outputStream = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val bytes = outputStream.toByteArray()
                val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
                viewModel.newProfileImageBase64 = base64String
            } catch (e: Exception) {
                Toast.makeText(context, "Errore immagine", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Gestione DatePicker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val dateString = sdf.format(Date(millis))
                        viewModel.currentDateOfBirth.value = dateString
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annulla") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LaunchedEffect(Unit) {
        val token = sessionManager.fetchSession()
        val userId = sessionManager.fetchUserId()
        if (token != null && userId != -1) {
            viewModel.loadCurrentProfile(userId, token)
        }
    }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            Toast.makeText(context, "Profilo aggiornato!", Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            onChangeScreen("Profile")
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            if (msg.contains("400")) {
                Toast.makeText(context, "Dati non validi (controlla lunghezza nome)", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifica Profilo") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetState()
                        onChangeScreen("Profile")
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Aggiunto scroll per schermi piccoli
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // FOTO
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageBitmap != null) {
                    Image(
                        bitmap = selectedImageBitmap!!.asImageBitmap(),
                        contentDescription = "Nuova Foto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Nota: Qui potresti caricare la foto attuale (base64) dal ViewModel se volessi,
                    // ma per semplicità lasciamo l'icona finché non ne selezioni una nuova.
                    Icon(Icons.Default.AccountCircle, null, Modifier.size(120.dp), Color.LightGray)
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.BottomEnd).background(Color.White, CircleShape).padding(4.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text("Cambia foto", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // CAMPO NOME (con limite 15 caratteri)
            OutlinedTextField(
                value = username,
                onValueChange = { if (it.length <= 15) viewModel.currentUsername.value = it },
                label = { Text("Nome Utente") },
                supportingText = { Text("${username.length}/15") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CAMPO DATA DI NASCITA (Readonly, cliccabile)
            OutlinedTextField(
                value = dateOfBirth ?: "",
                onValueChange = { },
                label = { Text("Data di Nascita") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true, // Impedisce la scrittura manuale
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleziona Data")
                    }
                },
                // Rendiamo tutto il campo cliccabile per aprire il calendario
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    .also { interactionSource ->
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect {
                                if (it is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                    showDatePicker = true
                                }
                            }
                        }
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CAMPO BIO
            OutlinedTextField(
                value = bio,
                onValueChange = { viewModel.currentBio.value = it },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(32.dp))

            // BOTTONE SALVA
            Button(
                onClick = {
                    val token = sessionManager.fetchSession()
                    if (token != null) viewModel.saveChanges(token)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading && username.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Salva Modifiche", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}