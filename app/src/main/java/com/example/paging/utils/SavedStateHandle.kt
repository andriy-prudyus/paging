package com.example.paging.utils

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import com.example.paging.ITEM_POSITION_ABSOLUTE
import com.example.paging.ITEM_POSITION_RELATIVE
import com.example.paging.ITEM_TOP_OFFSET
import com.example.paging.PAGE

fun SavedStateHandle.savePagingState(state: Bundle) {
    set(PAGE, state.getInt(PAGE))
    set(ITEM_POSITION_RELATIVE, state.getInt(ITEM_POSITION_RELATIVE))
    set(ITEM_POSITION_ABSOLUTE, state.getInt(ITEM_POSITION_ABSOLUTE))
    set(ITEM_TOP_OFFSET, state.getInt(ITEM_TOP_OFFSET))
}

fun SavedStateHandle.getPage(): Int = get<Int>(PAGE)?.let { if (it < 1) 1 else it } ?: 1

fun SavedStateHandle.getItemPositionRelative(): Int = get<Int>(ITEM_POSITION_RELATIVE) ?: 0

fun SavedStateHandle.getItemPositionAbsolute(): Int = get<Int>(ITEM_POSITION_ABSOLUTE) ?: 0

fun SavedStateHandle.getItemTopOffset(): Int = get<Int>(ITEM_TOP_OFFSET) ?: 0

fun SavedStateHandle.resetPagingState() {
    set(PAGE, 1)
    set(ITEM_POSITION_RELATIVE, 0)
    set(ITEM_POSITION_ABSOLUTE, 0)
    set(ITEM_TOP_OFFSET, 0)
}
