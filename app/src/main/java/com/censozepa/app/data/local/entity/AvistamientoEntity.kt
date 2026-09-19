package com.censozepa.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "avistamiento")
data class AvistamientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val id_sesion: Int,
    val id_especie: String,
    val hora: Long,
    val latitud: Double,
    val longitud: Double,
    val cantidad: Int,
    val alerta_fenologica: Boolean,
    val notas: String?
)
