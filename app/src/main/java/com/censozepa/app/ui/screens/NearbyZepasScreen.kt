package com.censozepa.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.censozepa.app.data.local.DatabaseProvider
import com.censozepa.app.data.local.entity.ZepaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*

data class ZepaWithDistance(
    val zepa: ZepaEntity,
    val distanceKm: Double
)

fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyZepasScreenContent(
    onZepaClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var nearbyZepas by remember { mutableStateOf<List<ZepaWithDistance>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var userLat by remember { mutableStateOf(42.5987) }
    var userLon by remember { mutableStateOf(-5.5671) }
    var userLocationStr by remember { mutableStateOf("") }
    
    // Expanded state for the top 3 ZEPA cards
    var expandedZepaId by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            findNearbyZepas(context) { list, lat, lon ->
                nearbyZepas = list
                userLat = lat
                userLon = lon
                userLocationStr = "GPS: %.4f, %.4f".format(lat, lon)
                isSearching = false
                hasSearched = true
            }
        } else {
            isSearching = false
            hasSearched = true
            userLat = 42.5987
            userLon = -5.5671
            findNearbyZepasWithCoords(context, userLat, userLon) { list ->
                nearbyZepas = list
                userLocationStr = "Ubicación por defecto (León) - Permiso denegado"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ZEPAs Cercanas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Encuentra las 3 ZEPAs más próximas a tu posición actual calculadas por GPS.",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Button(
                onClick = {
                    isSearching = true
                    when {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                            findNearbyZepas(context) { list, lat, lon ->
                                nearbyZepas = list
                                userLat = lat
                                userLon = lon
                                userLocationStr = "GPS: %.4f, %.4f".format(lat, lon)
                                isSearching = false
                                hasSearched = true
                            }
                        }
                        else -> {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("📍 Buscar ZEPAs cercanas a mí", fontSize = 16.sp)
            }

            if (isSearching) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator()
                Text("Obteniendo ubicación GPS y calculando distancias...")
            } else if (hasSearched) {
                // Clickable GPS coordinates banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val uri = Uri.parse("geo:$userLat,$userLon?q=$userLat,$userLon(Mi Ubicación)")
                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$userLat,$userLon"))
                                context.startActivity(webIntent)
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Map, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tu ubicación actual (Ver en Maps):", fontSize = 12.sp, color = Color.Gray)
                            Text(userLocationStr.ifBlank { "GPS: %.4f, %.4f".format(userLat, userLon) }, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Top 3 ZEPAs más cercanas (Pulsa para ver detalles):", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(nearbyZepas) { item ->
                        val isExpanded = expandedZepaId == item.zepa.id_codigo
                        val distText = if (item.distanceKm < 1.0) {
                            "${(item.distanceKm * 1000).toInt()} m"
                        } else {
                            "%.1f km".format(item.distanceKm)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedZepaId = if (isExpanded) null else item.zepa.id_codigo
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.zepa.id_codigo} - ${item.zepa.nombre}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Badge(containerColor = Color(0xFF2E7D32)) {
                                        Text(distText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Provincia/Región: ${item.zepa.provincia ?: "N/D"}", fontSize = 13.sp)
                                    Text("Superficie protegida: ${item.zepa.superficie ?: 0.0} ha", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            val zLat = item.zepa.lat ?: 40.0
                                            val zLon = item.zepa.lon ?: -4.0
                                            val uri = Uri.parse("google.navigation:q=$zLat,$zLon&mode=d")
                                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                                setPackage("com.google.android.apps.maps")
                                            }
                                            try {
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$zLat,$zLon&travelmode=driving"))
                                                context.startActivity(webIntent)
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.End),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                    ) {
                                        Icon(Icons.Filled.DirectionsCar, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("🚗 Cómo llegar a la ZEPA")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun findNearbyZepas(context: Context, onResult: (List<ZepaWithDistance>, Double, Double) -> Unit) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    var lat = 42.5987
    var lon = -5.5671
    try {
        val lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (lastKnown != null && lastKnown.latitude in 27.0..44.0 && lastKnown.longitude in -18.0..5.0) {
            lat = lastKnown.latitude
            lon = lastKnown.longitude
        }
    } catch (_: SecurityException) {
        // Fallback default
    }
    findNearbyZepasWithCoords(context, lat, lon) { list ->
        onResult(list, lat, lon)
    }
}

private fun findNearbyZepasWithCoords(context: Context, userLat: Double, userLon: Double, onResult: (List<ZepaWithDistance>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val db = DatabaseProvider.getDatabase(context)
        val ccaas = db.ccaaDao().getAll()
        val allZepas = mutableListOf<ZepaEntity>()
        for (c in ccaas) {
            allZepas.addAll(db.zepaDao().getByCcaa(c.id))
        }

        val listWithDist = allZepas.map { zepa ->
            val zLat = zepa.lat ?: 40.0
            val zLon = zepa.lon ?: -4.0
            val dist = calculateDistanceKm(userLat, userLon, zLat, zLon)
            ZepaWithDistance(zepa, dist)
        }.sortedBy { it.distanceKm }.take(3)

        withContext(Dispatchers.Main) {
            onResult(listWithDist)
        }
    }
}
