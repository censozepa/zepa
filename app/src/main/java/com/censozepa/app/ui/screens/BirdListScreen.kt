package com.censozepa.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.censozepa.app.data.local.DatabaseProvider
import com.censozepa.app.data.local.entity.EspecieEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirdListScreenContent(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var speciesList by remember { mutableStateOf<List<EspecieEntity>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    var expandedImageAsset by remember { mutableStateOf<String?>(null) }
    var expandedImageDesc by remember { mutableStateOf<String?>(null) }
    var expandedImageSciName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = DatabaseProvider.getDatabase(context)
            val list = db.especieDao().getAllList()
            withContext(Dispatchers.Main) {
                speciesList = list
                isLoading = false
            }
        }
    }

    val normalizedQuery = searchQuery.normalizeAccents()
    val filteredSpecies = speciesList.filter {
        (it.nombre_comun?.normalizeAccents()?.contains(normalizedQuery, ignoreCase = true) == true) ||
        (it.nombre_cientifico.normalizeAccents().contains(normalizedQuery, ignoreCase = true)) ||
        (it.codigo_n2000.normalizeAccents().contains(normalizedQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listado General de Aves (${speciesList.size})") },
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
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar ave por nombre común o científico...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Total aves encontradas: ${filteredSpecies.size}", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredSpecies) { especie ->
                        val common = especie.nombre_comun
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = common ?: especie.nombre_cientifico,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                },
                                supportingContent = {
                                    if (common != null) {
                                        Text(
                                            text = "${especie.nombre_cientifico} · Código: ${especie.codigo_n2000}",
                                            fontSize = 12.sp,
                                            fontStyle = FontStyle.Italic
                                        )
                                    } else {
                                        Text(
                                            text = "Código: ${especie.codigo_n2000}",
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                trailingContent = {
                                    val assetPath = especie.foto_asset
                                    if (assetPath != null) {
                                        AsyncImage(
                                            model = "file:///android_asset/$assetPath",
                                            contentDescription = common ?: especie.nombre_cientifico,
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    expandedImageAsset = assetPath
                                                    expandedImageDesc = common ?: especie.nombre_cientifico
                                                    expandedImageSciName = especie.nombre_cientifico
                                                },
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Surface(
                                            modifier = Modifier.size(52.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (expandedImageAsset != null) {
        Dialog(onDismissRequest = { expandedImageAsset = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = "file:///android_asset/$expandedImageAsset",
                        contentDescription = expandedImageDesc,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.FillWidth
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = expandedImageDesc ?: "",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (expandedImageSciName != null && expandedImageSciName != expandedImageDesc) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = expandedImageSciName ?: "",
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { expandedImageAsset = null }) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}
