package com.censozepa.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.censozepa.app.data.local.entity.EspecieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EspecieDao {
    @Query("SELECT * FROM especie")
    fun getAll(): Flow<List<EspecieEntity>>

    @Query("SELECT * FROM especie ORDER BY nombre_comun ASC")
    suspend fun getAllList(): List<EspecieEntity>

    @Query("SELECT * FROM especie WHERE codigo_n2000 = :codigo")
    suspend fun getByCodigo(codigo: String): EspecieEntity?

    @Query("""
        SELECT e.codigo_n2000, e.nombre_cientifico, e.nombre_comun, f.categoria AS categoria, e.foto_asset AS foto_asset 
        FROM especie e
        JOIN fenologia_zepa f ON e.codigo_n2000 = f.id_especie
        WHERE f.id_zepa = :zepaId
    """)
    suspend fun getSpeciesForZepa(zepaId: String): List<EspecieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(especies: List<EspecieEntity>)
}
