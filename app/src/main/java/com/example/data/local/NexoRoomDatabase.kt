package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*

@Database(
    entities = [
        BusinessEntity::class,
        BranchEntity::class,
        MemberEntity::class,
        CategoryEntity::class,
        MenuItemEntity::class,
        RestaurantTableEntity::class,
        CachedOrderEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class NexoRoomDatabase : RoomDatabase() {

    abstract fun businessDao(): BusinessDao
    abstract fun branchDao(): BranchDao
    abstract fun memberDao(): MemberDao
    abstract fun categoryDao(): CategoryDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun restaurantTableDao(): RestaurantTableDao
    abstract fun cachedOrderDao(): CachedOrderDao

    companion object {
        @Volatile
        private var INSTANCE: NexoRoomDatabase? = null

        fun getInstance(context: Context): NexoRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NexoRoomDatabase::class.java,
                    "nexo_business_os.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
