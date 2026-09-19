package com.censozepa.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censozepa.app.data.local.DatabaseProvider
import com.censozepa.app.data.local.entity.AvistamientoEntity
import com.censozepa.app.data.local.entity.EspecieEntity
import com.censozepa.app.data.local.entity.SesionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationsScreenContent(onBack: () -> Unit) {
    val context = LocalContext.current
    var sessions by remember { mutableStateOf<List<SesionEntity>>(emptyList()) }
    var sightingsMap by remember { mutableStateOf<Map<Int, List<AvistamientoEntity>>>(emptyMap()) }
    var speciesList by remember { mutableStateOf<List<EspecieEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = DatabaseProvider.getDatabase(context)
            val loadedSessions = db.sesionDao().getAll().first()
            val loadedSpecies = db.especieDao().getAll().first()
            val map = mutableMapOf<Int, List<AvistamientoEntity>>()
            for (s in loadedSessions) {
                map[s.id] = db.avistamientoDao().getBySesion(s.id).first()
            }
            withContext(Dispatchers.Main) {
                sessions = loadedSessions
                speciesList = loadedSpecies
                sightingsMap = map
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Observaciones y Muestreos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (sessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay observaciones registradas todavía.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sessions) { session ->
                    val sessionSightings = sightingsMap[session.id] ?: emptyList()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Jornada #${session.id} (ZEPA: ${session.id_zepa})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Inicio: ${Date(session.fecha_hora_inicio)}", fontSize = 12.sp, color = Color.Gray)
                            if (session.fecha_hora_fin != null) {
                                Text("Fin: ${Date(session.fecha_hora_fin)}", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                Text("Estado: En curso", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Avistamientos (${sessionSightings.size}):", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            
                            if (sessionSightings.isEmpty()) {
                                Text("Sin avistamientos registrados en esta sesión.", fontSize = 12.sp, fontStyle = FontStyle.Italic)
                            } else {
                                sessionSightings.forEach { av ->
                                    val sp = speciesList.find { it.codigo_n2000 == av.id_especie }
                                    Text("• ${sp?.nombre_comun ?: av.id_especie}: ${av.cantidad} ejemplares", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
