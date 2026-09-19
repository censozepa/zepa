package com.censozepa.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "especie")
data class EspecieEntity(
    @PrimaryKey val codigo_n2000: String,
    val nombre_cientifico: String,
    val nombre_comun: String?,
    val categoria: String?
)
