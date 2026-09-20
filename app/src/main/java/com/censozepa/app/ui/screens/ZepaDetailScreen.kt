package com.censozepa.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censozepa.app.data.local.DatabaseProvider
import com.censozepa.app.data.local.entity.AvistamientoEntity
import com.censozepa.app.data.local.entity.EspecieEntity
import com.censozepa.app.data.local.entity.FavoriteEntity
import com.censozepa.app.data.local.entity.SesionEntity
import com.censozepa.app.data.local.entity.ZepaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZepaDetailScreenContent(
    zepaId: String,
    onBack: () -> Unit
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
    
    // History dialog state
    var showHistory by remember { mutableStateOf(false) }
    var pastSessions by remember { mutableStateOf<List<SesionEntity>>(emptyList()) }

    // Load ZEPA, Species for this specific ZEPA, and Favorite status
    LaunchedEffect(zepaId) {
        withContext(Dispatchers.IO) {
            val db = DatabaseProvider.getDatabase(context)
            zepa = db.zepaDao().getById(zepaId)
            speciesList = db.especieDao().getSpeciesForZepa(zepaId)
            isFavorite = db.favoriteDao().isFavorite(zepaId)
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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val db = DatabaseProvider.getDatabase(context)
                            if (isFavorite) {
                                db.favoriteDao().removeFavorite(zepaId)
                                withContext(Dispatchers.Main) { isFavorite = false }
                            } else {
                                db.favoriteDao().addFavorite(FavoriteEntity(zepaId))
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
                    IconButton(onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val db = DatabaseProvider.getDatabase(context)
                            pastSessions = db.sesionDao().getAll().first()
                            withContext(Dispatchers.Main) {
                                showHistory = true
                            }
                        }
                    }) {
                        Icon(Icons.Filled.History, contentDescription = "Historial")
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
                // ZEPA Info Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Código: ${zepa!!.id_codigo}", fontWeight = FontWeight.Bold)
                        Text("Provincia: ${zepa!!.provincia ?: "N/D"}")
                        Text("Superficie: ${zepa!!.superficie ?: 0.0} ha")
                        Text("Especies catalogadas en esta ZEPA: ${speciesList.size}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (activeSessionId == null) {
                    // Start Session Button
                    Button(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                val db = DatabaseProvider.getDatabase(context)
                                val newSession = SesionEntity(
                                    id_zepa = zepaId,
                                    fecha_hora_inicio = System.currentTimeMillis(),
                                    fecha_hora_fin = null,
                                    distancia_recorrida = 0.0,
                                    track_gps_json = null
                                )
                                val id = db.sesionDao().insert(newSession).toInt()
                                withContext(Dispatchers.Main) {
                                    activeSessionId = id
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
                    // Active Session Controls
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
                                            val db = DatabaseProvider.getDatabase(context)
                                            val sessions = db.sesionDao().getAll().first()
                                            val currentSession = sessions.find { it.id == sessionId }
                                            if (currentSession != null) {
                                                db.sesionDao().update(currentSession.copy(fecha_hora_fin = System.currentTimeMillis()))
                                            }
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

                    // Species Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar entre ${speciesList.size} especies de esta ZEPA...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filtered Species List
                    val filteredSpecies = speciesList.filter {
                        (it.nombre_comun?.contains(searchQuery, ignoreCase = true) == true) ||
                        (it.nombre_cientifico.contains(searchQuery, ignoreCase = true)) ||
                        (it.codigo_n2000.contains(searchQuery, ignoreCase = true))
                    }

                    if (selectedSpecies == null) {
                        LazyColumn(
                            modifier = Modifier
                                .height(180.dp)
                                .fillMaxWidth()
                        ) {
                            items(filteredSpecies) { especie ->
                                ListItem(
                                    headlineContent = { Text(especie.nombre_comun ?: especie.codigo_n2000, fontWeight = FontWeight.Bold) },
                                    supportingContent = { Text("${especie.nombre_cientifico} (${especie.categoria})") },
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
                                Text(selectedSpecies!!.nombre_comun ?: selectedSpecies!!.codigo_n2000, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("${selectedSpecies!!.nombre_cientifico} · ${selectedSpecies!!.categoria}", fontSize = 12.sp, fontStyle = FontStyle.Italic)

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
                                    TextButton(onClick = { selectedSpecies = null; quantity = 1 }) {
                                        Text("Cambiar")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            val sesId = activeSessionId
                                            val sp = selectedSpecies
                                            if (sesId != null && sp != null) {
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    val db = DatabaseProvider.getDatabase(context)
                                                    val avistamiento = AvistamientoEntity(
                                                        id_sesion = sesId,
                                                        id_especie = sp.codigo_n2000,
                                                        hora = System.currentTimeMillis(),
                                                        latitud = 0.0,
                                                        longitud = 0.0,
                                                        cantidad = quantity,
                                                        alerta_fenologica = false,
                                                        notas = null
                                                    )
                                                    db.avistamientoDao().insert(avistamiento)
                                                    val updatedSightings = db.avistamientoDao().getBySesion(sesId).first()
                                                    withContext(Dispatchers.Main) {
                                                        sessionSightings = updatedSightings
                                                        selectedSpecies = null
                                                        quantity = 1
                                                        searchQuery = ""
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Avistamientos en esta jornada (${sessionSightings.size}):", fontWeight = FontWeight.Bold)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(sessionSightings) { av ->
                            val esp = speciesList.find { it.codigo_n2000 == av.id_especie }
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
                                        Text(esp?.nombre_comun ?: av.id_especie, fontWeight = FontWeight.Bold)
                                        Text("Cantidad: ${av.cantidad}", fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // History Dialog
    if (showHistory) {
        AlertDialog(
            onDismissRequest = { showHistory = false },
            title = { Text("Historial de Muestreos") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(pastSessions) { sesion ->
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Sesión #${sesion.id} - ZEPA: ${sesion.id_zepa}", fontWeight = FontWeight.Bold)
                            Text("Inicio: ${Date(sesion.fecha_hora_inicio)}")
                            if (sesion.fecha_hora_fin != null) {
                                Text("Fin: ${Date(sesion.fecha_hora_fin)}")
                            } else {
                                Text("Estado: En curso")
                            }
                            HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistory = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
