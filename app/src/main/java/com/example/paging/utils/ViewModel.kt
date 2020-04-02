package com.example.paging.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.paging.ui.items.list.viewModel.ItemListViewModel
import com.example.paging.ui.items.list.viewModel.ItemListViewModel.Companion.ITEM_POSITION
import com.example.paging.ui.items.list.viewModel.ItemListViewModel.Companion.ITEM_TOP_OFFSET
import com.example.paging.ui.items.list.viewModel.ItemListViewModel.Companion.PAGE
import timber.log.Timber
import kotlin.math.ceil

fun ItemListViewModel.saveRecyclerViewState(recyclerView: RecyclerView) {
    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
    val itemPosition = layoutManager.findFirstVisibleItemPosition()
    val itemTopOffset = layoutManager.findViewByPosition(itemPosition)?.top ?: 0
    val count = recyclerView.adapter?.itemCount ?: 0
    val savedPage = state.get<Int>(PAGE) ?: 1
    val page = ceil(itemPosition.toDouble() / ItemListViewModel.pagedListConfig.pageSize.toDouble()).toInt()

    val pos = itemPosition % ItemListViewModel.pagedListConfig.pageSize
    state.run {
        set(PAGE, page)
        set(ITEM_POSITION, pos)
        set(ITEM_TOP_OFFSET, itemTopOffset)
    }
    Timber.e("saveRecyclerViewState = $itemPosition, $pos, $itemTopOffset, $count, $page")
}