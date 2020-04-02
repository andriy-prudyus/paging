package com.example.paging.ui.items.list.viewModel

import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.example.paging.architecture.state.State
import com.example.paging.architecture.viewModel.AssistedSavedStateViewModelFactory
import com.example.paging.ui.items.list.dataSource.ItemListDataSourceFactory
import com.example.paging.ui.items.list.model.Item
import com.example.paging.ui.items.list.repository.ItemListRepository
import com.example.paging.utils.pagedListConfig
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber

class ItemListViewModel @AssistedInject constructor(
    @Assisted val state: SavedStateHandle,
    private val repository: ItemListRepository
) : ViewModel() {

    companion object {
        const val PAGE = "page"
        const val ITEM_POSITION = "item_position"
        const val ITEM_TOP_OFFSET = "item_top_offset"

        val pagedListConfig = pagedListConfig()
    }

    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<ItemListViewModel> {
        override fun create(state: SavedStateHandle): ItemListViewModel
    }

    val itemPosition: Int
        get() = state.get<Int>(ITEM_POSITION) ?: 0

    val itemTopOffset: Int
        get() = state.get<Int>(ITEM_TOP_OFFSET) ?: 0

    @Suppress("UNCHECKED_CAST")
    fun items(): LiveData<State<PagedList<Item>>> {
        return (Transformations.map(createPagedList()) {
            State.Success(it)
        } as MutableLiveData<State<PagedList<Item>>>).apply {
            value = State.Loading()
        }
    }

    private fun createPagedList(): LiveData<PagedList<Item>> {
        val dataSourceFactory = ItemListDataSourceFactory(
            (state.get<Int>(PAGE) ?: 1).also { Timber.e("createPagedList = $it") },
            viewModelScope,
            repository
        )
        return LivePagedListBuilder(dataSourceFactory, pagedListConfig).build()
    }

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
}
