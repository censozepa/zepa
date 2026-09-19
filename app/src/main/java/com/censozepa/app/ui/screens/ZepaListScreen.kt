package com.censozepa.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

    LaunchedEffect(ccaaId) {
        withContext(Dispatchers.IO) {
            val db = DatabaseProvider.getDatabase(context)
            zepaList = db.zepaDao().getByCcaa(ccaaId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ZEPAs: $ccaaName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(zepaList) { zepa ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
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
