package com.censozepa.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
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
import com.censozepa.app.data.local.UserDataDatabase
import com.censozepa.app.data.local.entity.AvistamientoEntity
import com.censozepa.app.data.local.entity.EspecieEntity
import com.censozepa.app.data.local.entity.SesionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationsScreenContent(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var sessions by remember { mutableStateOf<List<SesionEntity>>(emptyList()) }
    var sightingsMap by remember { mutableStateOf<Map<Int, List<AvistamientoEntity>>>(emptyMap()) }
    var speciesList by remember { mutableStateOf<List<EspecieEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Confirmation dialog state
    var sessionToDelete by remember { mutableStateOf<SesionEntity?>(null) }
    var showDeleteAllSyncedDialog by remember { mutableStateOf(false) }

    fun loadData() {
        coroutineScope.launch(Dispatchers.IO) {
            val db = DatabaseProvider.getDatabase(context)
            val userDb = UserDataDatabase.getDatabase(context)
            val loadedSessions = userDb.sesionDao().getAll().first()
            val loadedSpecies = db.especieDao().getAll().first()
            val map = mutableMapOf<Int, List<AvistamientoEntity>>()
            for (s in loadedSessions) {
                map[s.id] = userDb.avistamientoDao().getBySesion(s.id).first()
            }
            withContext(Dispatchers.Main) {
                sessions = loadedSessions
                speciesList = loadedSpecies
                sightingsMap = map
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    val syncedCount = sessions.count { it.sincronizado }
    val unsyncedCount = sessions.count { !it.sincronizado }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Observaciones y Muestreos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Resumen de Muestreos", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("✅ Sincronizados: $syncedCount", fontSize = 14.sp)
                            Text("⚠️ Pendientes: $unsyncedCount", fontSize = 14.sp)
                        }
                        
                        if (syncedCount > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showDeleteAllSyncedDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Borrar todos los sincronizados locales")
                            }
                        }
                    }
                }

                if (sessions.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No hay observaciones registradas todavía.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sessions) { session ->
                            val sessionSightings = sightingsMap[session.id] ?: emptyList()
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text("Jornada #${session.id} (ZEPA: ${session.id_zepa})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Inicio: ${Date(session.fecha_hora_inicio)}", fontSize = 12.sp, color = Color.Gray)
                                            if (session.fecha_hora_fin != null) {
                                                Text("Fin: ${Date(session.fecha_hora_fin)}", fontSize = 12.sp, color = Color.Gray)
                                            } else {
                                                Text("Estado: En curso", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            // Sync indicator icon
                                            Icon(
                                                imageVector = if (session.sincronizado) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                                                contentDescription = if (session.sincronizado) "Sincronizado con Google Drive" else "Pendiente de sincronizar con Google Drive",
                                                tint = if (session.sincronizado) Color(0xFF2E7D32) else Color(0xFFEF6C00),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            IconButton(onClick = {
                                                sessionToDelete = session
                                            }) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Borrar registro", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
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
    }

    // Smart Delete Confirmation Dialog for Single Session
    if (sessionToDelete != null) {
        val isSynced = sessionToDelete!!.sincronizado
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = {
                Text(if (isSynced) "🗑️ Borrar muestreo sincronizado" else "⚠️ ¡Atención! Muestreo no sincronizado")
            },
            text = {
                Text(
                    if (isSynced)
                        "Este muestreo (Jornada #${sessionToDelete!!.id}) ya ha sido exportado. Puedes borrarlo con total seguridad pues sus datos están respaldados (ej. Google Drive)."
                    else
                        "¡Este muestreo (Jornada #${sessionToDelete!!.id}) NO ha sido exportado todavía! Si lo borras ahora, los datos se perderán de este dispositivo sin haber sido respaldados. ¿Estás seguro de continuar?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val sToDelete = sessionToDelete
                        sessionToDelete = null
                        if (sToDelete != null) {
                            coroutineScope.launch(Dispatchers.IO) {
                                val userDb = UserDataDatabase.getDatabase(context)
                                userDb.avistamientoDao().deleteBySesion(sToDelete.id)
                                userDb.sesionDao().deleteById(sToDelete.id)
                                loadData()
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = if (isSynced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isSynced) "Sí, borrar" else "Borrar de todos modos")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete All Synced Dialog
    if (showDeleteAllSyncedDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllSyncedDialog = false },
            title = { Text("Borrar muestreos sincronizados") },
            text = { 
                Text("Se van a borrar localmente $syncedCount muestreos que ya fueron exportados. Estos datos seguirán a salvo en tus copias de seguridad (Google Drive) y no se perderán de la nube.\n\n¿Estás seguro de que deseas liberar este espacio en tu dispositivo?") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAllSyncedDialog = false
                        coroutineScope.launch(Dispatchers.IO) {
                            val userDb = UserDataDatabase.getDatabase(context)
                            val syncedSessions = sessions.filter { it.sincronizado }
                            for (s in syncedSessions) {
                                userDb.avistamientoDao().deleteBySesion(s.id)
                                userDb.sesionDao().deleteById(s.id)
                            }
                            loadData()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sí, borrar locales")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllSyncedDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
