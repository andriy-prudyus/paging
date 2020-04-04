package com.example.paging.utils

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.paging.ITEM_POSITION
import com.example.paging.ITEM_TOP_OFFSET
import com.example.paging.PAGE
import com.example.paging.PAGE_SIZE
import com.example.paging.architecture.adapter.PagedRecyclerViewAdapter
import com.example.paging.architecture.dataSource.BasePageKeyedDataSource

fun pagedListConfig(): PagedList.Config {
    return PagedList.Config.Builder()
        .setEnablePlaceholders(false)
        .setPageSize(PAGE_SIZE)
        .setInitialLoadSizeHint(PAGE_SIZE * 2)
        .setPrefetchDistance(PAGE_SIZE)
        .build()
}

fun PagedRecyclerViewAdapter<*, *, *>.getState(): Bundle {
    val layoutManager = recyclerView!!.layoutManager as LinearLayoutManager
    val visibleItemPosition = layoutManager.findFirstVisibleItemPosition()
    val itemPosition = (visibleItemPosition % (currentList?.config?.pageSize ?: 0))
    val itemTopOffset = layoutManager.findViewByPosition(visibleItemPosition)?.top ?: 0

    val page = visibleItemPosition / (currentList?.config?.pageSize ?: 1) +
            if (
                (currentList?.dataSource as? BasePageKeyedDataSource<*, *>)?.initialPage ?: 0 > 1
                && loadedBeforePage != 1
            ) {
                loadedBeforePage
            } else {
                1
            }

    return bundleOf(
        PAGE to page,
        ITEM_POSITION to itemPosition,
        ITEM_TOP_OFFSET to itemTopOffset
    )
}
