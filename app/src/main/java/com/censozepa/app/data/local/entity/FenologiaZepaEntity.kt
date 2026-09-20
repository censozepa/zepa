package com.censozepa.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fenologia_zepa")
data class FenologiaZepaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val id_zepa: String,
    val id_especie: String,
    val estatus_ene: String?,
    val estatus_feb: String?,
    val estatus_mar: String?,
    val estatus_abr: String?,
    val estatus_may: String?,
    val estatus_jun: String?,
    val estatus_jul: String?,
    val estatus_ago: String?,
    val estatus_sep: String?,
    val estatus_oct: String?,
    val estatus_nov: String?,
    val estatus_dic: String?,
    val abundancia: String?,
    val categoria: String?
)
