package com.example.paging.ui.items.details.viewModel

import androidx.lifecycle.SavedStateHandle
import com.example.paging.architecture.Optional
import com.example.paging.ui.items.details.repository.ItemDetailsRepository
import com.example.paging.ui.items.list.model.Item
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import timber.log.Timber

@RunWith(MockitoJUnitRunner::class)
class ItemDetailsViewModelTest {

    companion object {
        private const val ITEM_ID = "itemId"
    }

    @Mock
    private lateinit var mockState: SavedStateHandle

    @Mock
    private lateinit var mockRepository: ItemDetailsRepository

    private lateinit var viewModel: ItemDetailsViewModel

    @Before
    fun setup() {
        viewModel = ItemDetailsViewModel(mockState, mockRepository)
    }

    @Test
    fun item() {
        val itemId = 1L
        val item = Item(itemId, "Name", "https://test.com/1.png")
        `when`(mockState.get<Long>(ITEM_ID)).thenReturn(itemId)

        runBlocking {
            `when`(mockRepository.getItemDetails(itemId)).thenReturn(Optional(item))

            viewModel.item().observeForever {
                Timber.e("observeForever = $it")
            }

//            var loading = true
//            while (loading) {
//                val state =
//            }
        }
    }
}