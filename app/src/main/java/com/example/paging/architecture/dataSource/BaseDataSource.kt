package com.example.paging.architecture.dataSource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.example.paging.architecture.Optional
import com.example.paging.architecture.exception.AppException
import com.example.paging.architecture.exception.AppException.Code.*
import com.example.paging.architecture.state.PagingState.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.math.ceil

abstract class BaseDataSource<V, N>(
    private val initialPageIndex: Int,
    private val scope: CoroutineScope
) : PageKeyedDataSource<Int, V>() {

    private val loadInitial = MutableLiveData<Initial<Int, V>>()
    private val loadAfter = MutableLiveData<After<Int, V>>()
    private val loadBefore = MutableLiveData<Before<Int, V>>()

    protected abstract suspend fun getExpectedItemsCount(): Optional<Int>
    protected abstract suspend fun getCachedItemsCount(): Int
    protected abstract suspend fun loadItems(page: Int, pageSize: Int): N
    protected abstract suspend fun saveItems(data: N)
    protected abstract suspend fun getCachedItems(page: Int, pageSize: Int): List<V>

    fun loadInitial(): LiveData<Initial<Int, V>> = loadInitial

    fun loadAfter(): LiveData<After<Int, V>> = loadAfter

    fun loadBefore(): LiveData<Before<Int, V>> = loadBefore

    final override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, V>
    ) {
        val exceptionHandler = CoroutineExceptionHandler { _, e ->
            loadInitial.postValue(Initial.Failure(params, callback, e))
        }

        loadInitial.postValue(Initial.Loading())

        (scope + exceptionHandler).launch {
            var (expectedCount) = getExpectedItemsCount()
            if (expectedCount == null) {
                if (initialPageIndex > 1) throw AppException(INCORRECT_INITIAL_PAGE_INDEX)

                val data = loadItems(initialPageIndex, params.requestedLoadSize)
                saveItems(data)
                expectedCount = getExpectedItemsCount().value ?: 0
            }

            val cachedItemsCount = getCachedItemsCount()
            if (initialPageIndex > ceil((cachedItemsCount / params.requestedLoadSize).toDouble())) {
                throw AppException(INCORRECT_INITIAL_PAGE_INDEX)
            }

            val items = getCachedItems(initialPageIndex, params.requestedLoadSize)

            callback.onResult(
                items,
                if (initialPageIndex == 1) null else initialPageIndex - 1,
                if (expectedCount < (initialPageIndex - 1) * params.requestedLoadSize) {
                    null
                } else {
                    initialPageIndex + 1
                }
            )

            loadInitial.postValue(Initial.Success())
        }
    }

    final override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        val exceptionHandler = CoroutineExceptionHandler { _, e ->
            loadAfter.postValue(After.Failure(params, callback, e))
        }

        loadAfter.postValue(After.Loading())

        (scope + exceptionHandler).launch {
            val expectedItemsCount = getExpectedItemsCount().value
                ?: throw AppException(UNKNOWN_EXPECTED_ITEMS_COUNT)

            val cachedItemsCount = getCachedItemsCount()
            if (cachedItemsCount > expectedItemsCount) {
                throw AppException(CACHED_ITEMS_MORE_THAN_EXPECTED)
            }

            if (cachedItemsCount < params.key * params.requestedLoadSize) {
                val data = loadItems(params.key, params.requestedLoadSize)
                saveItems(data)
            }

            val items = getCachedItems(params.key, params.requestedLoadSize)

            callback.onResult(
                items,
                if (params.key < ceil(expectedItemsCount.toDouble() / params.requestedLoadSize.toDouble())) {
                    params.key + 1
                } else {
                    null
                }
            )

            loadAfter.postValue(After.Success())
        }
    }

    final override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        val exceptionHandler = CoroutineExceptionHandler { _, e ->
            loadBefore.postValue(Before.Failure(params, callback, e))
        }

        loadBefore.postValue(Before.Loading())

        (scope + exceptionHandler).launch {
            val items = getCachedItems(params.key, params.requestedLoadSize)
            callback.onResult(items, if (params.key == 1) null else params.key - 1)
            loadBefore.postValue(Before.Success())
        }
    }
}
