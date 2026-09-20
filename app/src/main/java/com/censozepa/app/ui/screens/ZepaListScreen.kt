package com.censozepa.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.censozepa.app.data.local.DatabaseProvider
import com.censozepa.app.data.local.entity.ZepaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZepaListScreenContent(
    ccaaId: Int,
    ccaaName: String,
    onBack: () -> Unit,
    onZepaClick: (ZepaEntity) -> Unit
) {
    val context = LocalContext.current
    var zepaList by remember { mutableStateOf<List<ZepaEntity>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(ccaaId) {
        withContext(Dispatchers.IO) {
            val db = DatabaseProvider.getDatabase(context)
            zepaList = db.zepaDao().getByCcaa(ccaaId)
        }
    }

    val filteredZepas = zepaList.filter {
        it.nombre.contains(searchQuery, ignoreCase = true) ||
        it.id_codigo.contains(searchQuery, ignoreCase = true) ||
        (it.provincia?.contains(searchQuery, ignoreCase = true) == true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ZEPAs: $ccaaName") },
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
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar ZEPA por nombre o código...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredZepas) { zepa ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onZepaClick(zepa) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = "${zepa.id_codigo} - ${zepa.nombre}",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
