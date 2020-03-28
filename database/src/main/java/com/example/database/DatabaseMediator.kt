package com.example.database

import androidx.room.withTransaction
import com.example.database.entity.count.CountDao
import com.example.database.entity.item.ItemDao
import javax.inject.Inject

class DatabaseMediator @Inject constructor(
    private val db: Database,
    val itemDao: ItemDao,
    val countDao: CountDao
) {

    suspend fun runInTransaction(action: suspend (dbMediator: DatabaseMediator) -> Unit) {
        db.withTransaction {
            action.invoke(this)
        }
    }
}