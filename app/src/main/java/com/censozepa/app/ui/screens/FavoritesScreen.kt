package com.censozepa.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censozepa.app.data.local.DatabaseProvider
import com.censozepa.app.data.local.entity.FavoriteEntity
import com.censozepa.app.data.local.entity.ZepaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreenContent(
    onBack: () -> Unit,
    onZepaClick: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var favorites by remember { mutableStateOf<List<FavoriteEntity>>(emptyList()) }
    var allZepas by remember { mutableStateOf<List<ZepaEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = DatabaseProvider.getDatabase(context)
            val favs = db.favoriteDao().getAllFavorites().first()
            val zepas = mutableListOf<ZepaEntity>()
            for (f in favs) {
                val z = db.zepaDao().getById(f.zepaId)
                if (z != null) zepas.add(z)
            }
            withContext(Dispatchers.Main) {
                favorites = favs
                allZepas = zepas
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ZEPAs Favoritas") },
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
        } else if (allZepas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes ZEPAs favoritas guardadas.\nMarca ZEPAs como favoritas para acceso rápido.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allZepas) { zepa ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onZepaClick(zepa.id_codigo) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${zepa.id_codigo} - ${zepa.nombre}", fontWeight = FontWeight.Bold)
                                Text("Provincia: ${zepa.provincia ?: "N/D"}", fontSize = 12.sp)
                            }
                            IconButton(onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    val db = DatabaseProvider.getDatabase(context)
                                    db.favoriteDao().removeFavorite(zepa.id_codigo)
                                    val favs = db.favoriteDao().getAllFavorites().first()
                                    val zepas = mutableListOf<ZepaEntity>()
                                    for (f in favs) {
                                        val z = db.zepaDao().getById(f.zepaId)
                                        if (z != null) zepas.add(z)
                                    }
                                    withContext(Dispatchers.Main) {
                                        favorites = favs
                                        allZepas = zepas
                                    }
                                }
                            }) {
                                Icon(Icons.Filled.Bookmark, contentDescription = "Quitar de favoritos", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
