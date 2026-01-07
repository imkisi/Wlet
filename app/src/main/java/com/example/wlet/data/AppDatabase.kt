package com.example.wlet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.wlet.data.local.dao.TransactionDao
import com.example.wlet.data.local.entities.Transaction
import com.example.wlet.data.local.entities.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Transaction::class, Category::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wlet_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val scope: CoroutineScope) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    val dao = database.transactionDao()
                    // Data awal
                    dao.insertCategory(Category(name = "Makanan & Minuman", type = "EXPENSE"))
                    dao.insertCategory(Category(name = "Belanja", type = "EXPENSE"))
                    dao.insertCategory(Category(name = "Hiburan", type = "EXPENSE"))
                    dao.insertCategory(Category(name = "Simpanan", type = "INCOME"))
                    // Tambahkan kategori lainnya...
                }
            }
        }
    }
}