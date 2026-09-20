package com.censozepa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreenContent(
    onSelectProvince: () -> Unit,
    onViewObservations: () -> Unit,
    onViewFavorites: () -> Unit,
    onViewBirdList: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Censo ZEPA - Menú Principal", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE8F5E9),
                            Color(0xFFC8E6C9)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuButton(
                    title = "Seleccionar Provincia / ZEPA",
                    subtitle = "Explorar CCAA, provincias y ZEPAs",
                    icon = Icons.Filled.Map,
                    onClick = onSelectProvince
                )

                MenuButton(
                    title = "Ver mis observaciones",
                    subtitle = "Historial de muestreos y censos",
                    icon = Icons.Filled.ListAlt,
                    onClick = onViewObservations
                )

                MenuButton(
                    title = "Zepas Favoritas",
                    subtitle = "Acceso rápido a ZEPAs guardadas",
                    icon = Icons.Filled.Bookmark,
                    onClick = onViewFavorites
                )

                MenuButton(
                    title = "Listado Aves",
                    subtitle = "Catálogo general de todas las aves",
                    icon = Icons.Filled.MenuBook,
                    onClick = onViewBirdList
                )

                MenuButton(
                    title = "Configuraciones",
                    subtitle = "Ajustes, datos y exportación",
                    icon = Icons.Filled.Settings,
                    onClick = onOpenSettings
                )
            }
        }
    }
}

@Composable
fun MenuButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1B4D3E),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1B4D3E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
