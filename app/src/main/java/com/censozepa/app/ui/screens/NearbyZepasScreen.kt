package com.censozepa.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
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
    var userLocationStr by remember { mutableStateOf("") }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            findNearbyZepas(context) { list, lat, lon ->
                nearbyZepas = list
                userLocationStr = "GPS: %.4f, %.4f".format(lat, lon)
                isSearching = false
                hasSearched = true
            }
        } else {
            isSearching = false
            hasSearched = true
            findNearbyZepasWithCoords(context, 42.5987, -5.5671) { list ->
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
                if (userLocationStr.isNotBlank()) {
                    Text(userLocationStr, fontSize = 12.sp, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.secondary)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Top 3 ZEPAs más cercanas:", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(nearbyZepas) { item ->
                        val distText = if (item.distanceKm < 1.0) {
                            "${(item.distanceKm * 1000).toInt()} m"
                        } else {
                            "%.1f km".format(item.distanceKm)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onZepaClick(item.zepa.id_codigo) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
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
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Provincia/Región: ${item.zepa.provincia ?: "N/D"} · Superficie: ${item.zepa.superficie ?: 0.0} ha", fontSize = 12.sp)
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
    var lat = 42.5987 // Default León
    var lon = -5.5671
    try {
        val lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (lastKnown != null && lastKnown.latitude in 27.0..44.0 && lastKnown.longitude in -18.0..5.0) {
            // Within Spain bounding box (including Canary Islands)
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
