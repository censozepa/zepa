package com.censozepa.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.room.Room
import com.censozepa.app.data.local.AppDatabase
import com.censozepa.app.data.local.entity.CcaaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CcaaListScreenContent(onCcaaClick: (CcaaEntity) -> Unit) {
    val context = LocalContext.current
    var ccaaList by remember { mutableStateOf<List<CcaaEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "censozepa.db")
                .createFromAsset("database/censozepa.db")
                .build()
            ccaaList = db.ccaaDao().getAll()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Comunidades Autónomas") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(ccaaList) { ccaa ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onCcaaClick(ccaa) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = ccaa.nombre,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
