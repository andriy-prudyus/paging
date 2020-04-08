package com.example.paging.utils

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.paging.*
import com.example.paging.architecture.adapter.PagedRecyclerViewAdapter
import com.example.paging.architecture.dataSource.BasePageKeyedDataSource

fun pagedListConfig(
    enablePlaceholders: Boolean = false,
    pageSize: Int = PAGE_SIZE
): PagedList.Config {
    return PagedList.Config.Builder()
        .setEnablePlaceholders(enablePlaceholders)
        .setPageSize(pageSize)
        .setInitialLoadSizeHint(INITIAL_LOAD_SIZE_HINT)
        .setPrefetchDistance(pageSize)
        .build()
}

fun PagedRecyclerViewAdapter<*, *, *>.getState(): Bundle {
    val pageSize = currentList?.config?.pageSize ?: PAGE_SIZE
    val initialPage = (currentList?.dataSource as? BasePageKeyedDataSource<*, *>)?.initialPage ?: 1
    val enablePlaceholders = currentList?.config?.enablePlaceholders ?: false
    val layoutManager = recyclerView!!.layoutManager as LinearLayoutManager
    val visibleItemPosition = layoutManager.findFirstVisibleItemPosition()
    val positionRelative = (visibleItemPosition % pageSize)
    val itemTopOffset = layoutManager.findViewByPosition(visibleItemPosition)?.top ?: 0

    val page = visibleItemPosition / pageSize +
            if (initialPage > 1 && loadedBeforePage != 1 && !enablePlaceholders) {
                loadedBeforePage
            } else {
                1
            }

    val positionAbsolute =
        if (enablePlaceholders) {
            visibleItemPosition
        } else {
            ((if (loadedBeforePage > 0) loadedBeforePage else initialPage) - 1) * pageSize +
                    visibleItemPosition
        }

    return bundleOf(
        PAGE to page,
        ITEM_POSITION_RELATIVE to positionRelative,
        ITEM_POSITION_ABSOLUTE to positionAbsolute,
        ITEM_TOP_OFFSET to itemTopOffset
    )
}
