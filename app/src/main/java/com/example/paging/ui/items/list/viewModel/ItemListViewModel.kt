package com.example.paging.ui.items.list.viewModel

import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.example.paging.architecture.state.PagingState
import com.example.paging.architecture.state.State
import com.example.paging.architecture.viewModel.AssistedSavedStateViewModelFactory
import com.example.paging.ui.items.list.dataSource.ItemListDataSourceFactory
import com.example.paging.ui.items.list.model.Item
import com.example.paging.ui.items.list.repository.ItemListRepository
import com.example.paging.utils.calculateInitialPage
import com.example.paging.utils.pagedListConfig
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber

class ItemListViewModel @AssistedInject constructor(
    @Assisted private val state: SavedStateHandle,
    private val repository: ItemListRepository
) : ViewModel() {

    companion object {
        private const val ITEM_POSITION = "item_position"
        private val pagedListConfig = pagedListConfig()
    }

    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<ItemListViewModel> {
        override fun create(state: SavedStateHandle): ItemListViewModel
    }

    private val loadInitialState = MutableLiveData<PagingState.Initial<Int, Item>>()
    private val loadAfterState = MutableLiveData<PagingState.After<Int, Item>>()
    private val loadBeforeState = MutableLiveData<PagingState.Before<Int, Item>>()

    private fun onLoadInitialState(state: PagingState.Initial<Int, Item>) {
        loadInitialState.postValue(state)
    }

    private fun onLoadAfterState(state: PagingState.After<Int, Item>) {
        loadAfterState.postValue(state)
    }

    private fun onLoadBeforeState(state: PagingState.Before<Int, Item>) {
        loadBeforeState.postValue(state)
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        items.value = State.Failure(e)
    }

    private val pagedList: LiveData<PagedList<Item>> by lazy {
        val dataSourceFactory = ItemListDataSourceFactory(
            calculateInitialPage(state.get<Int>(ITEM_POSITION) ?: 0, pagedListConfig.pageSize),
            viewModelScope + exceptionHandler,
            repository,
            this::onLoadInitialState,
            this::onLoadAfterState,
            this::onLoadBeforeState
        )
        LivePagedListBuilder(dataSourceFactory, pagedListConfig).build()
    }

    private val items: MutableLiveData<State<PagedList<Item>>> = Transformations.map(pagedList) {
        State.Success(it)
    } as MutableLiveData<State<PagedList<Item>>>

    fun items(): LiveData<State<PagedList<Item>>> {
        return items.apply {
            value = State.Loading()
        }
    }

    fun loadInitial(): LiveData<PagingState.Initial<Int, Item>> = loadInitialState

    fun loadAfter(): LiveData<PagingState.After<Int, Item>> = loadAfterState

    fun loadBefore(): LiveData<PagingState.Before<Int, Item>> = loadBeforeState

    fun refresh(): LiveData<State<Any>> {
        return MutableLiveData<State<Any>>().also { result ->
            result.value = State.Loading()

            (viewModelScope + CoroutineExceptionHandler { _, e ->
                Timber.e(e)
                result.value = State.Failure(e)
            }).launch {
                val data = repository.loadItems(1, pagedListConfig.pageSize)
                repository.refreshDataInDb(data)
                result.value = State.Success(Any())
            }
        }
    }

    fun saveItemPosition(position: Int) {
        Timber.e("saveItemPosition = $position")
        state.set(ITEM_POSITION, position)
    }
}