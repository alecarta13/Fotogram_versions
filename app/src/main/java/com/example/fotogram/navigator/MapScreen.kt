package com.example.fotogram.navigator

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.preference.PreferenceManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

// Soglia di vicinanza (50km)
const val VICINITY_THRESHOLD_METERS = 50000.0

// --- 1. SELETTORE (Creazione Post) ---
@Composable
fun MapSelectorScreen(
    onLocationSelected: (Double, Double) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var selectedGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }

    // Riferimento alla MapView per il bottone "Centra su di me"
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var locationOverlayRef by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        hasLocationPermission = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true || perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {

        // HEADER
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp).zIndex(1f)) {
            IconButton(onCancel, Modifier.align(Alignment.CenterStart)) { Icon(Icons.Default.Close, "Annulla", tint = Color.Black) }
            Text("Scegli Posizione", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
            if (selectedGeoPoint != null) {
                IconButton({ onLocationSelected(selectedGeoPoint!!.latitude, selectedGeoPoint!!.longitude) }, Modifier.align(Alignment.CenterEnd)) {
                    Icon(Icons.Default.Check, "Conferma", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

        // MAPPA
        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))) {

            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapViewRef = this
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        isTilesScaledToDpi = true
                        minZoomLevel = 4.0

                        // Default Roma (Fallback)
                        val startPoint = GeoPoint(41.9028, 12.4964)
                        controller.setZoom(10.0)
                        controller.setCenter(startPoint)

                        if (hasLocationPermission) {
                            val locOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                            locationOverlayRef = locOverlay

                            // --- FIX ICONA UTENTE ---
                            val personIcon = BitmapFactory.decodeResource(resources, org.osmdroid.library.R.drawable.person)
                            locOverlay.setPersonIcon(personIcon)
                            locOverlay.setDirectionIcon(personIcon)

                            locOverlay.enableMyLocation()
                            locOverlay.runOnFirstFix {
                                (ctx as? android.app.Activity)?.runOnUiThread {
                                    controller.animateTo(locOverlay.myLocation)
                                    controller.setZoom(16.0)
                                }
                            }
                            overlays.add(locOverlay)
                        }

                        // Tap Listener
                        overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                p?.let {
                                    selectedGeoPoint = it
                                    overlays.removeAll { o -> o is Marker }
                                    val m = Marker(this@apply).apply {
                                        position = it
                                        title = "Scelta"
                                        // Usiamo il PIN classico per il punto scelto
                                        icon = ContextCompat.getDrawable(ctx, org.osmdroid.library.R.drawable.marker_default)
                                    }
                                    m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    overlays.add(m)
                                    invalidate()
                                }
                                return true
                            }
                            override fun longPressHelper(p: GeoPoint?) = false
                        }))
                    }
                },
                // --- UPDATE FONDAMENTALE PER GESTIRE I PERMESSI IN RITARDO ---
                update = { mapView ->
                    if (hasLocationPermission && locationOverlayRef == null) {
                        val locOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)

                        val personIcon = BitmapFactory.decodeResource(context.resources, org.osmdroid.library.R.drawable.person)
                        locOverlay.setPersonIcon(personIcon)
                        locOverlay.setDirectionIcon(personIcon)

                        locOverlay.enableMyLocation()
                        locOverlay.runOnFirstFix {
                            (context as? android.app.Activity)?.runOnUiThread {
                                mapView.controller.animateTo(locOverlay.myLocation)
                                mapView.controller.setZoom(16.0)
                            }
                        }
                        mapView.overlays.add(locOverlay)
                        locationOverlayRef = locOverlay
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // --- BOTTONE "CENTRA SU DI ME" (FAB) ---
            if (hasLocationPermission) {
                FloatingActionButton(
                    onClick = {
                        val overlay = locationOverlayRef
                        val map = mapViewRef
                        if (overlay != null && map != null && overlay.myLocation != null) {
                            map.controller.animateTo(overlay.myLocation)
                            map.controller.setZoom(16.0)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    // Icona mirino (più corretta per "Centra su di me")
                    Icon(Icons.Default.LocationOn, contentDescription = "La mia posizione")
                }
            }
        }
    }
}

// --- 2. VISUALIZZATORE (Dettaglio Post) ---
@Composable
fun PostMapViewerScreen(
    postLat: Double,
    postLng: Double,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        // HEADER
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp).zIndex(1f), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Indietro", tint = Color.Black) }
            Spacer(Modifier.width(8.dp))
            Text("Posizione Post", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

        // MAPPA
        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        isTilesScaledToDpi = true

                        val postPoint = GeoPoint(postLat, postLng)

                        // 1. PIN POST (SEMPRE VISIBILE)
                        val m = Marker(this).apply {
                            position = postPoint
                            title = "Posizione Post"
                            // Assicuriamo che il post abbia il PIN classico (non l'omino)
                            icon = ContextCompat.getDrawable(ctx, org.osmdroid.library.R.drawable.marker_default)
                        }
                        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        overlays.add(m)

                        // 2. Default View sul Post
                        controller.setZoom(15.0)
                        controller.setCenter(postPoint)

                        // 3. MOSTRA UTENTE SE VICINO
                        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            val locOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)

                            // --- FIX ICONA UTENTE ---
                            val personIcon = BitmapFactory.decodeResource(resources, org.osmdroid.library.R.drawable.person)
                            locOverlay.setPersonIcon(personIcon)
                            locOverlay.setDirectionIcon(personIcon) // Niente più freccia bianca!

                            locOverlay.enableMyLocation()
                            locOverlay.isDrawAccuracyEnabled = true

                            locOverlay.runOnFirstFix {
                                val userLocation = locOverlay.myLocation
                                if (userLocation != null) {
                                    val distance = userLocation.distanceToAsDouble(postPoint)

                                    (ctx as? android.app.Activity)?.runOnUiThread {
                                        if (distance <= VICINITY_THRESHOLD_METERS) {
                                            // CASO: VICINO -> Aggiungi Overlay Utente
                                            if (!overlays.contains(locOverlay)) {
                                                overlays.add(locOverlay)
                                            }
                                            // Zoom automatico
                                            val box = BoundingBox.fromGeoPoints(listOf(postPoint, userLocation))
                                            zoomToBoundingBox(box, true, 150)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}