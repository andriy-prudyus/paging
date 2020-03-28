package com.example.paging.architecture.dataSource

import androidx.paging.PageKeyedDataSource
import com.example.paging.architecture.exception.AppException
import com.example.paging.architecture.exception.AppException.Code.*
import com.example.paging.architecture.state.PagingState
import com.example.paging.ui.items.list.model.Item
import com.example.paging.ui.items.list.repository.ItemListRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.ceil

abstract class BaseDataSource(
    private val initialPageIndex: Int,
    private val scope: CoroutineScope,
    private val repository: ItemListRepository,
    private val loadInitialChange: (state: PagingState.Initial<Int, Item>) -> Unit,
    private val loadAfterChange: (state: PagingState.After<Int, Item>) -> Unit,
    private val loadBeforeChange: (state: PagingState.Before<Int, Item>) -> Unit
) : PageKeyedDataSource<Int, Item>() {

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Item>
    ) {
        Timber.e("loadInitial")
        loadInitialChange(PagingState.Initial.Loading(params))

        scope.launch {
            var (expectedCount) = repository.getExpectedItemsCount()
            if (expectedCount == null) {
                if (initialPageIndex > 1) throw AppException(INCORRECT_INITIAL_PAGE_INDEX)

                val data = repository.loadItems(initialPageIndex, params.requestedLoadSize)
                repository.saveData(data)
                expectedCount = repository.getExpectedItemsCount().value ?: 0
            }

            val cachedItemsCount = repository.getCachedItemsCount()
            if (initialPageIndex > ceil((cachedItemsCount / params.requestedLoadSize).toDouble())) {
                throw AppException(INCORRECT_INITIAL_PAGE_INDEX)
            }

            val items = repository.loadCachedItems(initialPageIndex, params.requestedLoadSize)

            callback.onResult(
                items,
                if (initialPageIndex == 1) null else initialPageIndex - 1,
                if (expectedCount < (initialPageIndex - 1) * params.requestedLoadSize) {
                    null
                } else {
                    initialPageIndex + 1
                }
            )

            loadInitialChange(PagingState.Initial.Success(params))
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Item>) {
        Timber.e("loadAfter = ${params.key}")
        loadAfterChange(PagingState.After.Loading(params))

        scope.launch {
            val expectedItemsCount = repository.getExpectedItemsCount().value
                ?: throw AppException(UNKNOWN_EXPECTED_ITEMS_COUNT)

            val cachedItemsCount = repository.getCachedItemsCount()
            if (cachedItemsCount > expectedItemsCount) {
                throw AppException(CACHED_ITEMS_MORE_THAN_EXPECTED)
            }

            if (cachedItemsCount < params.key * params.requestedLoadSize) {
                val data = repository.loadItems(params.key, params.requestedLoadSize)
                repository.saveData(data)
            }

            val items = repository.loadCachedItems(params.key, params.requestedLoadSize)

            callback.onResult(
                items,
                if (params.key < ceil(expectedItemsCount.toDouble() / params.requestedLoadSize.toDouble())) {
                    params.key + 1
                } else {
                    null
                }
            )

            loadAfterChange(PagingState.After.Success(params))
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Item>) {
        Timber.e("loadBefore = ${params.key}")
        loadBeforeChange(PagingState.Before.Loading(params))

        scope.launch {
            val items = repository.loadCachedItems(params.key, params.requestedLoadSize)
            callback.onResult(items, if (params.key == 1) null else params.key - 1)
            loadBeforeChange(PagingState.Before.Success(params))
        }
    }
}