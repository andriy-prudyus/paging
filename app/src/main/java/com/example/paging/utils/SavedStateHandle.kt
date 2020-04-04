package com.example.paging.utils

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import com.example.paging.ITEM_POSITION
import com.example.paging.ITEM_TOP_OFFSET
import com.example.paging.PAGE

fun SavedStateHandle.savePagingState(state: Bundle) {
    set(PAGE, state.getInt(PAGE))
    set(ITEM_POSITION, state.getInt(ITEM_POSITION))
    set(ITEM_TOP_OFFSET, state.getInt(ITEM_TOP_OFFSET))
}

fun SavedStateHandle.getPage(): Int = get<Int>(PAGE)?.let { if (it < 1) 1 else it } ?: 1

fun SavedStateHandle.getItemPosition(): Int = get<Int>(ITEM_POSITION) ?: 0

fun SavedStateHandle.getItemTopOffset(): Int = get<Int>(ITEM_TOP_OFFSET) ?: 0

fun SavedStateHandle.resetPagingState() {
    set(PAGE, 1)
    set(ITEM_POSITION, 0)
    set(ITEM_TOP_OFFSET, 0)
}
