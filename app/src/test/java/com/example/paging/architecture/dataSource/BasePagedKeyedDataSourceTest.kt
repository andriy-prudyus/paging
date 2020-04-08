package com.example.paging.architecture.dataSource

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PageKeyedDataSource
import com.example.paging.architecture.Optional
import com.example.paging.architecture.exception.AppException
import com.example.paging.architecture.exception.AppException.Code.*
import com.example.paging.architecture.state.PagingState.*
import com.github.testcoroutinesrule.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class BasePagedKeyedDataSourceTest {

    companion object {
        private const val PAGE_SIZE = 10
        private const val INITIAL_LOAD_SIZE_HINT = PAGE_SIZE * 2
        private const val ITEM_COUNT = 51
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private val scope = TestCoroutineScope()

    @Test
    fun `loadInitial, page 1, no expected count`() {
        var finished = false
        val currentPage = 1
        val params = PageKeyedDataSource.LoadInitialParams<Int>(INITIAL_LOAD_SIZE_HINT, false)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(currentPage, scope, PAGE_SIZE) {

            private var expectedCountNull = true

            override suspend fun getExpectedItemsCount(): Optional<Int> {
                return Optional(
                    if (expectedCountNull) {
                        expectedCountNull = false
                        null
                    } else {
                        ITEM_COUNT
                    }
                )
            }

            override suspend fun getCachedItemsCount(): Int {
                throw NotImplementedError()
            }

            override suspend fun loadItems(page: Int, pageSize: Int) = Any()

            override suspend fun saveItems(data: Any) {
                // do nothing
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                return List(PAGE_SIZE) { Any() }
            }
        }

        val callback = object : PageKeyedDataSource.LoadInitialCallback<Int, Any>() {

            lateinit var data: List<Any>
            var position = -1
            var totalCount = -1
            var previousPageKey: Int? = null
            var nextPageKey: Int? = null

            override fun onResult(
                data: MutableList<Any>,
                position: Int,
                totalCount: Int,
                previousPageKey: Int?,
                nextPageKey: Int?
            ) {
                this.data = data
                this.position = position
                this.totalCount = totalCount
                this.previousPageKey = previousPageKey
                this.nextPageKey = nextPageKey
                finished = true
            }

            override fun onResult(
                data: MutableList<Any>,
                previousPageKey: Int?,
                nextPageKey: Int?
            ) {
                throw NotImplementedError()
            }
        }

        runBlockingTest {
            dataSource.loadInitial(params, callback)
        }

        dataSource.loadInitial().observeForever {
            assert(it is Initial.Success)
        }

        while (true) {
            if (finished) {
                assert(
                    callback.data.size == getLoadInitialCount(currentPage)
                            && callback.position == 0
                            && callback.totalCount == ITEM_COUNT
                            && callback.previousPageKey == null
                            && callback.nextPageKey == getNextPage(currentPage)
                )
                break
            }
        }
    }

    @Test
    fun `loadInitial, page 1, cached expected count`() {
        var finished = false
        val currentPage = 1
        val params = PageKeyedDataSource.LoadInitialParams<Int>(INITIAL_LOAD_SIZE_HINT, false)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(currentPage, scope, PAGE_SIZE) {

            override suspend fun getExpectedItemsCount() = Optional(ITEM_COUNT)

            override suspend fun getCachedItemsCount(): Int {
                throw NotImplementedError()
            }

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw NotImplementedError()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                return List(PAGE_SIZE) { Any() }
            }
        }

        val callback = object : PageKeyedDataSource.LoadInitialCallback<Int, Any>() {

            lateinit var data: List<Any>
            var position = -1
            var totalCount = -1
            var previousPageKey: Int? = null
            var nextPageKey: Int? = null

            override fun onResult(
                data: MutableList<Any>,
                position: Int,
                totalCount: Int,
                previousPageKey: Int?,
                nextPageKey: Int?
            ) {
                this.data = data
                this.position = position
                this.totalCount = totalCount
                this.previousPageKey = previousPageKey
                this.nextPageKey = nextPageKey
                finished = true
            }

            override fun onResult(
                data: MutableList<Any>,
                previousPageKey: Int?,
                nextPageKey: Int?
            ) {
                throw NotImplementedError()
            }
        }

        runBlockingTest {
            dataSource.loadInitial(params, callback)
        }

        dataSource.loadInitial().observeForever {
            assert(it is Initial.Success)
        }

        while (true) {
            if (finished) {
                assert(
                    callback.data.size == INITIAL_LOAD_SIZE_HINT
                            && callback.position == 0
                            && callback.totalCount == ITEM_COUNT
                            && callback.previousPageKey == getPreviousPage(currentPage)
                            && callback.nextPageKey == getNextPage(currentPage)
                )
                break
            }
        }
    }

    @Test
    fun `loadInitial, page 2, no expected count`() {
        val currentPage = 2
        val params = PageKeyedDataSource.LoadInitialParams<Int>(INITIAL_LOAD_SIZE_HINT, false)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(currentPage, scope, PAGE_SIZE) {

            override suspend fun getExpectedItemsCount(): Optional<Int> = Optional(null)

            override suspend fun getCachedItemsCount(): Int {
                throw NotImplementedError()
            }

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw NotImplementedError()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                throw NotImplementedError()
            }
        }

        val callback = object : PageKeyedDataSource.LoadInitialCallback<Int, Any>() {

            override fun onResult(
                data: MutableList<Any>,
                position: Int,
                totalCount: Int,
                previousPageKey: Int?,
                nextPageKey: Int?
            ) {
                throw NotImplementedError()
            }

            override fun onResult(
                data: MutableList<Any>,
                previousPageKey: Int?,
                nextPageKey: Int?
            ) {
                throw NotImplementedError()
            }
        }

        runBlockingTest {
            dataSource.loadInitial(params, callback)
        }

        dataSource.loadInitial().observeForever {
            assert(
                it is Initial.Failure
                        && it.params == params
                        && it.callback == callback
                        && it.throwable is AppException
                        && (it.throwable as AppException).code == INCORRECT_INITIAL_PAGE_INDEX
            )
        }
    }

    @Test
    fun `loadInitial, page 2, cached expected count`() {
        var finished = false
        val currentPage = 2
        val params = PageKeyedDataSource.LoadInitialParams<Int>(INITIAL_LOAD_SIZE_HINT, false)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(currentPage, scope, PAGE_SIZE) {

            override suspend fun getExpectedItemsCount() = Optional(ITEM_COUNT)

            override suspend fun getCachedItemsCount(): Int {
                throw NotImplementedError()
            }

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw NotImplementedError()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                return List(getCount(page)) { Any() }
            }
        }

        val callback = object : PageKeyedDataSource.LoadInitialCallback<Int, Any>() {

            lateinit var data: List<Any>
            var position = -1
            var totalCount = -1
            var previousPageKey: Int? = null
            var nextPageKey: Int? = null

            override fun onResult(
                data: MutableList<Any>,
                position: Int,
                totalCount: Int,
                previousPageKey: Int?,
                nextPageKey: Int?
            ) {
                this.data = data
                this.position = position
                this.totalCount = totalCount
                this.previousPageKey = previousPageKey
                this.nextPageKey = nextPageKey
                finished = true
            }

            override fun onResult(
                data: MutableList<Any>,
                previousPageKey: Int?,
                nextPageKey: Int?
            ) {
                throw NotImplementedError()
            }
        }

        runBlockingTest {
            dataSource.loadInitial(params, callback)
        }

        dataSource.loadInitial().observeForever {
            assert(it is Initial.Success)
        }

        while (true) {
            if (finished) {
                assert(
                    callback.data.size == getLoadInitialCount(currentPage)
                            && callback.position == PAGE_SIZE
                            && callback.totalCount == ITEM_COUNT
                            && callback.previousPageKey == getPreviousPage(currentPage)
                            && callback.nextPageKey == getNextPage(currentPage)
                )
                break
            }
        }
    }

    @Test
    fun `loadInitial, final page`() {
        var finished = false
        val currentPage = ITEM_COUNT / PAGE_SIZE + 1
        val params = PageKeyedDataSource.LoadInitialParams<Int>(INITIAL_LOAD_SIZE_HINT, false)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(currentPage, scope, PAGE_SIZE) {

            override suspend fun getExpectedItemsCount() = Optional(ITEM_COUNT)

            override suspend fun getCachedItemsCount(): Int {
                throw NotImplementedError()
            }

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw NotImplementedError()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                return List(getCount(page)) { Any() }
            }
        }

        val callback = object : PageKeyedDataSource.LoadInitialCallback<Int, Any>() {

            lateinit var data: List<Any>
            var position = -1
            var totalCount = -1
            var previousPageKey: Int? = null
            var nextPageKey: Int? = null

            override fun onResult(
                data: MutableList<Any>,
                position: Int,
                totalCount: Int,
                previousPageKey: Int?,
                nextPageKey: Int?
            ) {
                this.data = data
                this.position = position
                this.totalCount = totalCount
                this.previousPageKey = previousPageKey
                this.nextPageKey = nextPageKey
                finished = true
            }

            override fun onResult(
                data: MutableList<Any>,
                previousPageKey: Int?,
                nextPageKey: Int?
            ) {
                throw NotImplementedError()
            }
        }

        runBlockingTest {
            dataSource.loadInitial(params, callback)
        }

        dataSource.loadInitial().observeForever {
            assert(it is Initial.Success)
        }

        while (true) {
            if (finished) {
                assert(
                    callback.data.size == getCount(currentPage)
                            && callback.position == (currentPage - 1) * PAGE_SIZE
                            && callback.totalCount == ITEM_COUNT
                            && callback.previousPageKey == getPreviousPage(currentPage)
                            && callback.nextPageKey == getNextPage(currentPage)
                )
                break
            }
        }
    }

    @Test
    fun `loadInitial, getExpectedItemsCount() throws exception`() {
        val currentPage = 1
        val params = PageKeyedDataSource.LoadInitialParams<Int>(INITIAL_LOAD_SIZE_HINT, false)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(currentPage, scope, PAGE_SIZE) {

            override suspend fun getExpectedItemsCount(): Optional<Int> {
                throw Exception()
            }

            override suspend fun getCachedItemsCount(): Int {
                throw NotImplementedError()
            }

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw NotImplementedError()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                throw NotImplementedError()
            }
        }

        val callback = object : PageKeyedDataSource.LoadInitialCallback<Int, Any>() {

            override fun onResult(
                data: MutableList<Any>,
                position: Int,
                totalCount: Int,
                previousPageKey: Int?,
                nextPageKey: Int?
            ) {
                throw NotImplementedError()
            }

            override fun onResult(
                data: MutableList<Any>,
                previousPageKey: Int?,
                nextPageKey: Int?
            ) {
                throw NotImplementedError()
            }
        }

        runBlockingTest {
            dataSource.loadInitial(params, callback)
        }

        dataSource.loadInitial().observeForever {
            assert(
                it is Initial.Failure
                        && it.params == params
                        && it.callback == callback
                        && it.throwable is Exception
            )
        }
    }

    @Test
    fun `loadInitial, loadItems() throws exception`() {
        val currentPage = 1
        val params = PageKeyedDataSource.LoadInitialParams<Int>(INITIAL_LOAD_SIZE_HINT, false)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(currentPage, scope, PAGE_SIZE) {

            override suspend fun getExpectedItemsCount(): Optional<Int> = Optional(null)

            override suspend fun getCachedItemsCount(): Int {
                throw NotImplementedError()
            }

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw Exception()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                throw NotImplementedError()
            }
        }

        val callback = object : PageKeyedDataSource.LoadInitialCallback<Int, Any>() {

            override fun onResult(
                data: MutableList<Any>,
                position: Int,
                totalCount: Int,
                previousPageKey: Int?,
                nextPageKey: Int?
            ) {
                throw NotImplementedError()
            }

            override fun onResult(
                data: MutableList<Any>,
                previousPageKey: Int?,
                nextPageKey: Int?
            ) {
                throw NotImplementedError()
            }
        }

        runBlockingTest {
            dataSource.loadInitial(params, callback)
        }

        dataSource.loadInitial().observeForever {
            assert(
                it is Initial.Failure
                        && it.params == params
                        && it.callback == callback
                        && it.throwable is Exception
            )
        }
    }

    @Test
    fun `loadInitial, saveItems() throws exception`() {
        val currentPage = 1
        val params = PageKeyedDataSource.LoadInitialParams<Int>(INITIAL_LOAD_SIZE_HINT, false)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(currentPage, scope, PAGE_SIZE) {

            override suspend fun getExpectedItemsCount(): Optional<Int> = Optional(null)

            override suspend fun getCachedItemsCount(): Int {
                throw NotImplementedError()
            }

            override suspend fun loadItems(page: Int, pageSize: Int): Any = Any()

            override suspend fun saveItems(data: Any) {
                throw Exception()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                throw NotImplementedError()
            }
        }

        val callback = object : PageKeyedDataSource.LoadInitialCallback<Int, Any>() {

            override fun onResult(
                data: MutableList<Any>,
                position: Int,
                totalCount: Int,
                previousPageKey: Int?,
                nextPageKey: Int?
            ) {
                throw NotImplementedError()
            }

            override fun onResult(
                data: MutableList<Any>,
                previousPageKey: Int?,
                nextPageKey: Int?
            ) {
                throw NotImplementedError()
            }
        }

        runBlockingTest {
            dataSource.loadInitial(params, callback)
        }

        dataSource.loadInitial().observeForever {
            assert(
                it is Initial.Failure
                        && it.params == params
                        && it.callback == callback
                        && it.throwable is Exception
            )
        }
    }

    @Test
    fun `loadInitial, getCachedItems() throws exception`() {
        val currentPage = 1
        val params = PageKeyedDataSource.LoadInitialParams<Int>(INITIAL_LOAD_SIZE_HINT, false)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(currentPage, scope, PAGE_SIZE) {

            override suspend fun getExpectedItemsCount(): Optional<Int> = Optional(
                ITEM_COUNT
            )

            override suspend fun getCachedItemsCount(): Int {
                throw NotImplementedError()
            }

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw NotImplementedError()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                throw Exception()
            }
        }

        val callback = object : PageKeyedDataSource.LoadInitialCallback<Int, Any>() {

            override fun onResult(
                data: MutableList<Any>,
                position: Int,
                totalCount: Int,
                previousPageKey: Int?,
                nextPageKey: Int?
            ) {
                throw NotImplementedError()
            }

            override fun onResult(
                data: MutableList<Any>,
                previousPageKey: Int?,
                nextPageKey: Int?
            ) {
                throw NotImplementedError()
            }
        }

        runBlockingTest {
            dataSource.loadInitial(params, callback)
        }

        dataSource.loadInitial().observeForever {
            assert(
                it is Initial.Failure
                        && it.params == params
                        && it.callback == callback
                        && it.throwable is Exception
            )
        }
    }

    @Test
    fun `loadAfter, cached items`() {
        var finished = false
        val currentPage = 1
        val params = PageKeyedDataSource.LoadParams<Int>(currentPage, PAGE_SIZE)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(1, scope) {

            override suspend fun getExpectedItemsCount(): Optional<Int> {
                return Optional(ITEM_COUNT)
            }

            override suspend fun getCachedItemsCount(): Int = ITEM_COUNT

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw NotImplementedError()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                return List(PAGE_SIZE) { Any() }
            }
        }

        val callback = object : PageKeyedDataSource.LoadCallback<Int, Any>() {

            lateinit var data: List<Any>
            var adjacentPageKey: Int? = null

            override fun onResult(data: MutableList<Any>, adjacentPageKey: Int?) {
                this.data = data
                this.adjacentPageKey = adjacentPageKey
                finished = true
            }
        }

        runBlockingTest {
            dataSource.loadAfter(params, callback)
        }

        dataSource.loadAfter().observeForever {
            assert(it is After.Success)
        }

        while (true) {
            if (finished) {
                assert(
                    callback.data.size == PAGE_SIZE
                            && callback.adjacentPageKey == getLoadAfterAdjacentPage(currentPage)
                )
                break
            }
        }
    }

    @Test
    fun `loadAfter, final page, cached items`() {
        var finished = false
        val currentPage = ITEM_COUNT / PAGE_SIZE + 1
        val params = PageKeyedDataSource.LoadParams<Int>(currentPage, PAGE_SIZE)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(1, scope) {

            override suspend fun getExpectedItemsCount(): Optional<Int> {
                return Optional(ITEM_COUNT)
            }

            override suspend fun getCachedItemsCount(): Int = ITEM_COUNT

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw NotImplementedError()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                return List(getCount(page)) { Any() }
            }
        }

        val callback = object : PageKeyedDataSource.LoadCallback<Int, Any>() {

            lateinit var data: List<Any>
            var adjacentPageKey: Int? = null

            override fun onResult(data: MutableList<Any>, adjacentPageKey: Int?) {
                this.data = data
                this.adjacentPageKey = adjacentPageKey
                finished = true
            }
        }

        runBlockingTest {
            dataSource.loadAfter(params, callback)
        }

        dataSource.loadAfter().observeForever {
            assert(it is After.Success)
        }

        while (true) {
            if (finished) {
                assert(
                    callback.data.size == PAGE_SIZE - (currentPage * PAGE_SIZE % ITEM_COUNT)
                            && callback.adjacentPageKey == getLoadAfterAdjacentPage(currentPage)
                )
                break
            }
        }
    }

    @Test
    fun `loadAfter, no cached items`() {
        var finished = false
        val currentPage = 1
        val params = PageKeyedDataSource.LoadParams<Int>(currentPage, PAGE_SIZE)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(1, scope) {

            override suspend fun getExpectedItemsCount(): Optional<Int> {
                return Optional(ITEM_COUNT)
            }

            override suspend fun getCachedItemsCount(): Int = 0

            override suspend fun loadItems(page: Int, pageSize: Int): Any = Any()

            override suspend fun saveItems(data: Any) {
                // do nothing
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                return List(getCount(page)) { Any() }
            }
        }

        val callback = object : PageKeyedDataSource.LoadCallback<Int, Any>() {

            lateinit var data: List<Any>
            var adjacentPageKey: Int? = null

            override fun onResult(data: MutableList<Any>, adjacentPageKey: Int?) {
                this.data = data
                this.adjacentPageKey = adjacentPageKey
                finished = true
            }
        }

        runBlockingTest {
            dataSource.loadAfter(params, callback)
        }

        dataSource.loadAfter().observeForever {
            assert(it is After.Success)
        }

        while (true) {
            if (finished) {
                assert(
                    callback.data.size == getCount(currentPage)
                            && callback.adjacentPageKey == getLoadAfterAdjacentPage(currentPage)
                )
                break
            }
        }
    }

    @Test
    fun `loadAfter, getExpectedItemsCount() returns null`() {
        val currentPage = 1
        val params = PageKeyedDataSource.LoadParams<Int>(currentPage, PAGE_SIZE)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(1, scope) {

            override suspend fun getExpectedItemsCount(): Optional<Int> {
                return Optional(null)
            }

            override suspend fun getCachedItemsCount(): Int {
                throw NotImplementedError()
            }

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw NotImplementedError()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                throw NotImplementedError()
            }
        }

        val callback = object : PageKeyedDataSource.LoadCallback<Int, Any>() {

            override fun onResult(data: MutableList<Any>, adjacentPageKey: Int?) {
                throw NotImplementedError()
            }
        }

        runBlockingTest {
            dataSource.loadAfter(params, callback)
        }

        dataSource.loadAfter().observeForever {
            assert(
                it is After.Failure
                        && it.params == params
                        && it.callback == callback
                        && it.throwable is AppException
                        && (it.throwable as AppException).code == UNKNOWN_EXPECTED_ITEMS_COUNT
            )
        }
    }

    @Test
    fun `loadAfter, cachedCount is more than expectedCount`() {
        val currentPage = 1
        val params = PageKeyedDataSource.LoadParams<Int>(currentPage, PAGE_SIZE)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(1, scope) {

            override suspend fun getExpectedItemsCount(): Optional<Int> {
                return Optional(ITEM_COUNT)
            }

            override suspend fun getCachedItemsCount(): Int = ITEM_COUNT + 1

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw NotImplementedError()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                throw NotImplementedError()
            }
        }

        val callback = object : PageKeyedDataSource.LoadCallback<Int, Any>() {

            override fun onResult(data: MutableList<Any>, adjacentPageKey: Int?) {
                throw NotImplementedError()
            }
        }

        runBlockingTest {
            dataSource.loadAfter(params, callback)
        }

        dataSource.loadAfter().observeForever {
            assert(
                it is After.Failure
                        && it.params == params
                        && it.callback == callback
                        && it.throwable is AppException
                        && (it.throwable as AppException).code == CACHED_ITEMS_MORE_THAN_EXPECTED
            )
        }
    }

    @Test
    fun `loadAfter, needs to load items from server`() {
        var finished = false
        val currentPage = 1
        val params = PageKeyedDataSource.LoadParams<Int>(currentPage, PAGE_SIZE)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(1, scope) {

            override suspend fun getExpectedItemsCount(): Optional<Int> {
                return Optional(ITEM_COUNT)
            }

            override suspend fun getCachedItemsCount(): Int = 0

            override suspend fun loadItems(page: Int, pageSize: Int): Any = Any()

            override suspend fun saveItems(data: Any) {
                // do nothing
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                return List(getCount(page)) { Any() }
            }
        }

        val callback = object : PageKeyedDataSource.LoadCallback<Int, Any>() {

            lateinit var data: List<Any>
            var adjacentPageKey: Int? = null

            override fun onResult(data: MutableList<Any>, adjacentPageKey: Int?) {
                this.data = data
                this.adjacentPageKey = adjacentPageKey
                finished = true
            }
        }

        runBlockingTest {
            dataSource.loadAfter(params, callback)
        }

        dataSource.loadAfter().observeForever {
            assert(it is After.Success)
        }

        while (true) {
            if (finished) {
                assert(
                    callback.data.size == getCount(currentPage)
                            && callback.adjacentPageKey == getLoadAfterAdjacentPage(currentPage)
                )
                break
            }
        }
    }

    @Test
    fun `loadAfter, cachedCount != expectedCount, no need to load items from server`() {
        var finished = false
        val currentPage = 1
        val params = PageKeyedDataSource.LoadParams<Int>(currentPage, PAGE_SIZE)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(1, scope) {

            override suspend fun getExpectedItemsCount(): Optional<Int> {
                return Optional(ITEM_COUNT)
            }

            override suspend fun getCachedItemsCount(): Int = ITEM_COUNT - 1

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw NotImplementedError()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                return List(getCount(page)) { Any() }
            }
        }

        val callback = object : PageKeyedDataSource.LoadCallback<Int, Any>() {

            lateinit var data: List<Any>
            var adjacentPageKey: Int? = null

            override fun onResult(data: MutableList<Any>, adjacentPageKey: Int?) {
                this.data = data
                this.adjacentPageKey = adjacentPageKey
                finished = true
            }
        }

        runBlockingTest {
            dataSource.loadAfter(params, callback)
        }

        dataSource.loadAfter().observeForever {
            assert(it is After.Success)
        }

        while (true) {
            if (finished) {
                assert(
                    callback.data.size == getCount(currentPage)
                            && callback.adjacentPageKey == getLoadAfterAdjacentPage(currentPage)
                )
                break
            }
        }
    }

    @Test
    fun `loadAfter, cachedCount != expectedCount, needs to load items from server`() {
        var finished = false
        val currentPage = 1
        val params = PageKeyedDataSource.LoadParams<Int>(currentPage, PAGE_SIZE)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(1, scope) {

            override suspend fun getExpectedItemsCount(): Optional<Int> {
                return Optional(ITEM_COUNT)
            }

            override suspend fun getCachedItemsCount(): Int = 1

            override suspend fun loadItems(page: Int, pageSize: Int): Any = Any()

            override suspend fun saveItems(data: Any) {
                // do nothing
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                return List(getCount(page)) { Any() }
            }
        }

        val callback = object : PageKeyedDataSource.LoadCallback<Int, Any>() {

            lateinit var data: List<Any>
            var adjacentPageKey: Int? = null

            override fun onResult(data: MutableList<Any>, adjacentPageKey: Int?) {
                this.data = data
                this.adjacentPageKey = adjacentPageKey
                finished = true
            }
        }

        runBlockingTest {
            dataSource.loadAfter(params, callback)
        }

        dataSource.loadAfter().observeForever {
            assert(it is After.Success)
        }

        while (true) {
            if (finished) {
                assert(
                    callback.data.size == getCount(currentPage)
                            && callback.adjacentPageKey == getLoadAfterAdjacentPage(currentPage)
                )
                break
            }
        }
    }

    @Test
    fun `loadAfter, getExpectedItemsCount() throws exception`() {
        val currentPage = 1
        val params = PageKeyedDataSource.LoadParams<Int>(currentPage, PAGE_SIZE)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(currentPage, scope, PAGE_SIZE) {

            override suspend fun getExpectedItemsCount(): Optional<Int> {
                throw Exception()
            }

            override suspend fun getCachedItemsCount(): Int {
                throw NotImplementedError()
            }

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw NotImplementedError()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                throw NotImplementedError()
            }
        }

        val callback = object : PageKeyedDataSource.LoadCallback<Int, Any>() {

            override fun onResult(data: MutableList<Any>, adjacentPageKey: Int?) {
                throw NotImplementedError()
            }
        }

        runBlockingTest {
            dataSource.loadAfter(params, callback)
        }

        dataSource.loadAfter().observeForever {
            assert(
                it is After.Failure
                        && it.params == params
                        && it.callback == callback
                        && it.throwable is Exception
            )
        }
    }

    @Test
    fun `loadAfter, getCachedItemsCount() throws exception`() {
        val currentPage = 1
        val params = PageKeyedDataSource.LoadParams<Int>(currentPage, PAGE_SIZE)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(currentPage, scope, PAGE_SIZE) {

            override suspend fun getExpectedItemsCount(): Optional<Int> {
                return Optional(ITEM_COUNT)
            }

            override suspend fun getCachedItemsCount(): Int {
                throw Exception()
            }

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw NotImplementedError()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                throw NotImplementedError()
            }
        }

        val callback = object : PageKeyedDataSource.LoadCallback<Int, Any>() {

            override fun onResult(data: MutableList<Any>, adjacentPageKey: Int?) {
                throw NotImplementedError()
            }
        }

        runBlockingTest {
            dataSource.loadAfter(params, callback)
        }

        dataSource.loadAfter().observeForever {
            assert(
                it is After.Failure
                        && it.params == params
                        && it.callback == callback
                        && it.throwable is Exception
            )
        }
    }

    @Test
    fun `loadAfter, loadItems() throws exception`() {
        val currentPage = 1
        val params = PageKeyedDataSource.LoadParams<Int>(currentPage, PAGE_SIZE)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(currentPage, scope, PAGE_SIZE) {

            override suspend fun getExpectedItemsCount(): Optional<Int> {
                return Optional(ITEM_COUNT)
            }

            override suspend fun getCachedItemsCount(): Int = 0

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw Exception()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                throw NotImplementedError()
            }
        }

        val callback = object : PageKeyedDataSource.LoadCallback<Int, Any>() {

            override fun onResult(data: MutableList<Any>, adjacentPageKey: Int?) {
                throw NotImplementedError()
            }
        }

        runBlockingTest {
            dataSource.loadAfter(params, callback)
        }

        dataSource.loadAfter().observeForever {
            assert(
                it is After.Failure
                        && it.params == params
                        && it.callback == callback
                        && it.throwable is Exception
            )
        }
    }

    @Test
    fun `loadAfter, saveItems() throws exception`() {
        val currentPage = 1
        val params = PageKeyedDataSource.LoadParams<Int>(currentPage, PAGE_SIZE)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(currentPage, scope, PAGE_SIZE) {

            override suspend fun getExpectedItemsCount(): Optional<Int> {
                return Optional(ITEM_COUNT)
            }

            override suspend fun getCachedItemsCount(): Int = 0

            override suspend fun loadItems(page: Int, pageSize: Int): Any = Any()

            override suspend fun saveItems(data: Any) {
                throw Exception()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                throw NotImplementedError()
            }
        }

        val callback = object : PageKeyedDataSource.LoadCallback<Int, Any>() {

            override fun onResult(data: MutableList<Any>, adjacentPageKey: Int?) {
                throw NotImplementedError()
            }
        }

        runBlockingTest {
            dataSource.loadAfter(params, callback)
        }

        dataSource.loadAfter().observeForever {
            assert(
                it is After.Failure
                        && it.params == params
                        && it.callback == callback
                        && it.throwable is Exception
            )
        }
    }

    @Test
    fun `loadAfter, getCachedItems() throws exception`() {
        val currentPage = 1
        val params = PageKeyedDataSource.LoadParams<Int>(currentPage, PAGE_SIZE)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(currentPage, scope, PAGE_SIZE) {

            override suspend fun getExpectedItemsCount(): Optional<Int> {
                return Optional(ITEM_COUNT)
            }

            override suspend fun getCachedItemsCount(): Int = ITEM_COUNT

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw NotImplementedError()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                throw Exception()
            }
        }

        val callback = object : PageKeyedDataSource.LoadCallback<Int, Any>() {

            override fun onResult(data: MutableList<Any>, adjacentPageKey: Int?) {
                throw NotImplementedError()
            }
        }

        runBlockingTest {
            dataSource.loadAfter(params, callback)
        }

        dataSource.loadAfter().observeForever {
            assert(
                it is After.Failure
                        && it.params == params
                        && it.callback == callback
                        && it.throwable is Exception
            )
        }
    }

    @Test
    fun `loadBefore, fist page, success`() {
        var finished = false
        val currentPage = 1
        val params = PageKeyedDataSource.LoadParams<Int>(currentPage, PAGE_SIZE)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(1, scope) {

            override suspend fun getExpectedItemsCount(): Optional<Int> {
                throw NotImplementedError()
            }

            override suspend fun getCachedItemsCount(): Int {
                throw NotImplementedError()
            }

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw NotImplementedError()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                return List(PAGE_SIZE) { Any() }
            }
        }

        val callback = object : PageKeyedDataSource.LoadCallback<Int, Any>() {

            lateinit var data: List<Any>
            var adjacentPageKey: Int? = null

            override fun onResult(data: MutableList<Any>, adjacentPageKey: Int?) {
                this.data = data
                this.adjacentPageKey = adjacentPageKey
                finished = true
            }
        }

        runBlockingTest {
            dataSource.loadBefore(params, callback)
        }

        dataSource.loadBefore().observeForever {
            assert(it is Before.Success)
        }

        while (true) {
            if (finished) {
                assert(
                    callback.data.size == PAGE_SIZE
                            && callback.adjacentPageKey == getLoadBeforeAdjacentPage(currentPage)
                )
                break
            }
        }
    }

    @Test
    fun `loadBefore, page before last, success`() {
        var finished = false
        val currentPage = ITEM_COUNT / PAGE_SIZE
        val params = PageKeyedDataSource.LoadParams<Int>(currentPage, PAGE_SIZE)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(1, scope) {

            override suspend fun getExpectedItemsCount(): Optional<Int> {
                throw NotImplementedError()
            }

            override suspend fun getCachedItemsCount(): Int {
                throw NotImplementedError()
            }

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw NotImplementedError()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                return List(PAGE_SIZE) { Any() }
            }
        }

        val callback = object : PageKeyedDataSource.LoadCallback<Int, Any>() {

            lateinit var data: List<Any>
            var adjacentPageKey: Int? = null

            override fun onResult(data: MutableList<Any>, adjacentPageKey: Int?) {
                this.data = data
                this.adjacentPageKey = adjacentPageKey
                finished = true
            }
        }

        runBlockingTest {
            dataSource.loadBefore(params, callback)
        }

        dataSource.loadBefore().observeForever {
            assert(it is Before.Success)
        }

        while (true) {
            if (finished) {
                assert(
                    callback.data.size == PAGE_SIZE
                            && callback.adjacentPageKey == getLoadBeforeAdjacentPage(currentPage)
                )
                break
            }
        }
    }

    @Test
    fun `loadBefore, getCachedItems() throws exception`() {
        val currentPage = 1
        val params = PageKeyedDataSource.LoadParams<Int>(currentPage, PAGE_SIZE)

        val dataSource = object : BasePageKeyedDataSource<Any, Any>(currentPage, scope, PAGE_SIZE) {

            override suspend fun getExpectedItemsCount(): Optional<Int> {
                throw NotImplementedError()
            }

            override suspend fun getCachedItemsCount(): Int {
                throw NotImplementedError()
            }

            override suspend fun loadItems(page: Int, pageSize: Int): Any {
                throw NotImplementedError()
            }

            override suspend fun saveItems(data: Any) {
                throw NotImplementedError()
            }

            override suspend fun getCachedItems(page: Int, pageSize: Int): List<Any> {
                throw Exception()
            }
        }

        val callback = object : PageKeyedDataSource.LoadCallback<Int, Any>() {

            override fun onResult(data: MutableList<Any>, adjacentPageKey: Int?) {
                throw NotImplementedError()
            }
        }

        runBlockingTest {
            dataSource.loadBefore(params, callback)
        }

        dataSource.loadBefore().observeForever {
            assert(
                it is Before.Failure
                        && it.params == params
                        && it.callback == callback
                        && it.throwable is Exception
            )
        }
    }

    private fun getPreviousPage(currentPage: Int): Int? {
        return if (currentPage > 1) currentPage - 1 else null
    }

    private fun getNextPage(currentPage: Int): Int? {
        return if (ITEM_COUNT < (currentPage - 1) * PAGE_SIZE + INITIAL_LOAD_SIZE_HINT) {
            null
        } else {
            currentPage + INITIAL_LOAD_SIZE_HINT / PAGE_SIZE
        }
    }

    private fun getLoadAfterAdjacentPage(currentPage: Int): Int? {
        return if (currentPage < ITEM_COUNT / PAGE_SIZE + 1) currentPage + 1 else null
    }

    private fun getLoadBeforeAdjacentPage(currentPage: Int): Int? {
        return if (currentPage == 1) null else currentPage - 1
    }

    private fun getCount(page: Int): Int {
        return if (page < ITEM_COUNT / PAGE_SIZE + 1) {
            PAGE_SIZE
        } else {
            ITEM_COUNT % PAGE_SIZE
        }
    }

    private fun getLoadInitialCount(currentPage: Int): Int {
        return if ((currentPage - 1) * PAGE_SIZE + INITIAL_LOAD_SIZE_HINT > ITEM_COUNT) {
            ITEM_COUNT - (currentPage - 1) * PAGE_SIZE
        } else {
            INITIAL_LOAD_SIZE_HINT
        }
    }
}