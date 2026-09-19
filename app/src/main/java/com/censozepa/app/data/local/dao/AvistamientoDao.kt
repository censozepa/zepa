package com.censozepa.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.censozepa.app.data.local.entity.AvistamientoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AvistamientoDao {
    @Query("SELECT * FROM avistamiento WHERE id_sesion = :idSesion ORDER BY hora ASC")
    fun getBySesion(idSesion: Int): Flow<List<AvistamientoEntity>>

    @Insert
    suspend fun insert(avistamiento: AvistamientoEntity): Long

    @Delete
    suspend fun delete(avistamiento: AvistamientoEntity)
}
