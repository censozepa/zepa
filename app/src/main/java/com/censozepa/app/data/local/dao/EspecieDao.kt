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

    @Query("SELECT * FROM especie WHERE codigo_n2000 = :codigo")
    suspend fun getByCodigo(codigo: String): EspecieEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(especies: List<EspecieEntity>)
}
