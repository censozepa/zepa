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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    onBack: () -> Unit,
    onOpenGoogleDriveSync: () -> Unit,
    onOpenCensoZepaBackendSync: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("censozepa_backend_prefs", Context.MODE_PRIVATE)
    }

    var serverUrl by remember {
        mutableStateOf(sharedPrefs.getString("backend_server_url", "http://10.0.2.2:3000") ?: "http://10.0.2.2:3000")
    }
    val isConnected = sharedPrefs.getBoolean("backend_user_connected", false)
    val userEmail = sharedPrefs.getString("backend_user_email", "") ?: ""

    fun updateServerUrl(newUrl: String) {
        serverUrl = newUrl
        sharedPrefs.edit().putString("backend_server_url", newUrl).apply()
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
        }
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

                    // Navigate to CensoZEPABackend Sync Screen Button
                    Button(
                        onClick = onOpenCensoZepaBackendSync,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Storage, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar en CensoZEPABackend")
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
}
