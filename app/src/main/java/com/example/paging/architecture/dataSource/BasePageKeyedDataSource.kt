package com.example.paging.architecture.dataSource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.example.paging.PAGE_SIZE
import com.example.paging.architecture.Optional
import com.example.paging.architecture.exception.AppException
import com.example.paging.architecture.exception.AppException.Code.*
import com.example.paging.architecture.state.PagingState.*
import kotlinx.coroutines.*
import timber.log.Timber

abstract class BasePageKeyedDataSource<V, N>(
    var initialPage: Int,
    private val scope: CoroutineScope,
    private val pageSize: Int = PAGE_SIZE
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

        suspend fun prepareExpectedItemsCount(scope: CoroutineScope): Int {
            for (i in initialPage until initialPage + params.requestedLoadSize / pageSize) {
                if (!scope.isActive) {
                    break
                }

                val data = loadItems(i, pageSize)
                saveItems(data)
            }

            return getExpectedItemsCount().value ?: 0
        }

        suspend fun prepareCachedItems(scope: CoroutineScope, expectedCount: Int): List<V> {
            val items = mutableListOf<V>()

            val to = (params.requestedLoadSize / pageSize).let {
                val pageCount = expectedCount / pageSize + 1
                if (initialPage + it > pageCount) pageCount - initialPage else it - 1
            }

            for (i in 0..to) {
                if (!scope.isActive) {
                    break
                }
                items.addAll(getCachedItems(initialPage + i, pageSize))
            }

            return items
        }

        val exceptionHandler = CoroutineExceptionHandler { _, e ->
            Timber.e(e)
            loadInitial.postValue(Initial.Failure(params, callback, e))
        }

        loadInitial.postValue(Initial.Loading())

        (scope + exceptionHandler).launch {
            var (expectedCount) = getExpectedItemsCount()
            if (expectedCount == null) {
                if (initialPage > 1) throw AppException(INCORRECT_INITIAL_PAGE_INDEX)
                expectedCount = prepareExpectedItemsCount(this)
            }

            callback.onResult(
                prepareCachedItems(this, expectedCount),
                (initialPage - 1) * pageSize,
                expectedCount,
                if (initialPage == 1) null else initialPage - 1,
                if (expectedCount < (initialPage - 1) * pageSize + params.requestedLoadSize) {
                    null
                } else {
                    initialPage + params.requestedLoadSize / pageSize
                }
            )

            loadInitial.postValue(Initial.Success())
        }
    }

    final override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        val exceptionHandler = CoroutineExceptionHandler { _, e ->
            Timber.e(e)
            loadAfter.postValue(After.Failure(params, callback, e))
        }

        loadAfter.postValue(After.Loading())

        (scope + exceptionHandler).launch {
            val expectedCount = getExpectedItemsCount().value
                ?: throw AppException(UNKNOWN_EXPECTED_ITEMS_COUNT)

            val cachedCount = getCachedItemsCount()
            if (cachedCount > expectedCount) {
                throw AppException(CACHED_ITEMS_MORE_THAN_EXPECTED)
            }

            if (cachedCount < expectedCount && cachedCount < params.key * params.requestedLoadSize) {
                val data = loadItems(params.key, params.requestedLoadSize)
                saveItems(data)
            }

            callback.onResult(
                getCachedItems(params.key, params.requestedLoadSize),
                if (params.key < expectedCount / params.requestedLoadSize + 1) {
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
            Timber.e(e)
            loadBefore.postValue(Before.Failure(params, callback, e))
        }

        loadBefore.postValue(Before.Loading())

        (scope + exceptionHandler).launch {
            val items = getCachedItems(params.key, params.requestedLoadSize)
            callback.onResult(items, if (params.key == 1) null else params.key - 1)
            loadBefore.postValue(Before.Success(params))
        }
    }
}
