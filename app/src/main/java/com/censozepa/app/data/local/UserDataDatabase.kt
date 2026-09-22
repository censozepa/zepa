package com.censozepa.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.censozepa.app.data.local.dao.AvistamientoDao
import com.censozepa.app.data.local.dao.FavoriteDao
import com.censozepa.app.data.local.dao.SesionDao
import com.censozepa.app.data.local.entity.AvistamientoEntity
import com.censozepa.app.data.local.entity.FavoriteEntity
import com.censozepa.app.data.local.entity.SesionEntity

@Database(
    entities = [
        SesionEntity::class,
        AvistamientoEntity::class,
        FavoriteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UserDataDatabase : RoomDatabase() {
    abstract fun sesionDao(): SesionDao
    abstract fun avistamientoDao(): AvistamientoDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: UserDataDatabase? = null

        fun getDatabase(context: Context): UserDataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDataDatabase::class.java,
                    "user_data.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
