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

    @Query("""
        SELECT z.* FROM zepa z
        JOIN fenologia_zepa f ON z.id_codigo = f.id_zepa
        WHERE f.id_especie = :especieId
        ORDER BY z.nombre ASC
    """)
    suspend fun getZepasForEspecie(especieId: String): List<ZepaEntity>
}
