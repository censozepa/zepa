package com.censozepa.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zepa")
data class ZepaEntity(
    @PrimaryKey val id_codigo: String,
    val id_ccaa: Int,
    val nombre: String,
    val provincia: String?,
    val superficie: Double?,
    val bounding_box: String?,
    @ColumnInfo(name = "path_mapa_offline") val pathMapaOffline: String?,
    val lat: Double?,
    val lon: Double?,
    val localidad: String?
)
