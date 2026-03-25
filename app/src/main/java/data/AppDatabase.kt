package com.savares.dailyinstallmentsaver.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.savares.dailyinstallmentsaver.model.InstallmentEntity

@Database(entities = [InstallmentEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun installmentDao(): InstallmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "installment_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
