package com.censozepa.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.censozepa.app.data.local.entity.SesionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SesionDao {
    @Query("SELECT * FROM sesion ORDER BY fecha_hora_inicio DESC")
    fun getAll(): Flow<List<SesionEntity>>

    @Insert
    suspend fun insert(sesion: SesionEntity): Long

    @Update
    suspend fun update(sesion: SesionEntity)

    @Delete
    suspend fun delete(sesion: SesionEntity)

    @Query("DELETE FROM sesion WHERE id = :sessionId")
    suspend fun deleteById(sessionId: Int)
}
