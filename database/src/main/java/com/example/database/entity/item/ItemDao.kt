package com.example.database.entity.item

import androidx.room.Dao
import androidx.room.Query
import com.example.database.BaseDao
import com.example.database.Database

@Dao
abstract class ItemDao(db: Database) : BaseDao<DbItem>(db) {

    @Query("SELECT * FROM DbItem ORDER BY id ASC LIMIT :offset, :limit")
    abstract suspend fun select(limit: Int, offset: Int): List<DbItem>

    @Query("SELECT COUNT(id) FROM DbItem")
    abstract suspend fun selectCount(): Int

    @Query("SELeCT * FROM DbItem WHERE id = :itemId")
    abstract suspend fun selectItem(itemId: Long): DbItem?

    @Query("DELETE FROM DbItem")
    abstract suspend fun delete(): Int
}