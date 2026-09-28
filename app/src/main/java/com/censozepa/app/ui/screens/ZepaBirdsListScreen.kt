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
                                                val presCode = extractTipoPresencia(fen)
                                                val (bgColor, fgColor) = when (presCode) {
                                                    "p" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32) // Verde (Residente / Permanente)
                                                    "r" -> Color(0xFFFFF9C4) to Color(0xFFF57F17) // Amarillo (Reproductor / Estival)
                                                    "w" -> Color(0xFFE3F2FD) to Color(0xFF1565C0) // Azul (Invernante)
                                                    "c" -> Color(0xFFF3E5F5) to Color(0xFF6A1B9A) // Morado (Migratoria / Paso)
                                                    else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                                                }

                                        Surface(
                                            modifier = Modifier
                                                .padding(end = 12.dp)
                                                .clickable { showSdfDialogForEspecie = especie },
                                            shape = RoundedCornerShape(8.dp),
                                            color = bgColor,
                                            border = BorderStroke(1.dp, fgColor.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = "SDF",
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = fgColor
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
                        val presCode = extractTipoPresencia(fen)
                        val abCode = extractAbundancia(fen)
                        val uniCode = extractUnidades(fen)
                        val calCode = extractCalidadDatos(fen)
                        val consCode = extractEstadoConservacion(fen)

                        // 1. Tipo de Presencia
                        item {
                            Text("Tipo de Presencia", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            val text = when (presCode) {
                                "p" -> "p — Residente / Permanente (Resident)\nPoblación presente de forma estable y continuada durante todo el año."
                                "r" -> "r — Reproductor (Reproducing)\nPoblación presente durante la época reproductora (nidificación y cría)."
                                "w" -> "w — Invernante (Wintering)\nPoblación presente durante la temporada de invernada fuera de la época de cría."
                                "c" -> "c — Concentración / Paso (Concentrating)\nPoblación en paso migratorio, escala de descanso o concentraciones temporales."
                                else -> "No asignado"
                            }
                            Text("• $text", fontSize = 13.sp)
                        }

                        // 2. Abundancia Relativa
                        item {
                            Text("Abundancia Relativa", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            val text = when (abCode) {
                                "C" -> "C — Común (Common)\nEspecie habitual y frecuente en los hábitats propicios del espacio."
                                "R" -> "R — Rara (Rare)\nEspecie presente con baja densidad o en escaso número."
                                "V" -> "V — Muy rara (Very rare)\nEspecie de presencia excepcional, accidental o muy localizada."
                                "P" -> "P — Presente (Present)\nPresencia confirmada en el espacio pero con población no cuantificada."
                                else -> "No asignada"
                            }
                            Text("• $text", fontSize = 13.sp)
                        }

                        // 3. Unidades de Población
                        item {
                            Text("Unidades de Población", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            val text = when (uniCode) {
                                "p" -> "p — Parejas (Pairs)\nNúmero de parejas reproductoras censadas o estimadas en la ZEPA."
                                "i" -> "i — Individuos (Individuals)\nNúmero de ejemplares censados (habitual en invernada o aves no coloniales)."
                                "cmales" -> "cmales — Machos cantores (Calling males)\nMachos detectados en actividad territorial."
                                else -> "No asignada"
                            }
                            Text("• $text", fontSize = 13.sp)
                        }

                        // 4. Calidad de los Datos
                        item {
                            Text("Calidad de los Datos", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            val text = when (calCode) {
                                "G" -> "G — Buena (Good)\nBasada en censos exhaustivos, metodología contrastada y estudios recientes."
                                "M" -> "M — Moderada (Moderate)\nBasada en censos parciales, muestreos limitados o extrapolaciones fundadas."
                                "P" -> "P — Pobre (Poor)\nBasada en estimaciones cualitativas rudimentarias o conjeturas de expertos."
                                "DD" -> "DD — Datos Deficientes (Data Deficient)\nSin datos cuantitativos fiables ni información numérica disponible."
                                else -> "No evaluada"
                            }
                            Text("• $text", fontSize = 13.sp)
                        }

                        // 5. Estado de Conservación
                        item {
                            Text("Estado de Conservación", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            val text = when (consCode) {
                                "A" -> "A — Excelente (Excellent)\nConservación excelente de la población y los elementos clave de su hábitat."
                                "B" -> "B — Buena (Good)\nBuena conservación con perspectivas favorables y estructura adecuada."
                                "C" -> "C — Media / Significativa (Average)\nConservación media o reducida, pero con presencia significativa para la ZEPA."
                                "D" -> "D — No significativa (Non-significant)\nPresencia marginal no significativa para la valoración del espacio."
                                else -> if (!fen.categoria.isNullOrBlank()) "Directiva / Categoría legal: ${fen.categoria}" else "No asignado"
                            }
                            Text("• $text", fontSize = 13.sp)
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
