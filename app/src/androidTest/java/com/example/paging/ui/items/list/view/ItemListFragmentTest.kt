package com.example.paging.ui.items.list.view

import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.paging.R
import com.example.paging.architecture.exception.AppException
import com.example.paging.architecture.exception.AppException.Code.INCORRECT_INITIAL_PAGE_INDEX
import com.example.paging.architecture.exception.AppException.Code.UNKNOWN_EXPECTED_ITEMS_COUNT
import com.example.paging.architecture.state.PagingState
import com.example.paging.architecture.state.State
import com.example.paging.architecture.viewModel.InjectingSavedStateViewModelFactory
import com.example.paging.ui.items.list.dataSource.ItemListDataSource
import com.example.paging.ui.items.list.dataSource.ItemListDataSourceFactory
import com.example.paging.ui.items.list.model.Item
import com.example.paging.ui.items.list.repository.ItemListRepository
import com.example.paging.ui.items.list.viewModel.ItemListViewModel
import com.example.paging.utils.*
import com.example.paging.utils.SwipeRefreshLayoutMatchers.isRefreshing
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.GlobalScope
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ItemListFragmentTest {

    companion object {
        private const val PAGE_SIZE = 10
        private const val INITIAL_LOAD_SIZE_HINT = PAGE_SIZE * 2
        private const val ITEMS_COUNT = 32
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @MockK
    private lateinit var mockViewModel: ItemListViewModel

    @MockK
    private lateinit var mockInjectingViewModelFactory: InjectingSavedStateViewModelFactory

    @MockK
    private lateinit var mockSavedStateHandle: SavedStateHandle

    @MockK
    private lateinit var mockDataSourceFactory: ItemListDataSourceFactory

    @MockK
    private lateinit var mockRepository: ItemListRepository

    @MockK
    private lateinit var mockLoadInitialParams: PageKeyedDataSource.LoadInitialParams<Int>

    @MockK
    private lateinit var mockLoadInitialCallback: PageKeyedDataSource.LoadInitialCallback<Int, Item>

    private lateinit var fragment: ItemListFragment

    private val fragmentFactory = object : FragmentFactory() {
        override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
            return fragment
        }
    }

    private val itemAssertion = object : RecyclerViewInteraction.ItemViewAssertion<Item> {
        override fun check(item: Item, view: View, e: NoMatchingViewException?) {
            matches(
                allOf(
                    hasDescendant(allOf(withId(R.id.imageView), isCompletelyDisplayed())),
                    hasDescendant(
                        allOf(
                            withId(R.id.nameTextView),
                            withText(item.name),
                            isCompletelyDisplayed()
                        )
                    )
                )
            ).check(view, e)
        }
    }

    private val loadingAssertion = object : RecyclerViewInteraction.ItemViewAssertion<Any> {
        override fun check(item: Any, view: View, e: NoMatchingViewException?) {
            matches(hasDescendant(allOf(withId(R.id.progressBar), isCompletelyDisplayed())))
                .check(view, e)
        }
    }

    private val placeholderAssertion = object : RecyclerViewInteraction.ItemViewAssertion<Any> {
        override fun check(item: Any, view: View, e: NoMatchingViewException?) {
            matches(
                allOf(
                    hasDescendant(allOf(withId(R.id.imageView), isCompletelyDisplayed())),
                    hasDescendant(
                        allOf(withId(R.id.nameTextView), withText(""), isCompletelyDisplayed())
                    )
                )
            ).check(view, e)
        }
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        fragment = ItemListFragment(mockInjectingViewModelFactory)

        val savedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(fragment, null) {

                override fun <T : ViewModel?> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    @Suppress("UNCHECKED_CAST")
                    return mockViewModel as T
                }
            }

        every { mockInjectingViewModelFactory.create(fragment) } returns savedStateViewModelFactory
        every { mockViewModel.state } returns mockSavedStateHandle
    }

    @After
    fun afterTest() {
        unmockkAll()
    }

    @Test
    fun items_stateLoading() {
        every { mockViewModel.items() } returns MutableLiveData(State.Loading())

        launchFragmentInContainer<ItemListFragment>(null, R.style.AppTheme, fragmentFactory)

        checkToolbar()
        onView(withId(R.id.progressBar)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.recyclerView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.placeholder)).check(matches(not(isDisplayed())))
        onView(withId(R.id.swipeRefreshLayout)).check(matches(not(isRefreshing())))
    }

    @Test
    fun items_stateFailure() {
        every { mockViewModel.items() } returns MutableLiveData(
            State.Failure(
                AppException(
                    INCORRECT_INITIAL_PAGE_INDEX
                )
            )
        )

        launchFragmentInContainer<ItemListFragment>(null, R.style.AppTheme, fragmentFactory)

        checkToolbar()
        onView(withId(R.id.progressBar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.recyclerView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.placeholder)).check(matches(isCompletelyDisplayed()))
        onView(allOf(withId(R.id.textView), withParent(withId(R.id.placeholder))))
            .check(matches(withText(R.string.error_incorrect_initial_page_index)))
        onView(allOf(withId(R.id.imageView), withParent(withId(R.id.placeholder))))
            .check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.swipeRefreshLayout)).check(matches(not(isRefreshing())))
    }

    @Test
    fun items_stateSuccess() {
        val spyDataSource = spyk(ItemListDataSource(1, GlobalScope, mockRepository))
        every { spyDataSource.loadInitial(any(), any()) } returns Unit
        every { spyDataSource.loadInitial() } returns MutableLiveData()
        every { mockDataSourceFactory.create() } returns spyDataSource
        every { mockSavedStateHandle.getItemPositionRelative() } returns 0
        every { mockSavedStateHandle.getItemTopOffset() } returns 0
        every { mockViewModel.items() } returns MutableLiveData(State.Success(getPagedList(false)))

        launchFragmentInContainer<ItemListFragment>(null, R.style.AppTheme, fragmentFactory)

        checkToolbar()
        onView(withId(R.id.progressBar)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.recyclerView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.placeholder)).check(matches(not(isDisplayed())))
        onView(withId(R.id.swipeRefreshLayout)).check(matches(not(isRefreshing())))
    }

    @Test
    fun observeInitial_stateLoading() {
        val spyDataSource = spyk(ItemListDataSource(1, GlobalScope, mockRepository))
        every { spyDataSource.loadInitial() } returns MutableLiveData(PagingState.Initial.Loading())
        every { mockDataSourceFactory.create() } returns spyDataSource
        every { mockSavedStateHandle.getItemPositionRelative() } returns 0
        every { mockSavedStateHandle.getItemTopOffset() } returns 0
        every { mockViewModel.items() } returns MutableLiveData()

        launchFragmentInContainer<ItemListFragment>(null, R.style.AppTheme, fragmentFactory)

        checkToolbar()
        onView(withId(R.id.progressBar)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.recyclerView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.placeholder)).check(matches(not(isDisplayed())))
        onView(withId(R.id.swipeRefreshLayout)).check(matches(not(isRefreshing())))
    }

    @Test
    fun observeInitial_stateFailure() {
        val loadAfter = MutableLiveData<PagingState.Initial<Int, Item>>()
        val spyDataSource = spyk(ItemListDataSource(1, GlobalScope, mockRepository))
        every { spyDataSource.loadInitial(any(), any()) } returns Unit
        every { spyDataSource.loadInitial() } returns loadAfter
        every { mockDataSourceFactory.create() } returns spyDataSource
        every { mockSavedStateHandle.getItemPositionRelative() } returns 0
        every { mockSavedStateHandle.getItemTopOffset() } returns 0
        every { mockViewModel.items() } returns MutableLiveData(State.Success(getPagedList(false)))

        lateinit var fragment: Fragment
        launchFragmentInContainer<ItemListFragment>(null, R.style.AppTheme, fragmentFactory)
            .also { scenario ->
                scenario.onFragment { fragment = it }
            }

        fragment.activity?.runOnUiThread {
            loadAfter.value = PagingState.Initial.Failure(
                mockLoadInitialParams,
                mockLoadInitialCallback,
                AppException(INCORRECT_INITIAL_PAGE_INDEX)
            )
        }

        checkToolbar()
        onView(withId(R.id.progressBar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.recyclerView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.placeholder)).check(matches(isCompletelyDisplayed()))
        onView(allOf(withId(R.id.textView), withParent(withId(R.id.placeholder))))
            .check(matches(withText(R.string.error_incorrect_initial_page_index)))
        onView(allOf(withId(R.id.imageView), withParent(withId(R.id.placeholder))))
            .check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.swipeRefreshLayout)).check(matches(not(isRefreshing())))
    }

    @Test
    fun observeInitial_stateSuccess_noPlaceholders_noBeforeState_noAfterState() {
        val items = getItems(1) + getItems(2)
        val spyDataSource = spyk(ItemListDataSource(1, GlobalScope, mockRepository))

        val slotCallback = slot<PageKeyedDataSource.LoadInitialCallback<Int, Item>>()
        every { spyDataSource.loadInitial(any(), capture(slotCallback)) } answers {
            slotCallback.captured.onResult(items, 0, ITEMS_COUNT, null, null)
        }

        every { spyDataSource.loadInitial() } returns MutableLiveData(PagingState.Initial.Success())
        every { spyDataSource.loadAfter() } returns MutableLiveData()
        every { mockDataSourceFactory.create() } returns spyDataSource
        every { mockSavedStateHandle.getItemPositionRelative() } returns 0
        every { mockSavedStateHandle.getItemTopOffset() } returns 0
        every { mockViewModel.items() } returns MutableLiveData(State.Success(getPagedList(false)))

        launchFragmentInContainer<ItemListFragment>(null, R.style.AppTheme, fragmentFactory)

        checkToolbar()
        onView(withId(R.id.progressBar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.recyclerView)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.placeholder)).check(matches(not(isDisplayed())))
        onView(withId(R.id.swipeRefreshLayout)).check(matches(not(isRefreshing())))

        checkRecyclerViewItems(items, 0, itemAssertion)
    }

    @Test
    fun observeInitial_stateSuccess_noPlaceholders_noBeforeState_afterStateLoading() {
        val items = getItems(1) + getItems(2)
        val loadAfter = MutableLiveData<PagingState.After<Int, Item>>()
        val spyDataSource = spyk(ItemListDataSource(1, GlobalScope, mockRepository))
        every { spyDataSource.loadAfter(any(), any()) } returns Unit

        val slotLoadInitialCallback = slot<PageKeyedDataSource.LoadInitialCallback<Int, Item>>()
        every { spyDataSource.loadInitial(any(), capture(slotLoadInitialCallback)) } answers {
            slotLoadInitialCallback.captured.onResult(items, 0, ITEMS_COUNT, null, 3)
        }

        every { spyDataSource.loadInitial() } returns MutableLiveData(PagingState.Initial.Success())
        every { spyDataSource.loadAfter() } returns loadAfter
        every { mockDataSourceFactory.create() } returns spyDataSource
        every { mockSavedStateHandle.getItemPositionRelative() } returns 0
        every { mockSavedStateHandle.getItemTopOffset() } returns 0
        every { mockViewModel.items() } returns MutableLiveData(State.Success(getPagedList(false)))

        lateinit var fragment: Fragment
        launchFragmentInContainer<ItemListFragment>(null, R.style.AppTheme, fragmentFactory)
            .also { scenario ->
                scenario.onFragment { fragment = it }
            }

        onView(withId(R.id.recyclerView))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(items.size - 1))

        fragment.activity?.runOnUiThread {
            loadAfter.value = PagingState.After.Loading()
        }

        checkRecyclerViewItems(items, 0, itemAssertion)
        checkRecyclerViewItems(List(1) { Any() }, items.size, loadingAssertion)
    }

    @Test
    fun observeInitial_stateSuccess_noPlaceholders_noBeforeState_afterStateSuccess() {
        val loadInitialItems = getItems(1) + getItems(2)
        val loadAfterItems = getItems(3)
        val spyDataSource = spyk(ItemListDataSource(1, GlobalScope, mockRepository))

        val slotLoadInitialCallback = slot<PageKeyedDataSource.LoadInitialCallback<Int, Item>>()
        every { spyDataSource.loadInitial(any(), capture(slotLoadInitialCallback)) } answers {
            slotLoadInitialCallback.captured.onResult(loadInitialItems, 0, ITEMS_COUNT, null, 3)
        }

        val slotLoadAfterCallback = slot<PageKeyedDataSource.LoadCallback<Int, Item>>()
        every { spyDataSource.loadAfter(any(), capture(slotLoadAfterCallback)) } returns Unit

        every { spyDataSource.loadInitial() } returns MutableLiveData(PagingState.Initial.Success())
        every { spyDataSource.loadAfter() } returns MutableLiveData(PagingState.After.Success())
        every { mockDataSourceFactory.create() } returns spyDataSource
        every { mockSavedStateHandle.getItemPositionRelative() } returns 0
        every { mockSavedStateHandle.getItemTopOffset() } returns 0
        every { mockViewModel.items() } returns MutableLiveData(State.Success(getPagedList(false)))

        lateinit var fragment: Fragment
        launchFragmentInContainer<ItemListFragment>(null, R.style.AppTheme, fragmentFactory)
            .also { scenario ->
                scenario.onFragment { fragment = it }
            }

        onView(withId(R.id.recyclerView))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(loadInitialItems.size - 1))

        fragment.activity?.runOnUiThread {
            slotLoadAfterCallback.captured.onResult(loadAfterItems, null)
        }

        checkRecyclerViewItems(loadInitialItems + loadAfterItems, 0, itemAssertion)
    }

    @Test
    fun observeInitial_stateSuccess_noPlaceholders_noBeforeState_afterStateFailure() {
        val loadInitialItems = getItems(1) + getItems(2)
        val loadAfterItems = getItems(3)
        val loadAfter = MutableLiveData<PagingState.After<Int, Item>>()
        val spyDataSource = spyk(ItemListDataSource(1, GlobalScope, mockRepository))

        val slotLoadInitialCallback = slot<PageKeyedDataSource.LoadInitialCallback<Int, Item>>()
        every { spyDataSource.loadInitial(any(), capture(slotLoadInitialCallback)) } answers {
            slotLoadInitialCallback.captured.onResult(loadInitialItems, 0, ITEMS_COUNT, null, 3)
        }

        val slotLoadParams = mutableListOf<PageKeyedDataSource.LoadParams<Int>>()
        val slotLoadAfterCallbacks = mutableListOf<PageKeyedDataSource.LoadCallback<Int, Item>>()
        every {
            spyDataSource.loadAfter(
                capture(slotLoadParams),
                capture(slotLoadAfterCallbacks)
            )
        } returns Unit

        every { spyDataSource.loadInitial() } returns MutableLiveData(PagingState.Initial.Success())
        every { spyDataSource.loadAfter() } returns loadAfter
        every { mockDataSourceFactory.create() } returns spyDataSource
        every { mockSavedStateHandle.getItemPositionRelative() } returns 0
        every { mockSavedStateHandle.getItemTopOffset() } returns 0
        every { mockViewModel.items() } returns MutableLiveData(State.Success(getPagedList(false)))

        lateinit var fragment: Fragment
        launchFragmentInContainer<ItemListFragment>(null, R.style.AppTheme, fragmentFactory)
            .also { scenario ->
                scenario.onFragment { fragment = it }
            }

        onView(withId(R.id.recyclerView))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(loadInitialItems.size - 1))

        fragment.activity?.runOnUiThread {
            loadAfter.value = PagingState.After.Failure(
                slotLoadParams.first(),
                slotLoadAfterCallbacks.first(),
                AppException(INCORRECT_INITIAL_PAGE_INDEX)
            )
        }

        verify(exactly = 1) { spyDataSource.loadAfter(any(), any()) }

        checkRecyclerViewItems(loadInitialItems, 0, itemAssertion)

        checkRecyclerViewItems(
            List(1) { Any() },
            loadInitialItems.size,
            getFailureAssertion(R.string.error_incorrect_initial_page_index)
        )

        onView(withId(R.id.recyclerView))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    loadInitialItems.size,
                    click()
                )
            )

        verify(exactly = 2) { spyDataSource.loadAfter(any(), any()) }

        fragment.activity?.runOnUiThread {
            slotLoadAfterCallbacks.first().onResult(loadAfterItems, null)
            loadAfter.value = PagingState.After.Success()
        }

        checkRecyclerViewItems(loadInitialItems + loadAfterItems, 0, itemAssertion)
    }

    @Test
    fun observeInitial_stateSuccess_placeholders_noBeforeState_noAfterState() {
        val items = getItems(1) + getItems(2)
        val spyDataSource = spyk(ItemListDataSource(1, GlobalScope, mockRepository))

        val slotCallback = slot<PageKeyedDataSource.LoadInitialCallback<Int, Item>>()
        every { spyDataSource.loadInitial(any(), capture(slotCallback)) } answers {
            slotCallback.captured.onResult(items, 0, ITEMS_COUNT, null, null)
        }

        every { spyDataSource.loadInitial() } returns MutableLiveData(PagingState.Initial.Success())
        every { spyDataSource.loadAfter() } returns MutableLiveData()
        every { mockDataSourceFactory.create() } returns spyDataSource
        every { mockSavedStateHandle.getItemPositionAbsolute() } returns 0
        every { mockSavedStateHandle.getItemTopOffset() } returns 0
        every { mockViewModel.items() } returns MutableLiveData(State.Success(getPagedList(true)))

        launchFragmentInContainer<ItemListFragment>(null, R.style.AppTheme, fragmentFactory)

        checkToolbar()
        onView(withId(R.id.progressBar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.recyclerView)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.placeholder)).check(matches(not(isDisplayed())))
        onView(withId(R.id.swipeRefreshLayout)).check(matches(not(isRefreshing())))

        checkRecyclerViewItems(items, 0, itemAssertion)

        checkRecyclerViewItems(
            List(ITEMS_COUNT - items.size) { Any() },
            items.size,
            placeholderAssertion
        )
    }

    @Test
    fun observeInitial_stateSuccess_placeholders_noBeforeState_afterStateSuccess() {
        val loadAfter = MutableLiveData<PagingState.After<Int, Item>>()
        val loadInitialItems = getItems(1) + getItems(2)
        val loadAfterItems = getItems(3)
        val spyDataSource = spyk(ItemListDataSource(1, GlobalScope, mockRepository))

        val slotLoadInitialCallback = slot<PageKeyedDataSource.LoadInitialCallback<Int, Item>>()
        every { spyDataSource.loadInitial(any(), capture(slotLoadInitialCallback)) } answers {
            slotLoadInitialCallback.captured.onResult(loadInitialItems, 0, ITEMS_COUNT, null, 3)
        }

        val slotLoadAfterCallback = slot<PageKeyedDataSource.LoadCallback<Int, Item>>()
        every { spyDataSource.loadAfter(any(), capture(slotLoadAfterCallback)) } returns Unit

        every { spyDataSource.loadInitial() } returns MutableLiveData(PagingState.Initial.Success())
        every { spyDataSource.loadAfter() } returns loadAfter
        every { mockDataSourceFactory.create() } returns spyDataSource
        every { mockSavedStateHandle.getItemPositionAbsolute() } returns 0
        every { mockSavedStateHandle.getItemTopOffset() } returns 0
        every { mockViewModel.items() } returns MutableLiveData(State.Success(getPagedList(true)))

        lateinit var fragment: Fragment
        launchFragmentInContainer<ItemListFragment>(null, R.style.AppTheme, fragmentFactory)
            .also { scenario ->
                scenario.onFragment { fragment = it }
            }

        onView(withId(R.id.recyclerView))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(loadInitialItems.size - 1))

        fragment.activity?.runOnUiThread {
            slotLoadAfterCallback.captured.onResult(loadAfterItems, null)
            loadAfter.value = PagingState.After.Success()
        }

        val items = loadInitialItems + loadAfterItems
        checkRecyclerViewItems(items, 0, itemAssertion)
        checkRecyclerViewItems(
            List(ITEMS_COUNT - items.size) { Any() },
            items.size,
            placeholderAssertion
        )
    }

    @Test
    fun observeInitial_stateSuccess_placeholders_noBeforeState_afterStateFailure() {
        val loadAfter = MutableLiveData<PagingState.After<Int, Item>>()
        val items = getItems(1) + getItems(2)
        val spyDataSource = spyk(ItemListDataSource(1, GlobalScope, mockRepository))

        val slotLoadInitialCallback = slot<PageKeyedDataSource.LoadInitialCallback<Int, Item>>()
        every { spyDataSource.loadInitial(any(), capture(slotLoadInitialCallback)) } answers {
            slotLoadInitialCallback.captured.onResult(items, 0, ITEMS_COUNT, null, 3)
        }

        val slotAfterParams = slot<PageKeyedDataSource.LoadParams<Int>>()
        val slotAfterCallback = slot<PageKeyedDataSource.LoadCallback<Int, Item>>()
        every {
            spyDataSource.loadAfter(
                capture(slotAfterParams),
                capture(slotAfterCallback)
            )
        } returns Unit

        every { spyDataSource.loadInitial() } returns MutableLiveData(PagingState.Initial.Success())
        every { spyDataSource.loadAfter() } returns loadAfter
        every { mockDataSourceFactory.create() } returns spyDataSource
        every { mockSavedStateHandle.getItemPositionAbsolute() } returns 0
        every { mockSavedStateHandle.getItemTopOffset() } returns 0
        every { mockViewModel.items() } returns MutableLiveData(State.Success(getPagedList(true)))

        lateinit var fragment: Fragment
        launchFragmentInContainer<ItemListFragment>(null, R.style.AppTheme, fragmentFactory)
            .also { scenario ->
                scenario.onFragment { fragment = it }
            }

        onView(withId(R.id.recyclerView))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(items.size - 1))

        fragment.activity?.runOnUiThread {
            loadAfter.value = PagingState.After.Failure(
                slotAfterParams.captured,
                slotAfterCallback.captured,
                AppException(INCORRECT_INITIAL_PAGE_INDEX)
            )
        }

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.error_incorrect_initial_page_index)))

        checkRecyclerViewItems(items, 0, itemAssertion)
        checkRecyclerViewItems(
            List(ITEMS_COUNT - items.size) { Any() },
            items.size,
            placeholderAssertion
        )
    }

    @Test
    fun observeInitial_stateSuccess_noPlaceholders_beforeStateLoading_noAfterState() {
        val items = getItems(2) + getItems(3)
        val loadBefore = MutableLiveData<PagingState.Before<Int, Item>>()
        val spyDataSource = spyk(ItemListDataSource(2, GlobalScope, mockRepository))
        every { spyDataSource.loadBefore(any(), any()) } returns Unit

        val slotCallback = slot<PageKeyedDataSource.LoadInitialCallback<Int, Item>>()
        every { spyDataSource.loadInitial(any(), capture(slotCallback)) } answers {
            slotCallback.captured.onResult(items, PAGE_SIZE, ITEMS_COUNT, 1, null)
        }

        every { spyDataSource.loadInitial() } returns MutableLiveData(PagingState.Initial.Success())
        every { spyDataSource.loadBefore() } returns loadBefore
        every { mockDataSourceFactory.create() } returns spyDataSource
        every { mockSavedStateHandle.getItemPositionRelative() } returns 0
        every { mockSavedStateHandle.getItemTopOffset() } returns 0
        every { mockViewModel.items() } returns MutableLiveData(State.Success(getPagedList(false)))

        lateinit var fragment: Fragment
        launchFragmentInContainer<ItemListFragment>(null, R.style.AppTheme, fragmentFactory)
            .also { scenario ->
                scenario.onFragment { fragment = it }
            }

        fragment.activity?.runOnUiThread {
            loadBefore.value = PagingState.Before.Loading()
        }

        checkRecyclerViewItems(List(1) { Any() }, 0, loadingAssertion)
        checkRecyclerViewItems(items, 1, itemAssertion)
    }

    @Test
    fun observeInitial_stateSuccess_noPlaceholders_beforeStateSuccess_noAfterState() {
        val loadInitialItems = getItems(2) + getItems(3)
        val loadBeforeItems = getItems(1)
        val loadBefore = MutableLiveData<PagingState.Before<Int, Item>>()
        val spyDataSource = spyk(ItemListDataSource(2, GlobalScope, mockRepository))

        val slotLoadBeforeCallback = slot<PageKeyedDataSource.LoadCallback<Int, Item>>()
        every { spyDataSource.loadBefore(any(), capture(slotLoadBeforeCallback)) } returns Unit

        val slotLoadInitialCallback = slot<PageKeyedDataSource.LoadInitialCallback<Int, Item>>()
        every { spyDataSource.loadInitial(any(), capture(slotLoadInitialCallback)) } answers {
            slotLoadInitialCallback.captured.onResult(
                loadInitialItems,
                PAGE_SIZE,
                ITEMS_COUNT,
                1,
                null
            )
        }

        every { spyDataSource.loadInitial() } returns MutableLiveData(PagingState.Initial.Success())
        every { spyDataSource.loadBefore() } returns loadBefore
        every { mockDataSourceFactory.create() } returns spyDataSource
        every { mockSavedStateHandle.getItemPositionRelative() } returns 0
        every { mockSavedStateHandle.getItemTopOffset() } returns 0
        every { mockViewModel.items() } returns MutableLiveData(State.Success(getPagedList(false)))

        lateinit var fragment: Fragment
        launchFragmentInContainer<ItemListFragment>(null, R.style.AppTheme, fragmentFactory)
            .also { scenario ->
                scenario.onFragment { fragment = it }
            }

        fragment.activity?.runOnUiThread {
            slotLoadBeforeCallback.captured.onResult(loadBeforeItems, null)
        }

        checkRecyclerViewItems(loadBeforeItems + loadInitialItems, 0, itemAssertion)
    }

    @Test
    fun observeInitial_stateSuccess_noPlaceholders_beforeStateFailure_noAfterState() {
        val loadInitialItems = getItems(2) + getItems(3)
        val loadBeforeItems = getItems(1)
        val loadBefore = MutableLiveData<PagingState.Before<Int, Item>>()
        val spyDataSource = spyk(ItemListDataSource(2, GlobalScope, mockRepository))

        val slotLoadBeforeCallbacks = mutableListOf<PageKeyedDataSource.LoadCallback<Int, Item>>()
        val slotLoadParams = mutableListOf<PageKeyedDataSource.LoadParams<Int>>()
        every {
            spyDataSource.loadBefore(
                capture(slotLoadParams),
                capture(slotLoadBeforeCallbacks)
            )
        } returns Unit

        val slotLoadInitialCallback = slot<PageKeyedDataSource.LoadInitialCallback<Int, Item>>()
        every { spyDataSource.loadInitial(any(), capture(slotLoadInitialCallback)) } answers {
            slotLoadInitialCallback.captured.onResult(
                loadInitialItems,
                PAGE_SIZE,
                ITEMS_COUNT,
                1,
                null
            )
        }

        every { spyDataSource.loadInitial() } returns MutableLiveData(PagingState.Initial.Success())
        every { spyDataSource.loadBefore() } returns loadBefore
        every { mockDataSourceFactory.create() } returns spyDataSource
        every { mockSavedStateHandle.getItemPositionRelative() } returns 0
        every { mockSavedStateHandle.getItemTopOffset() } returns 0
        every { mockViewModel.items() } returns MutableLiveData(State.Success(getPagedList(false)))

        lateinit var fragment: Fragment
        launchFragmentInContainer<ItemListFragment>(null, R.style.AppTheme, fragmentFactory)
            .also { scenario ->
                scenario.onFragment { fragment = it }
            }

        fragment.activity?.runOnUiThread {
            loadBefore.value = PagingState.Before.Failure(
                slotLoadParams.first(),
                slotLoadBeforeCallbacks.first(),
                AppException(UNKNOWN_EXPECTED_ITEMS_COUNT)
            )
        }

        checkRecyclerViewItems(
            List(1) { Any() },
            0,
            getFailureAssertion(R.string.error_unknown_expected_items_count)
        )
        checkRecyclerViewItems(loadInitialItems, 1, itemAssertion)

        verify(exactly = 1) { spyDataSource.loadBefore(any(), any()) }

        onView(withId(R.id.recyclerView))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )

        verify(exactly = 2) { spyDataSource.loadBefore(any(), any()) }

        fragment.activity?.runOnUiThread {
            slotLoadBeforeCallbacks.first().onResult(loadBeforeItems, null)
            loadBefore.value = PagingState.Before.Success(slotLoadParams.first())
        }

        checkRecyclerViewItems(loadBeforeItems + loadInitialItems, 0, itemAssertion)
    }

    @Test
    fun observeInitial_stateSuccess_noPlaceholders_beforeStateLoading_afterStateLoading() {
        val items = getItems(2) + getItems(3)
        val loadBefore = MutableLiveData<PagingState.Before<Int, Item>>()
        val loadAfter = MutableLiveData<PagingState.After<Int, Item>>()
        val spyDataSource = spyk(ItemListDataSource(2, GlobalScope, mockRepository))
        every { spyDataSource.loadBefore(any(), any()) } returns Unit
        every { spyDataSource.loadAfter(any(), any()) } returns Unit

        val slotCallback = slot<PageKeyedDataSource.LoadInitialCallback<Int, Item>>()
        every { spyDataSource.loadInitial(any(), capture(slotCallback)) } answers {
            slotCallback.captured.onResult(items, PAGE_SIZE, ITEMS_COUNT, 1, 4)
        }

        every { spyDataSource.loadInitial() } returns MutableLiveData(PagingState.Initial.Success())
        every { spyDataSource.loadBefore() } returns loadBefore
        every { spyDataSource.loadAfter() } returns loadAfter
        every { mockDataSourceFactory.create() } returns spyDataSource
        every { mockSavedStateHandle.getItemPositionRelative() } returns 0
        every { mockSavedStateHandle.getItemTopOffset() } returns 0
        every { mockViewModel.items() } returns MutableLiveData(State.Success(getPagedList(false)))

        lateinit var fragment: Fragment
        launchFragmentInContainer<ItemListFragment>(null, R.style.AppTheme, fragmentFactory)
            .also { scenario ->
                scenario.onFragment { fragment = it }
            }

        fragment.activity?.runOnUiThread {
            loadBefore.value = PagingState.Before.Loading()
            loadAfter.value = PagingState.After.Loading()
        }

        checkRecyclerViewItems(List(1) { Any() }, 0, loadingAssertion)
        checkRecyclerViewItems(items, 1, itemAssertion)
        checkRecyclerViewItems(List(1) { Any() }, items.size + 1, loadingAssertion)
    }

    @Test
    fun observeInitial_stateSuccess_placeholders_beforeStateSuccess_noAfterState() {
        val loadBefore = MutableLiveData<PagingState.Before<Int, Item>>()
        val loadInitialItems = getItems(2) + getItems(3)
        val loadBeforeItems = getItems(1)
        val spyDataSource = spyk(ItemListDataSource(2, GlobalScope, mockRepository))

        val slotLoadInitialCallback = slot<PageKeyedDataSource.LoadInitialCallback<Int, Item>>()
        every { spyDataSource.loadInitial(any(), capture(slotLoadInitialCallback)) } answers {
            slotLoadInitialCallback.captured.onResult(
                loadInitialItems,
                PAGE_SIZE,
                ITEMS_COUNT,
                1,
                null
            )
        }

        val slotLoadBeforeParams = slot<PageKeyedDataSource.LoadParams<Int>>()
        val slotLoadBeforeCallback = slot<PageKeyedDataSource.LoadCallback<Int, Item>>()
        every {
            spyDataSource.loadBefore(
                capture(slotLoadBeforeParams),
                capture(slotLoadBeforeCallback)
            )
        } returns Unit

        every { spyDataSource.loadInitial() } returns MutableLiveData(PagingState.Initial.Success())
        every { spyDataSource.loadBefore() } returns loadBefore
        every { mockDataSourceFactory.create() } returns spyDataSource
        every { mockSavedStateHandle.getItemPositionAbsolute() } returns 0
        every { mockSavedStateHandle.getItemTopOffset() } returns 0
        every { mockViewModel.items() } returns MutableLiveData(State.Success(getPagedList(true)))

        lateinit var fragment: Fragment
        launchFragmentInContainer<ItemListFragment>(null, R.style.AppTheme, fragmentFactory)
            .also { scenario ->
                scenario.onFragment { fragment = it }
            }

        fragment.activity?.runOnUiThread {
            slotLoadBeforeCallback.captured.onResult(loadBeforeItems, null)
            loadBefore.value = PagingState.Before.Success(slotLoadBeforeParams.captured)
        }

        val items = loadBeforeItems + loadInitialItems
        checkRecyclerViewItems(items, 0, itemAssertion)
        checkRecyclerViewItems(
            List(ITEMS_COUNT - items.size) { Any() },
            items.size,
            placeholderAssertion
        )
    }

    @Test
    fun observeInitial_stateSuccess_placeholders_beforeStateFailure_noAfterState() {
        val loadBefore = MutableLiveData<PagingState.Before<Int, Item>>()
        val items = getItems(2) + getItems(3)
        val spyDataSource = spyk(ItemListDataSource(2, GlobalScope, mockRepository))

        val slotLoadInitialCallback = slot<PageKeyedDataSource.LoadInitialCallback<Int, Item>>()
        every { spyDataSource.loadInitial(any(), capture(slotLoadInitialCallback)) } answers {
            slotLoadInitialCallback.captured.onResult(items, PAGE_SIZE, ITEMS_COUNT, 1, null)
        }

        val slotBeforeParams = slot<PageKeyedDataSource.LoadParams<Int>>()
        val slotBeforeCallback = slot<PageKeyedDataSource.LoadCallback<Int, Item>>()
        every {
            spyDataSource.loadBefore(
                capture(slotBeforeParams),
                capture(slotBeforeCallback)
            )
        } returns Unit

        every { spyDataSource.loadInitial() } returns MutableLiveData(PagingState.Initial.Success())
        every { spyDataSource.loadBefore() } returns loadBefore
        every { mockDataSourceFactory.create() } returns spyDataSource
        every { mockSavedStateHandle.getItemPositionAbsolute() } returns 0
        every { mockSavedStateHandle.getItemTopOffset() } returns 0
        every { mockViewModel.items() } returns MutableLiveData(State.Success(getPagedList(true)))

        lateinit var fragment: Fragment
        launchFragmentInContainer<ItemListFragment>(null, R.style.AppTheme, fragmentFactory)
            .also { scenario ->
                scenario.onFragment { fragment = it }
            }

        onView(withId(R.id.recyclerView))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(items.size - 1))

        fragment.activity?.runOnUiThread {
            loadBefore.value = PagingState.Before.Failure(
                slotBeforeParams.captured,
                slotBeforeCallback.captured,
                AppException(INCORRECT_INITIAL_PAGE_INDEX)
            )
        }

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.error_incorrect_initial_page_index)))

        checkRecyclerViewItems(List(PAGE_SIZE) { Any() }, 0, placeholderAssertion)
        checkRecyclerViewItems(items, PAGE_SIZE, itemAssertion)
    }

    private fun <T> checkRecyclerViewItems(
        items: List<T>, startPosition: Int,
        itemAssertion: RecyclerViewInteraction.ItemViewAssertion<T>
    ) {
        RecyclerViewInteraction
            .onRecyclerView<T, RecyclerView.ViewHolder>(withId(R.id.recyclerView), startPosition)
            .withItems(items)
            .check(itemAssertion)
    }

    private fun checkToolbar() {
        onView(withId(R.id.toolbar)).check(matches(isCompletelyDisplayed()))
        onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.toolbar))))
            .check(matches(withText(R.string.items)))
    }

    private fun pagedListConfig(enablePlaceholders: Boolean): PagedList.Config {
        return PagedList.Config.Builder()
            .setEnablePlaceholders(enablePlaceholders)
            .setPageSize(PAGE_SIZE)
            .setInitialLoadSizeHint(INITIAL_LOAD_SIZE_HINT)
            .setPrefetchDistance(PAGE_SIZE)
            .build()
    }

    private fun getPagedList(enablePlaceholders: Boolean): PagedList<Item> {
        return LivePagedListBuilder(
            mockDataSourceFactory,
            pagedListConfig(enablePlaceholders)
        )
            .build()
            .getOrAwaitValue()
    }

    private fun getItems(page: Int): List<Item> {
        val size = if (page * PAGE_SIZE > ITEMS_COUNT) {
            ITEMS_COUNT % (page - 1) * PAGE_SIZE
        } else {
            PAGE_SIZE
        }

        return List(size) {
            val index = (page - 1) * PAGE_SIZE + 1 + it
            Item(index.toLong(), "Name $index", "https://test.com/$index.png")
        }
    }

    private fun getFailureAssertion(@StringRes error: Int): RecyclerViewInteraction.ItemViewAssertion<Any> {
        return object : RecyclerViewInteraction.ItemViewAssertion<Any> {
            override fun check(item: Any, view: View, e: NoMatchingViewException?) {
                matches(
                    allOf(
                        hasDescendant(allOf(withId(R.id.imageView), isCompletelyDisplayed())),
                        hasDescendant(
                            allOf(withId(R.id.textView), withText(error), isCompletelyDisplayed())
                        )
                    )
                )
                    .check(view, e)
            }
        }
    }
}