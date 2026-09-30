package com.censozepa.app.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censozepa.app.data.local.DatabaseProvider
import com.censozepa.app.data.local.UserDataDatabase
import com.censozepa.app.data.remote.CensoZepaBackendClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    onBack: () -> Unit,
    onOpenGoogleDriveSync: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val sharedPrefs = remember {
        context.getSharedPreferences("censozepa_backend_prefs", Context.MODE_PRIVATE)
    }

    var serverUrl by remember {
        mutableStateOf(sharedPrefs.getString("backend_server_url", "http://10.0.2.2:3000") ?: "http://10.0.2.2:3000")
    }
    var isConnected by remember {
        mutableStateOf(sharedPrefs.getBoolean("backend_user_connected", false))
    }
    var userEmail by remember {
        mutableStateOf(sharedPrefs.getString("backend_user_email", "") ?: "")
    }

    var showCreateUserDialog by remember { mutableStateOf(false) }
    var newUserEmailInput by remember { mutableStateOf(userEmail.ifBlank { "usuario@gmail.com" }) }
    var isSyncingBackend by remember { mutableStateOf(false) }

    // Save server URL whenever edited
    fun updateServerUrl(newUrl: String) {
        serverUrl = newUrl
        sharedPrefs.edit().putString("backend_server_url", newUrl).apply()
    }

    // Backend sync function
    fun syncToBackend() {
        if (isSyncingBackend) return
        isSyncingBackend = true

        coroutineScope.launch(Dispatchers.IO) {
            val userDb = UserDataDatabase.getDatabase(context)
            val appDb = DatabaseProvider.getDatabase(context)

            val sessions = userDb.sesionDao().getAll().first()
            val pendingSessions = sessions.filter { !it.sincronizado }

            if (pendingSessions.isEmpty()) {
                withContext(Dispatchers.Main) {
                    isSyncingBackend = false
                    snackbarHostState.showSnackbar("No hay registros pendientes de guardar en CensoZEPABackend.")
                }
                return@launch
            }

            // Build JSON payload
            val registriesArray = buildJsonArray {
                for (s in pendingSessions) {
                    val avs = userDb.avistamientoDao().getBySesion(s.id).first()
                    val avsArray = buildJsonArray {
                        for (av in avs) {
                            val especie = appDb.especieDao().getByCodigo(av.id_especie)
                            add(buildJsonObject {
                                put("id_especie", av.id_especie)
                                put("nombre_comun", especie?.nombre_comun ?: "")
                                put("nombre_cientifico", especie?.nombre_cientifico ?: "")
                                put("hora", av.hora)
                                put("cantidad", av.cantidad)
                                put("alerta_fenologica", av.alerta_fenologica)
                            })
                        }
                    }

                    add(buildJsonObject {
                        put("id_sesion", s.id)
                        put("id_zepa", s.id_zepa)
                        put("fecha_hora_inicio", s.fecha_hora_inicio)
                        put("fecha_hora_fin", s.fecha_hora_fin ?: 0L)
                        put("distancia_recorrida", s.distancia_recorrida ?: 0.0)
                        put("avistamientos", avsArray)
                    })
                }
            }

            val payload = buildJsonObject {
                put("email", userEmail)
                put("total_sesiones", pendingSessions.size)
                put("registros", registriesArray)
            }

            val result = CensoZepaBackendClient.addRegistry(serverUrl, payload)

            withContext(Dispatchers.Main) {
                isSyncingBackend = false
                result.fold(
                    onSuccess = {
                        coroutineScope.launch(Dispatchers.IO) {
                            for (s in pendingSessions) {
                                userDb.sesionDao().update(s.copy(sincronizado = true))
                            }
                        }
                        snackbarHostState.showSnackbar("¡${pendingSessions.size} muestreos guardados con éxito en CensoZEPABackend!")
                    },
                    onFailure = { err ->
                        snackbarHostState.showSnackbar("Error al conectar con CensoZEPABackend: ${err.message}")
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración y Datos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Cloud Sync & Backends
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sincronización y Respaldo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Exporta o sincroniza tus muestreos ornitológicos en Google Drive o en el servidor centralizado CensoZEPABackend.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Google Drive Export Button
                    Button(
                        onClick = onOpenGoogleDriveSync,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportar a Google Drive")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // CensoZEPABackend Section
                    Text("CensoZEPABackend (Servidor Central)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { updateServerUrl(it) },
                        label = { Text("URL Servidor CensoZEPABackend") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Dns, contentDescription = null) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isConnected && userEmail.isNotBlank()) {
                        Text(
                            text = "🟢 Conectado como: $userEmail",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "⚪ No conectado a CensoZEPABackend",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Save to CensoZEPABackend Button
                    ElevatedButton(
                        onClick = {
                            if (!isConnected || userEmail.isBlank()) {
                                showCreateUserDialog = true
                            } else {
                                syncToBackend()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSyncingBackend
                    ) {
                        if (isSyncingBackend) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardando...")
                        } else {
                            Icon(Icons.Filled.Storage, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar en CensoZEPABackend")
                        }
                    }
                }
            }

            // Card 2: App Info
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Acerca de Censo ZEPA", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Versión de la App: 1.0.0", fontSize = 14.sp)
                    Text("Base de datos: Red Natura 2000 (MITECO / EEA)", fontSize = 14.sp)
                }
            }
        }
    }

    // Account Creation Dialog for CensoZEPABackend
    if (showCreateUserDialog) {
        AlertDialog(
            onDismissRequest = { showCreateUserDialog = false },
            title = {
                Text("Crear Cuenta en CensoZEPABackend", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Para sincronizar tus muestreos con el servidor centralizado, introduce tu cuenta de Google o correo electrónico. Se enviará a $serverUrl/createuser",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newUserEmailInput,
                        onValueChange = { newUserEmailInput = it },
                        label = { Text("Correo Cuenta Google") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val emailToRegister = newUserEmailInput.trim()
                        if (emailToRegister.isNotBlank()) {
                            coroutineScope.launch {
                                val result = CensoZepaBackendClient.createUser(serverUrl, emailToRegister)
                                result.fold(
                                    onSuccess = {
                                        sharedPrefs.edit()
                                            .putBoolean("backend_user_connected", true)
                                            .putString("backend_user_email", emailToRegister)
                                            .apply()
                                        isConnected = true
                                        userEmail = emailToRegister
                                        showCreateUserDialog = false
                                        snackbarHostState.showSnackbar("Cuenta vinculada con éxito. Guardando registros...")
                                        syncToBackend()
                                    },
                                    onFailure = { err ->
                                        snackbarHostState.showSnackbar("Error al crear cuenta: ${err.message}")
                                    }
                                )
                            }
                        }
                    }
                ) {
                    Text("Crear / Conectar Cuenta")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateUserDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
