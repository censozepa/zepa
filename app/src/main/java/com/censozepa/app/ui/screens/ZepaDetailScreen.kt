package com.censozepa.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.censozepa.app.data.local.DatabaseProvider
import com.censozepa.app.data.local.UserDataDatabase
import com.censozepa.app.data.local.entity.AvistamientoEntity
import com.censozepa.app.data.local.entity.EspecieEntity
import com.censozepa.app.data.local.entity.FavoriteEntity
import com.censozepa.app.data.local.entity.SesionEntity
import com.censozepa.app.data.local.entity.ZepaEntity
import com.censozepa.app.service.ZepaCensoService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

fun startCensusSession(context: Context, zepaId: String, zepaName: String?, onStarted: (Int) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val userDb = UserDataDatabase.getDatabase(context)
        val newSession = SesionEntity(
            id_zepa = zepaId,
            fecha_hora_inicio = System.currentTimeMillis(),
            fecha_hora_fin = null,
            distancia_recorrida = 0.0,
            track_gps_json = null
        )
        val id = userDb.sesionDao().insert(newSession).toInt()
        
        val intent = Intent(context, ZepaCensoService::class.java).apply {
            putExtra("ZEPA_ID", zepaId)
            putExtra("ZEPA_NAME", zepaName ?: zepaId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        withContext(Dispatchers.Main) {
            onStarted(id)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZepaDetailScreenContent(
    zepaId: String,
    onBack: () -> Unit,
    onHomeClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var zepa by remember { mutableStateOf<ZepaEntity?>(null) }
    var speciesList by remember { mutableStateOf<List<EspecieEntity>>(emptyList()) }
    var isFavorite by remember { mutableStateOf(false) }
    
    // Active session state
    var activeSessionId by remember { mutableStateOf<Int?>(null) }
    var sessionTimeSeconds by remember { mutableStateOf(0L) }
    
    // Sighting input state
    var searchQuery by remember { mutableStateOf("") }
    var selectedSpecies by remember { mutableStateOf<EspecieEntity?>(null) }
    var quantity by remember { mutableStateOf(1) }
    var sessionSightings by remember { mutableStateOf<List<AvistamientoEntity>>(emptyList()) }
    var isPhenologyAlert by remember { mutableStateOf(false) }
    
    // Dialog states
    var showSightingsDialog by remember { mutableStateOf(false) }

    // Notification permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        startCensusSession(context, zepaId, zepa?.nombre) { sessionId ->
            activeSessionId = sessionId
            sessionTimeSeconds = 0L
            sessionSightings = emptyList()
        }
    }

    // Load ZEPA, Species for this specific ZEPA, and Favorite status
    LaunchedEffect(zepaId) {
        withContext(Dispatchers.IO) {
            val db = DatabaseProvider.getDatabase(context)
            val userDb = UserDataDatabase.getDatabase(context)
            zepa = db.zepaDao().getById(zepaId)
            speciesList = db.especieDao().getSpeciesForZepa(zepaId)
            isFavorite = userDb.favoriteDao().isFavorite(zepaId)
        }
    }

    // Check phenology anomaly when selectedSpecies changes
    LaunchedEffect(selectedSpecies) {
        if (selectedSpecies != null) {
            withContext(Dispatchers.IO) {
                val db = DatabaseProvider.getDatabase(context)
                val fen = db.fenologiaZepaDao().getByZepaAndEspecie(zepaId, selectedSpecies!!.codigo_n2000)
                if (fen != null) {
                    val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
                    val currentStatus = when (currentMonth) {
                        1 -> fen.estatus_ene
                        2 -> fen.estatus_feb
                        3 -> fen.estatus_mar
                        4 -> fen.estatus_abr
                        5 -> fen.estatus_may
                        6 -> fen.estatus_jun
                        7 -> fen.estatus_jul
                        8 -> fen.estatus_ago
                        9 -> fen.estatus_sep
                        10 -> fen.estatus_oct
                        11 -> fen.estatus_nov
                        12 -> fen.estatus_dic
                        else -> null
                    }
                    val hasAnyStatus = listOf(
                        fen.estatus_ene, fen.estatus_feb, fen.estatus_mar, fen.estatus_abr,
                        fen.estatus_may, fen.estatus_jun, fen.estatus_jul, fen.estatus_ago,
                        fen.estatus_sep, fen.estatus_oct, fen.estatus_nov, fen.estatus_dic
                    ).any { !it.isNullOrBlank() && it != "-" }

                    val isAbsentNow = currentStatus.isNullOrBlank() || currentStatus == "-"
                    withContext(Dispatchers.Main) {
                        isPhenologyAlert = hasAnyStatus && isAbsentNow
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        isPhenologyAlert = false
                    }
                }
            }
        } else {
            isPhenologyAlert = false
        }
    }

    // Timer coroutine when session is active
    LaunchedEffect(activeSessionId) {
        if (activeSessionId != null) {
            while (true) {
                delay(1000L)
                sessionTimeSeconds++
            }
        }
    }

    // Format timer
    val minutes = sessionTimeSeconds / 60
    val seconds = sessionTimeSeconds % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(zepa?.nombre ?: "Censo ZEPA") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    // Home button (visible only when session is NOT active)
                    if (activeSessionId == null) {
                        IconButton(onClick = onHomeClick) {
                            Icon(Icons.Filled.Home, contentDescription = "Menú principal")
                        }
                    }

                    // Favorite button
                    IconButton(onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val userDb = UserDataDatabase.getDatabase(context)
                            if (isFavorite) {
                                userDb.favoriteDao().removeFavorite(zepaId)
                                withContext(Dispatchers.Main) { isFavorite = false }
                            } else {
                                userDb.favoriteDao().addFavorite(FavoriteEntity(zepaId))
                                withContext(Dispatchers.Main) { isFavorite = true }
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    
                    // Top-right button: Avistamientos en esta jornada with strong forest green badge
                    IconButton(onClick = {
                        val sesId = activeSessionId
                        if (sesId != null) {
                            coroutineScope.launch(Dispatchers.IO) {
                                val userDb = UserDataDatabase.getDatabase(context)
                                val sightings = userDb.avistamientoDao().getBySesion(sesId).first()
                                withContext(Dispatchers.Main) {
                                    sessionSightings = sightings
                                    showSightingsDialog = true
                                }
                            }
                        }
                    }) {
                        BadgedBox(
                            badge = {
                                if (activeSessionId != null && sessionSightings.isNotEmpty()) {
                                    Badge(
                                        containerColor = Color(0xFF2E7D32), // Strong forest green
                                        contentColor = Color.White
                                    ) {
                                        Text("${sessionSightings.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.List, contentDescription = "Avistamientos de la jornada")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (zepa == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Block 1: ZEPA Info Header (Clean & concise)
                val art4Count = speciesList.count { it.categoria == "Art. 4" }
                val rel33Count = speciesList.count { it.categoria != "Art. 4" }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Código ZEPA: ${zepa!!.id_codigo}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Total especies catalogadas: ${speciesList.size}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Text("• Especies Art. 4 Directiva Aves: $art4Count", fontSize = 14.sp)
                        Text("• Otras especies relevantes (3.3): $rel33Count", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (activeSessionId == null) {
                    // Start Session Button
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                startCensusSession(context, zepaId, zepa?.nombre) { sessionId ->
                                    activeSessionId = sessionId
                                    sessionTimeSeconds = 0L
                                    sessionSightings = emptyList()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("🚀 Empezar Conteo (Iniciar Jornada)", fontSize = 16.sp)
                    }
                } else {
                    // Block 2: Active Session Controls (Start/Stop counter)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Muestreo en Curso", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("⏱️ Tiempo: $formattedTime", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    val sessionId = activeSessionId
                                    if (sessionId != null) {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val userDb = UserDataDatabase.getDatabase(context)
                                            val sessions = userDb.sesionDao().getAll().first()
                                            val currentSession = sessions.find { it.id == sessionId }
                                            if (currentSession != null) {
                                                userDb.sesionDao().update(currentSession.copy(fecha_hora_fin = System.currentTimeMillis()))
                                            }
                                            
                                            // Stop foreground service
                                            val intent = Intent(context, ZepaCensoService::class.java).apply {
                                                action = "STOP_SERVICE"
                                            }
                                            context.startService(intent)

                                            withContext(Dispatchers.Main) {
                                                activeSessionId = null
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Filled.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Parar Jornada")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Block 3: Species Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar entre ${speciesList.size} especies...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Block 4: Species List
                    val filteredSpecies = speciesList.filter {
                        (it.nombre_comun?.normalizeAccents()?.contains(searchQuery.normalizeAccents(), ignoreCase = true) == true) ||
                        (it.nombre_cientifico.normalizeAccents().contains(searchQuery.normalizeAccents(), ignoreCase = true)) ||
                        (it.codigo_n2000.normalizeAccents().contains(searchQuery.normalizeAccents(), ignoreCase = true))
                    }

                    if (selectedSpecies == null) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            items(filteredSpecies) { especie ->
                                val common = especie.nombre_comun
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            text = common ?: especie.nombre_cientifico,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    },
                                    supportingContent = {
                                        if (common != null) {
                                            Text(
                                                text = "${especie.nombre_cientifico} · ${especie.categoria ?: "N/D"}",
                                                fontSize = 12.sp,
                                                fontStyle = FontStyle.Italic
                                            )
                                        } else {
                                            Text(
                                                text = especie.categoria ?: "N/D",
                                                fontSize = 12.sp
                                            )
                                        }
                                    },
                                    trailingContent = {
                                        val assetPath = especie.foto_asset
                                        if (assetPath != null) {
                                            AsyncImage(
                                                model = "file:///android_asset/$assetPath",
                                                contentDescription = common ?: especie.nombre_cientifico,
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedSpecies = especie }
                                )
                            }
                        }
                    } else {
                        // Selected Species & Quantity Stepper
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Especie seleccionada:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                val selCommon = selectedSpecies!!.nombre_comun
                                Text(
                                    text = selCommon ?: selectedSpecies!!.nombre_cientifico,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                if (selCommon != null) {
                                    Text(
                                        text = "${selectedSpecies!!.nombre_cientifico} · ${selectedSpecies!!.categoria ?: "N/D"}",
                                        fontSize = 12.sp,
                                        fontStyle = FontStyle.Italic
                                    )
                                } else {
                                    Text(
                                        text = selectedSpecies!!.categoria ?: "N/D",
                                        fontSize = 12.sp
                                    )
                                }

                                if (isPhenologyAlert) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                        border = BorderStroke(1.dp, Color(0xFFEF6C00))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "⚠️ Alerta Fenológica: No es común avistar esta especie en la fecha actual según el SDF de la ZEPA. ¿Está seguro de que es correcta?",
                                                fontSize = 13.sp,
                                                color = Color(0xFFE65100),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Ejemplares: $quantity")
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { if (quantity > 1) quantity-- }) {
                                            Icon(Icons.Filled.Remove, contentDescription = "Menos")
                                        }
                                        Text("$quantity", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        IconButton(onClick = { quantity++ }) {
                                            Icon(Icons.Filled.Add, contentDescription = "Más")
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { selectedSpecies = null; quantity = 1; isPhenologyAlert = false }) {
                                        Text("Cambiar")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            val sesId = activeSessionId
                                            val sp = selectedSpecies
                                            if (sesId != null && sp != null) {
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    val userDb = UserDataDatabase.getDatabase(context)
                                                    val avistamiento = AvistamientoEntity(
                                                        id_sesion = sesId,
                                                        id_especie = sp.codigo_n2000,
                                                        hora = System.currentTimeMillis(),
                                                        latitud = 0.0,
                                                        longitud = 0.0,
                                                        cantidad = quantity,
                                                        alerta_fenologica = isPhenologyAlert,
                                                        notas = if (isPhenologyAlert) "Alerta fenológica: fuera de época típica" else null
                                                    )
                                                    userDb.avistamientoDao().insert(avistamiento)
                                                    val updatedSightings = userDb.avistamientoDao().getBySesion(sesId).first()
                                                    withContext(Dispatchers.Main) {
                                                        sessionSightings = updatedSightings
                                                        selectedSpecies = null
                                                        quantity = 1
                                                        searchQuery = ""
                                                        isPhenologyAlert = false
                                                    }
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Filled.Check, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("OK / Registrar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Sightings Detail Dialog with Back Arrow
    if (showSightingsDialog) {
        AlertDialog(
            onDismissRequest = { showSightingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showSightingsDialog = false }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Avistamientos (${sessionSightings.size})")
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .height(350.dp)
                        .fillMaxWidth()
                ) {
                    items(sessionSightings) { av ->
                        val esp = speciesList.find { it.codigo_n2000 == av.id_especie }
                        val displayCommon = esp?.nombre_comun ?: esp?.nombre_cientifico ?: av.id_especie
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(displayCommon, fontWeight = FontWeight.Bold)
                                        if (av.alerta_fenologica) {
                                            Badge(containerColor = Color(0xFFEF6C00)) { Text("Fuera de época", color = Color.White, fontSize = 10.sp) }
                                        }
                                    }
                                    if (esp?.nombre_comun != null) {
                                        Text(esp.nombre_cientifico, fontSize = 12.sp, fontStyle = FontStyle.Italic)
                                    }
                                    Text("Cantidad: ${av.cantidad}", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSightingsDialog = false }) {
                    Text("Volver")
                }
            }
        )
    }
}
