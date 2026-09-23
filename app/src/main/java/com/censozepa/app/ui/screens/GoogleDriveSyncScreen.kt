package com.censozepa.app.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Folder
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleDriveSyncScreenContent(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val prefs = remember {
        context.getSharedPreferences("google_drive_prefs", Context.MODE_PRIVATE)
    }

    var configuredFolder by remember {
        mutableStateOf(prefs.getString("gdrive_folder", null))
    }

    var unsyncedCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFolderOption by remember { mutableStateOf("📁 Censo ZEPA / Sincronización") }

    val mockFolders = listOf(
        "📁 Mi Unidad (Raíz)",
        "📁 Censo ZEPA / Sincronización",
        "📁 Muestreos de Campo ZEPA",
        "📁 Documentos / Ornitología"
    )

    fun loadUnsyncedCount() {
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

    LaunchedEffect(Unit) {
        loadUnsyncedCount()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sincronización con Google Drive") },
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
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
                                "⚠️ Tienes $unsyncedCount muestreos pendientes de sincronizar con Google Drive."
                            else
                                "✅ Todos los muestreos están sincronizados.",
                            fontSize = 14.sp,
                            color = if (unsyncedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (configuredFolder == null) {
                    // First time configuration: Choose folder from list
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Configuración Inicial", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Selecciona la carpeta de tu Google Drive donde deseas almacenar los muestreos. Esta elección quedará guardada permanentemente.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            LazyColumn(
                                modifier = Modifier
                                    .height(180.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(mockFolders) { folder ->
                                    val isSelected = selectedFolderOption == folder
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedFolderOption = folder },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(Icons.Filled.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text(folder, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    prefs.edit().putString("gdrive_folder", selectedFolderOption).apply()
                                    configuredFolder = selectedFolderOption
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Carpeta '$selectedFolderOption' guardada con éxito.")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Guardar carpeta de destino")
                            }
                        }
                    }
                } else {
                    // Already configured
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Carpeta de Destino en Google Drive", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(configuredFolder ?: "", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        val userDb = UserDataDatabase.getDatabase(context)
                                        val sessions = userDb.sesionDao().getAll().first()
                                        for (s in sessions) {
                                            if (!s.sincronizado) {
                                                userDb.sesionDao().update(s.copy(sincronizado = true))
                                            }
                                        }
                                        loadUnsyncedCount()
                                        withContext(Dispatchers.Main) {
                                            snackbarHostState.showSnackbar("¡Sincronización completada con éxito en '$configuredFolder'!")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = unsyncedCount > 0
                            ) {
                                Icon(Icons.Filled.CloudSync, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sincronizar ahora con Google Drive")
                            }
                        }
                    }
                }
            }
        }
    }
}
