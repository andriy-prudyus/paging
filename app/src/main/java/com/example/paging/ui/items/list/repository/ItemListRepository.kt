package com.example.paging.ui.items.list.repository

import com.example.database.DatabaseMediator
import com.example.database.entity.count.DbCount
import com.example.database.entity.item.DbItem
import com.example.database.typeConverter.count.CountType
import com.example.network.service.ItemService
import com.example.paging.architecture.Optional
import com.example.paging.converter.toDbEntities
import com.example.paging.converter.toItems
import com.example.paging.ui.items.list.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ItemListRepository @Inject constructor(
    private val itemService: ItemService,
    private val dbMediator: DatabaseMediator
) {

    suspend fun loadItems(pageIndex: Int, pageSize: Int): Pair<List<DbItem>, Int> {
        return withContext(Dispatchers.IO) {
            itemService.loadItems(pageIndex, pageSize).toDbEntities()
        }
    }

    suspend fun getCachedItems(pageIndex: Int, pageSize: Int): List<Item> {
        return withContext(Dispatchers.IO) {
            dbMediator.itemDao.select(pageSize, (pageIndex - 1) * pageSize).toItems()
        }
    }

    suspend fun getExpectedItemsCount(): Optional<Int> {
        return withContext(Dispatchers.IO) {
            Optional(dbMediator.countDao.select(CountType.ITEM))
        }
    }

    suspend fun getCachedItemsCount(): Int {
        return withContext(Dispatchers.IO) {
            dbMediator.itemDao.selectCount()
        }
    }

    suspend fun saveData(data: Pair<List<DbItem>, Int>) {
        withContext(Dispatchers.IO) {
            val (items, count) = data

            dbMediator.runInTransaction {
                it.itemDao.insert(items)
                it.countDao.insert(DbCount(CountType.ITEM, count))
            }
        }
    }

    suspend fun refreshDataInDb(data: Pair<List<DbItem>, Int>) {
        withContext(Dispatchers.IO) {
            dbMediator.runInTransaction {
                it.countDao.delete(CountType.ITEM)
                it.itemDao.delete()
                saveData(data)
            }
        }
    }
}