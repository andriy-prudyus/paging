package com.example.paging.ui.items.details.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.example.paging.architecture.Optional
import com.example.paging.architecture.exception.AppException
import com.example.paging.architecture.state.State
import com.example.paging.ui.items.details.repository.ItemDetailsRepository
import com.example.paging.ui.items.list.model.Item
import com.github.testcoroutinesrule.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ItemDetailsViewModelTest {

    companion object {
        private const val ITEM_ID = "itemId"
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineRule = TestCoroutineRule()

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
    fun `item data correct`() {
        val itemId = 1L
        val item = Item(itemId, "Name", "https://test.com/1.png")
        `when`(mockState.get<Long>(ITEM_ID)).thenReturn(itemId)

        runBlocking {
            `when`(mockRepository.getItemDetails(itemId)).thenReturn(Optional(item))

            viewModel.item().observeForever {
                assert(it is State.Success && it.data.value == item)
            }
        }
    }

    @Test
    fun `itemId incorrect`() {
        `when`(mockState.get<Long>(ITEM_ID)).thenReturn(0)

        runBlocking {
            viewModel.item().observeForever {
                assert(
                    it is State.Failure
                            && it.throwable is AppException
                            && (it.throwable as AppException).code == AppException.Code.INCORRECT_ITEM_ID
                )
            }
        }
    }
}