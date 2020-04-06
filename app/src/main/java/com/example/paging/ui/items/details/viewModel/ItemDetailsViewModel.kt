package com.example.paging.ui.items.details.viewModel

import androidx.lifecycle.*
import com.example.paging.architecture.Optional
import com.example.paging.architecture.exception.AppException
import com.example.paging.architecture.exception.AppException.Code.INCORRECT_ITEM_ID
import com.example.paging.architecture.state.State
import com.example.paging.architecture.viewModel.AssistedSavedStateViewModelFactory
import com.example.paging.ui.items.details.repository.ItemDetailsRepository
import com.example.paging.ui.items.list.model.Item
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber

class ItemDetailsViewModel @AssistedInject constructor(
    @Assisted private val state: SavedStateHandle,
    private val repository: ItemDetailsRepository
) : ViewModel() {

    companion object {
        private const val ITEM_ID = "itemId"
    }

    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<ItemDetailsViewModel> {
        override fun create(state: SavedStateHandle): ItemDetailsViewModel
    }

    private val item = MutableLiveData<State<Optional<Item>>>()

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        Timber.e(e)
        item.value = State.Failure(e)
    }

    fun item(): LiveData<State<Optional<Item>>> {
        item.value = State.Loading()

        (viewModelScope + exceptionHandler).launch {
            val itemId = state.get<Long>(ITEM_ID) ?: 0
            if (itemId < 1) {
                throw AppException(INCORRECT_ITEM_ID)
            }

            item.value = State.Success(repository.getItemDetails(itemId))
        }

        return item
    }
}
