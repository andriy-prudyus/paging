package com.example.paging.ui.items.details.repository

import com.example.database.DatabaseMediator
import com.example.paging.architecture.Optional
import com.example.paging.converter.toItem
import com.example.paging.ui.items.list.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ItemDetailsRepository @Inject constructor(private val dbMediator: DatabaseMediator) {

    suspend fun getItemDetails(itemId: Long): Optional<Item> {
        return withContext(Dispatchers.IO) {
            Optional(dbMediator.itemDao.selectItem(itemId)?.toItem())
        }
    }
}