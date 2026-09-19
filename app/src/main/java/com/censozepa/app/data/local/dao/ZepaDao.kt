package com.censozepa.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.censozepa.app.data.local.entity.ZepaEntity

@Dao
interface ZepaDao {
    @Query("SELECT * FROM zepa WHERE id_ccaa = :ccaaId ORDER BY nombre ASC")
    suspend fun getByCcaa(ccaaId: Int): List<ZepaEntity>

    @Query("SELECT * FROM zepa WHERE id_codigo = :zepaId LIMIT 1")
    suspend fun getById(zepaId: String): ZepaEntity?
}
