package com.example.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.database.entity.count.CountDao
import com.example.database.entity.count.DbCount
import com.example.database.entity.item.DbItem
import com.example.database.entity.item.ItemDao
import com.example.database.typeConverter.count.CountTypeConverter

@Database(
    entities = [
        DbItem::class,
        DbCount::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(CountTypeConverter::class)
abstract class Database : RoomDatabase() {

    abstract fun itemDao(): ItemDao
    abstract fun countDao(): CountDao
}