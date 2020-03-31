package com.example.paging.utils

import androidx.annotation.StringRes
import com.example.paging.R
import com.example.paging.architecture.exception.AppException
import com.example.paging.architecture.exception.AppException.Code.*

@StringRes
fun localizedErrorMessage(e: Throwable): Int {
    return when (e) {
        is AppException -> localizedErrorMessage(e)
        else -> R.string.error_unknown
    }
}

@StringRes
fun localizedErrorMessage(e: AppException): Int {
    return when (e.code) {
        INCORRECT_INITIAL_PAGE_INDEX -> R.string.error_incorrect_initial_page_index
        UNKNOWN_EXPECTED_ITEMS_COUNT -> R.string.error_unknown_expected_items_count
        CACHED_ITEMS_MORE_THAN_EXPECTED -> R.string.error_cached_items_more_than_expected
        INCORRECT_ITEM_ID -> R.string.error_incorrect_item_id
    }
}