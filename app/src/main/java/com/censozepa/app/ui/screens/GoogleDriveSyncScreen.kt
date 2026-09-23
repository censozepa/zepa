package com.censozepa.app.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censozepa.app.data.local.UserDataDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleDriveSyncScreenContent(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var unsyncedCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var pendingJsonExport by remember { mutableStateOf<String?>(null) }

    val loadUnsyncedCount: () -> Unit = {
        coroutineScope.launch(Dispatchers.IO) {
            val userDb = UserDataDatabase.getDatabase(context)
            val sessions = userDb.sesionDao().getAll().first()
            val pending = sessions.count { !it.sincronizado }
            withContext(Dispatchers.Main) {
                unsyncedCount = pending
                isLoading = false
            }
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        pendingJsonExport?.let { json ->
                            outputStream.write(json.toByteArray(Charsets.UTF_8))
                        }
                    }
                    coroutineScope.launch {
                        val userDb = UserDataDatabase.getDatabase(context)
                        val sessions = userDb.sesionDao().getAll().first()
                        for (s in sessions) {
                            if (!s.sincronizado) {
                                userDb.sesionDao().update(s.copy(sincronizado = true))
                            }
                        }
                        loadUnsyncedCount()
                        snackbarHostState.showSnackbar("¡Fichero guardado y sincronizado con éxito!")
                    }
                } catch (e: Exception) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Error al escribir el fichero: ${e.message}")
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadUnsyncedCount()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exportación de Muestreos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Estado de Sincronización", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (unsyncedCount > 0)
                                "⚠️ Tienes $unsyncedCount muestreos pendientes de subir a la nube."
                            else
                                "✅ Todos los muestreos están sincronizados o respaldados.",
                            fontSize = 14.sp,
                            color = if (unsyncedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Exportación Nativa", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pulsa el botón inferior para abrir el explorador de archivos seguro de tu dispositivo. Podrás seleccionar directamente tu cuenta de Google Drive en el menú lateral para subir todos los muestreos pendientes de forma automática.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    val userDb = UserDataDatabase.getDatabase(context)
                                    val sessions = userDb.sesionDao().getAll().first()
                                    val pendingSessions = sessions.filter { !it.sincronizado }

                                    if (pendingSessions.isEmpty()) {
                                        withContext(Dispatchers.Main) {
                                            snackbarHostState.showSnackbar("No hay muestreos pendientes de sincronizar.")
                                        }
                                        return@launch
                                    }

                                    val exportData = mutableMapOf<String, Any>()
                                    val sessionsList = mutableListOf<Map<String, Any>>()
                                    for (s in pendingSessions) {
                                        val avs = userDb.avistamientoDao().getBySesion(s.id).first()
                                        val sMap = mapOf(
                                            "id" to s.id,
                                            "id_zepa" to s.id_zepa,
                                            "fecha_hora_inicio" to s.fecha_hora_inicio,
                                            "fecha_hora_fin" to (s.fecha_hora_fin ?: 0L),
                                            "distancia_recorrida" to (s.distancia_recorrida ?: 0.0),
                                            "avistamientos" to avs.map { av ->
                                                mapOf(
                                                    "id_especie" to av.id_especie,
                                                    "hora" to av.hora,
                                                    "cantidad" to av.cantidad,
                                                    "alerta_fenologica" to av.alerta_fenologica,
                                                    "notas" to (av.notas ?: "")
                                                )
                                            }
                                        )
                                        sessionsList.add(sMap)
                                    }
                                    exportData["muestreos"] = sessionsList
                                    val jsonString = JSONObject(exportData as Map<*, *>).toString(2)
                                    pendingJsonExport = jsonString

                                    withContext(Dispatchers.Main) {
                                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                            addCategory(Intent.CATEGORY_OPENABLE)
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_TITLE, "censo_zepa_muestreos_${System.currentTimeMillis()}.json")
                                        }
                                        createDocumentLauncher.launch(intent)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = unsyncedCount > 0
                        ) {
                            Icon(Icons.Filled.CloudSync, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exportar y Guardar (Google Drive)")
                        }
                    }
                }
            }
        }
    }
}
