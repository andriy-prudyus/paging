package com.example.paging.ui.items.list.viewModel

import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.example.paging.architecture.state.State
import com.example.paging.architecture.viewModel.AssistedSavedStateViewModelFactory
import com.example.paging.ui.items.list.dataSource.ItemListDataSourceFactory
import com.example.paging.ui.items.list.model.Item
import com.example.paging.ui.items.list.repository.ItemListRepository
import com.example.paging.utils.getPage
import com.example.paging.utils.pagedListConfig
import com.example.paging.utils.resetPagingState
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
        val pagedListConfig = pagedListConfig()
    }

    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<ItemListViewModel> {
        override fun create(state: SavedStateHandle): ItemListViewModel
    }

    @Suppress("UNCHECKED_CAST")
    fun items(): LiveData<State<PagedList<Item>>> {
        return (Transformations.map(createPagedList()) {
            State.Success(it)
        } as MutableLiveData<State<PagedList<Item>>>).apply {
            value = State.Loading()
        }
    }

    private fun createPagedList(): LiveData<PagedList<Item>> {
        val factory = ItemListDataSourceFactory(viewModelScope, repository) {
            state.getPage()
        }

        return LivePagedListBuilder(factory, pagedListConfig).build()
    }

    fun refresh(): LiveData<State<Any>> {
        state.resetPagingState()

        return MutableLiveData<State<Any>>().also { result ->
            result.value = State.Loading()

            (viewModelScope + CoroutineExceptionHandler { _, e ->
                Timber.e(e)
                result.value = State.Failure(e)
            }).launch {
                val data = repository.loadItems(1, pagedListConfig.initialLoadSizeHint)
                repository.refreshDataInDb(data)
                result.value = State.Success(Any())
            }
        }
    }
}
