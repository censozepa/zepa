package com.censozepa.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.censozepa.app.data.local.dao.*
import com.censozepa.app.data.local.entity.*

@Database(
    entities = [
        CcaaEntity::class,
        ZepaEntity::class,
        EspecieEntity::class,
        FenologiaZepaEntity::class,
        SesionEntity::class,
        AvistamientoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ccaaDao(): CcaaDao
    abstract fun zepaDao(): ZepaDao
    abstract fun especieDao(): EspecieDao
    abstract fun fenologiaZepaDao(): FenologiaZepaDao
    abstract fun sesionDao(): SesionDao
    abstract fun avistamientoDao(): AvistamientoDao
}
