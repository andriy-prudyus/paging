package com.example.paging.utils

import androidx.paging.PagedList
import com.example.paging.PAGE_SIZE
import kotlin.math.ceil

fun pagedListConfig(
    enablePlaceholders: Boolean = true,
    pageSize: Int = PAGE_SIZE
): PagedList.Config {
    return PagedList.Config.Builder()
        .setEnablePlaceholders(enablePlaceholders)
        .setPageSize(pageSize)
        .setInitialLoadSizeHint(pageSize)
        .setPrefetchDistance(pageSize)
        .build()
}

fun calculateInitialPage(itemPosition: Int, pageSize: Int): Int {
    return if (itemPosition == 0) {
        1
    } else {
        ceil(itemPosition.toDouble() / pageSize.toDouble()).toInt()
    }
}