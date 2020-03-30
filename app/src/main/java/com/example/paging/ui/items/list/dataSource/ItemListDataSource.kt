package com.example.paging.ui.items.list.dataSource

import com.example.database.entity.item.DbItem
import com.example.paging.architecture.Optional
import com.example.paging.architecture.dataSource.BaseDataSource
import com.example.paging.ui.items.list.model.Item
import com.example.paging.ui.items.list.repository.ItemListRepository
import kotlinx.coroutines.CoroutineScope

class ItemListDataSource(
    initialPageIndex: Int,
    scope: CoroutineScope,
    private val repository: ItemListRepository
) : BaseDataSource<Item, Pair<List<DbItem>, Int>>(initialPageIndex, scope) {

    override suspend fun getExpectedItemsCount(): Optional<Int> = repository.getExpectedItemsCount()

    override suspend fun getCachedItemsCount(): Int = repository.getCachedItemsCount()

    override suspend fun loadItems(page: Int, pageSize: Int): Pair<List<DbItem>, Int> {
        return repository.loadItems(page, pageSize)
    }

    override suspend fun saveItems(data: Pair<List<DbItem>, Int>) {
        repository.saveData(data)
    }

    override suspend fun getCachedItems(page: Int, pageSize: Int): List<Item> {
        return repository.getCachedItems(page, pageSize)
    }
}
