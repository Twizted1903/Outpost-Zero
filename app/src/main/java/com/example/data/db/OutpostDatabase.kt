package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [OperatorEntity::class, BaseStashEntity::class, CollectedCdEntity::class],
    version = 9,
    exportSchema = false
)
abstract class OutpostDatabase : RoomDatabase() {
    abstract fun outpostDao(): OutpostDao

    companion object {
        @Volatile
        private var INSTANCE: OutpostDatabase? = null

        fun getInstance(context: Context): OutpostDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    OutpostDatabase::class.java,
                    "outpost_zero.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
