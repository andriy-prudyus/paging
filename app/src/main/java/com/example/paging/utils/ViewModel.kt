package com.example.paging.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.paging.ui.items.list.viewModel.ItemListViewModel
import com.example.paging.ui.items.list.viewModel.ItemListViewModel.Companion.ITEM_POSITION
import com.example.paging.ui.items.list.viewModel.ItemListViewModel.Companion.ITEM_TOP_OFFSET

fun ItemListViewModel.saveRecyclerViewState(recyclerView: RecyclerView) {
    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
    val itemPosition = layoutManager.findFirstVisibleItemPosition()
    val itemTopOffset = layoutManager.findViewByPosition(itemPosition)?.top ?: 0

    state.run {
        set(ITEM_POSITION, itemPosition)
        set(ITEM_TOP_OFFSET, itemTopOffset)
    }
}