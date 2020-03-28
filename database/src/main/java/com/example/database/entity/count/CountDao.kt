package com.example.database.entity.count

import androidx.room.Dao
import androidx.room.Query
import com.example.database.BaseDao
import com.example.database.Database
import com.example.database.typeConverter.count.CountType

@Dao
abstract class CountDao(db: Database) : BaseDao<DbCount>(db) {

    @Query("SELECT count FROM DbCount WHERE type = :type")
    abstract suspend fun select(type: CountType): Int?

    @Query("DELETE FROM DbCount WHERE type = :type")
    abstract suspend fun delete(type: CountType): Int
}