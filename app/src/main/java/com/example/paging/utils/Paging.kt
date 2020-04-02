package com.example.paging.utils

import androidx.paging.PagedList
import com.example.paging.PAGE_SIZE

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