package com.censozepa.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ccaa")
data class CcaaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String
)
