package com.censozepa.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.censozepa.app.data.local.DatabaseProvider
import com.censozepa.app.data.local.entity.EspecieEntity
import com.censozepa.app.data.local.entity.FenologiaZepaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZepaBirdsListScreenContent(
    zepaId: String,
    zepaName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var speciesList by remember { mutableStateOf<List<EspecieEntity>>(emptyList()) }
    var fenologiaMap by remember { mutableStateOf<Map<String, FenologiaZepaEntity>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    var expandedImageAsset by remember { mutableStateOf<String?>(null) }
    var expandedImageDesc by remember { mutableStateOf<String?>(null) }
    var expandedImageSciName by remember { mutableStateOf<String?>(null) }
    var showSdfDialogForEspecie by remember { mutableStateOf<EspecieEntity?>(null) }

    LaunchedEffect(zepaId) {
        withContext(Dispatchers.IO) {
            val db = DatabaseProvider.getDatabase(context)
            val list = db.especieDao().getSpeciesForZepa(zepaId)
            val fenList = db.fenologiaZepaDao().getByZepa(zepaId)
            withContext(Dispatchers.Main) {
                speciesList = list
                fenologiaMap = fenList.associateBy { it.id_especie }
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aves en $zepaName (${speciesList.size})") },
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
        } else if (speciesList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay aves catalogadas para esta ZEPA.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(speciesList) { especie ->
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
                                        text = "${especie.nombre_cientifico} · ${especie.categoria ?: "N/D"}",
                                        fontSize = 12.sp,
                                        fontStyle = FontStyle.Italic
                                    )
                                } else {
                                    Text(
                                        text = especie.categoria ?: "N/D",
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val fen = fenologiaMap[especie.codigo_n2000]
                                    if (fen != null) {
                                        Surface(
                                            modifier = Modifier
                                                .padding(end = 12.dp)
                                                .clickable { showSdfDialogForEspecie = especie },
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.tertiaryContainer,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
                                        ) {
                                            Text(
                                                text = "SDF",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
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
                                                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            }
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

    // SDF Info Dialog
    if (showSdfDialogForEspecie != null) {
        val especie = showSdfDialogForEspecie!!
        val fen = fenologiaMap[especie.codigo_n2000]
        AlertDialog(
            onDismissRequest = { showSdfDialogForEspecie = null },
            title = {
                Text(
                    text = "Datos SDF: ${especie.nombre_comun ?: especie.nombre_cientifico}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                if (fen == null) {
                    Text("No hay datos SDF para esta especie en esta ZEPA.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Phenology (Estatus)
                        val statuses = listOfNotNull(
                            fen.estatus_ene, fen.estatus_feb, fen.estatus_mar, fen.estatus_abr,
                            fen.estatus_may, fen.estatus_jun, fen.estatus_jul, fen.estatus_ago,
                            fen.estatus_sep, fen.estatus_oct, fen.estatus_nov, fen.estatus_dic
                        ).filter { it.isNotBlank() && it != "-" }.map { it.lowercase() }.toSet()

                        if (statuses.isNotEmpty()) {
                            item {
                                Text("Fenología / Categoría Oficial", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                statuses.forEach { code ->
                                    val desc = when (code) {
                                        "p" -> "Residente (p) - Población presente de forma estable todo el año."
                                        "r" -> "Reproductor (r) - Población presente durante la época reproductora."
                                        "w" -> "Invernante (w) - Población presente durante la temporada de invernada."
                                        "c" -> "Concentración / Paso (c) - Población en paso migratorio o descanso."
                                        else -> "Código: $code"
                                    }
                                    Text("• $desc", fontSize = 14.sp)
                                }
                            }
                        }

                        // Abundancia
                        if (!fen.abundancia.isNullOrBlank() && fen.abundancia != "-") {
                            item {
                                Text("Abundancia Relativa", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                val desc = when (fen.abundancia.uppercase()) {
                                    "C" -> "Común (C) - Habitual y frecuente."
                                    "R" -> "Rara (R) - Baja densidad o escaso número."
                                    "V" -> "Muy rara (V) - Excepcional o muy localizada."
                                    "P" -> "Presente (P) - Presencia confirmada, población no cuantificada."
                                    else -> "Código: ${fen.abundancia}"
                                }
                                Text("• $desc", fontSize = 14.sp)
                            }
                        }

                        // Categoría
                        if (!fen.categoria.isNullOrBlank() && fen.categoria != "-") {
                            item {
                                Text("Otros Códigos (Categoría)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                val desc = when (fen.categoria.uppercase()) {
                                    "G" -> "Buena Calidad (G) - Basada en censos exhaustivos."
                                    "M" -> "Moderada (M) - Basada en censos parciales."
                                    "P" -> "Pobre (Calidad) o Parejas (Unidad) o Presente (Abundancia)"
                                    "DD" -> "Datos Deficientes (DD) - Sin datos cuantitativos."
                                    "A" -> "Excelente (A) - Conservación excelente."
                                    "B" -> "Buena (B) - Buena conservación."
                                    "C" -> "Media (C) - Conservación media o significativa."
                                    "D" -> "No significativa (D) - Presencia marginal."
                                    "I" -> "Individuos (i) - Número de ejemplares."
                                    "CMALES" -> "Machos cantores (cmales) - Machos detectados territoriales."
                                    else -> "Código: ${fen.categoria}"
                                }
                                Text("• $desc", fontSize = 14.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSdfDialogForEspecie = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
