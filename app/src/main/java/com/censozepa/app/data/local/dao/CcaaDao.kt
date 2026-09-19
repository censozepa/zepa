package com.censozepa.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.censozepa.app.data.local.entity.CcaaEntity

@Dao
interface CcaaDao {
    @Query("SELECT * FROM ccaa ORDER BY nombre ASC")
    suspend fun getAll(): List<CcaaEntity>
}
