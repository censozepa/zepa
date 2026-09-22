package com.censozepa.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.censozepa.app.data.local.entity.FenologiaZepaEntity

@Dao
interface FenologiaZepaDao {
    @Query("SELECT * FROM fenologia_zepa WHERE id_zepa = :zepaId")
    suspend fun getByZepa(zepaId: String): List<FenologiaZepaEntity>

    @Query("SELECT * FROM fenologia_zepa WHERE id_zepa = :zepaId AND id_especie = :especieId LIMIT 1")
    suspend fun getByZepaAndEspecie(zepaId: String, especieId: String): FenologiaZepaEntity?
}
