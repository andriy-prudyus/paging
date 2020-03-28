package com.example.paging.ui.items.list.dataSource

import androidx.paging.DataSource
import com.example.paging.architecture.state.PagingState
import com.example.paging.ui.items.list.model.Item
import com.example.paging.ui.items.list.repository.ItemListRepository
import kotlinx.coroutines.CoroutineScope

class ItemListDataSourceFactory(
    private val initialPageIndex: Int,
    private val coroutineScope: CoroutineScope,
    private val repository: ItemListRepository,
    private val loadInitialChange: (state: PagingState.Initial<Int, Item>) -> Unit,
    private val loadAfterChange: (state: PagingState.After<Int, Item>) -> Unit,
    private val loadBeforeChange: (state: PagingState.Before<Int, Item>) -> Unit
) : DataSource.Factory<Int, Item>() {

    override fun create(): DataSource<Int, Item> {
        return ItemListDataSource(
            initialPageIndex,
            coroutineScope,
            repository,
            loadInitialChange,
            loadAfterChange,
            loadBeforeChange
        )
    }
}