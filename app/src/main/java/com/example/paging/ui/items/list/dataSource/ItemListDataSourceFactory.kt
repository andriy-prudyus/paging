package com.example.paging.ui.items.list.dataSource

import androidx.paging.DataSource
import com.example.paging.ui.items.list.model.Item
import com.example.paging.ui.items.list.repository.ItemListRepository
import kotlinx.coroutines.CoroutineScope

class ItemListDataSourceFactory(
    private val coroutineScope: CoroutineScope,
    private val repository: ItemListRepository,
    private val initialPageProducer: () -> Int
) : DataSource.Factory<Int, Item>() {

    override fun create(): DataSource<Int, Item> {
        return ItemListDataSource(initialPageProducer.invoke(), coroutineScope, repository)
    }
}