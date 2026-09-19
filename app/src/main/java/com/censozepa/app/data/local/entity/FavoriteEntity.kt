package com.censozepa.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_zepa")
data class FavoriteEntity(
    @PrimaryKey val zepaId: String
)
