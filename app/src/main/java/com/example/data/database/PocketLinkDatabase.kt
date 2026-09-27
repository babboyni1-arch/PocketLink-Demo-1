package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.GameDao
import com.example.data.dao.GameplaySessionDao
import com.example.data.model.GameEntity
import com.example.data.model.GameplaySessionEntity

@Database(
    entities = [GameEntity::class, GameplaySessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PocketLinkDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun gameplaySessionDao(): GameplaySessionDao

    companion object {
        @Volatile
        private var INSTANCE: PocketLinkDatabase? = null

        fun getDatabase(context: Context): PocketLinkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PocketLinkDatabase::class.java,
                    "pocketlink_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
