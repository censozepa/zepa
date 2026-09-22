package com.censozepa.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censozepa.app.data.local.DatabaseProvider
import com.censozepa.app.data.local.UserDataDatabase
import com.censozepa.app.data.local.entity.FavoriteEntity
import com.censozepa.app.data.local.entity.ZepaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Normalizer

fun String.normalizeAccents(): String {
    val normal = Normalizer.normalize(this, Normalizer.Form.NFD)
    return Regex("\\p{InCombiningDiacriticalMarks}+").replace(normal, "")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreenContent(
    onBack: () -> Unit,
    onZepaClick: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var favorites by remember { mutableStateOf<List<FavoriteEntity>>(emptyList()) }
    var favoriteZepas by remember { mutableStateOf<List<ZepaEntity>>(emptyList()) }
    var allZepas by remember { mutableStateOf<List<ZepaEntity>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    fun loadData() {
        coroutineScope.launch(Dispatchers.IO) {
            val db = DatabaseProvider.getDatabase(context)
            val userDb = UserDataDatabase.getDatabase(context)
            val favs = userDb.favoriteDao().getAllFavorites().first()
            val zepasFav = mutableListOf<ZepaEntity>()
            for (f in favs) {
                val z = db.zepaDao().getById(f.zepaId)
                if (z != null) zepasFav.add(z)
            }
            val ccaas = db.ccaaDao().getAll()
            val allZ = mutableListOf<ZepaEntity>()
            for (c in ccaas) {
                allZ.addAll(db.zepaDao().getByCcaa(c.id))
            }
            withContext(Dispatchers.Main) {
                favorites = favs
                favoriteZepas = zepasFav
                allZepas = allZ
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ZEPAs Favoritas") },
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search bar with accent-insensitive filtering
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar ZEPA para añadir a favoritas...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (searchQuery.isNotBlank()) {
                    val normalizedQuery = searchQuery.normalizeAccents()
                    val searchResults = allZepas.filter {
                        it.nombre.normalizeAccents().contains(normalizedQuery, ignoreCase = true) ||
                        it.id_codigo.normalizeAccents().contains(normalizedQuery, ignoreCase = true)
                    }

                    Text("Resultados de búsqueda (${searchResults.size}):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    LazyColumn(
                        modifier = Modifier
                            .height(180.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(searchResults) { zepa ->
                            val isFav = favorites.any { it.zepaId == zepa.id_codigo }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${zepa.id_codigo} - ${zepa.nombre}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val userDb = UserDataDatabase.getDatabase(context)
                                            if (isFav) {
                                                userDb.favoriteDao().removeFavorite(zepa.id_codigo)
                                            } else {
                                                userDb.favoriteDao().addFavorite(FavoriteEntity(zepa.id_codigo))
                                            }
                                            loadData()
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if (isFav) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                            contentDescription = null,
                                            tint = if (isFav) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Text("Mis ZEPAs Favoritas (${favoriteZepas.size}):", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                if (favoriteZepas.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No tienes ZEPAs favoritas guardadas.\nUsa el buscador superior para añadir tus ZEPAs frecuentes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(favoriteZepas) { zepa ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onZepaClick(zepa.id_codigo) },
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${zepa.id_codigo} - ${zepa.nombre}",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    // Explicit delete favorite button with trash icon
                                    IconButton(onClick = {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val userDb = UserDataDatabase.getDatabase(context)
                                            userDb.favoriteDao().removeFavorite(zepa.id_codigo)
                                            loadData()
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Eliminar de favoritos",
                                            tint = MaterialTheme.colorScheme.error
                                        )
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
