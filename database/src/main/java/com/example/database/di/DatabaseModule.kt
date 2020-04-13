package com.example.database.di

import android.content.Context
import androidx.room.Room
import com.example.database.Database
import com.example.database.entity.count.CountDao
import com.example.database.entity.item.ItemDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DatabaseModule {

    private const val DB_NAME = "database"

    @Singleton
    @Provides
    fun provideDatabase(context: Context): Database {
        return Room.databaseBuilder(context, Database::class.java, DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideItemDao(db: Database): ItemDao = db.itemDao()

    @Singleton
    @Provides
    fun provideCountDao(db: Database): CountDao = db.countDao()
}