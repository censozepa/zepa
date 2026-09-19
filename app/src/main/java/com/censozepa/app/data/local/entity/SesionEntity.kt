package com.censozepa.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sesion")
data class SesionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val id_zepa: String,
    val fecha_hora_inicio: Long,
    val fecha_hora_fin: Long?,
    val distancia_recorrida: Double?,
    val track_gps_json: String?
)
