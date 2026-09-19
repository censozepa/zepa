package com.censozepa.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.censozepa.app.data.local.AppDatabase
import com.censozepa.app.data.local.entity.ZepaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZepaDetailScreenContent(
    zepaId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var zepa by remember { mutableStateOf<ZepaEntity?>(null) }
    var birdsCount by remember { mutableStateOf(0) }

    LaunchedEffect(zepaId) {
        withContext(Dispatchers.IO) {
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "censozepa.db")
                .createFromAsset("database/censozepa.db")
                .build()
            zepa = db.zepaDao().getById(zepaId)
            birdsCount = db.fenologiaZepaDao().getByZepa(zepaId).size
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(zepa?.nombre ?: "Cargando...") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
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
                Text("Código: ${zepa!!.id_codigo}")
                Text("Superficie: ${zepa!!.superficie} ha")
                Text("Total aves (Catálogo unificado): $birdsCount")
                // En un futuro añadiremos el mapa de OSMDroid aquí
            }
        }
    }
}
