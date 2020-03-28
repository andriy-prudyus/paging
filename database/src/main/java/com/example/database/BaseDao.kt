package com.example.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE

@Dao
abstract class BaseDao<T>(private val db: Database) {

    /**
     * @param item that has to be inserted
     * @return row id
     */
    @Insert(onConflict = REPLACE)
    abstract suspend fun insert(item: T): Long

    /**
     * @param items that have to be inserted
     * @return list of row ids
     */
    @Insert(onConflict = REPLACE)
    abstract suspend fun insert(items: List<T>): List<Long>
}