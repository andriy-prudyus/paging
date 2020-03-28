package com.example.paging.utils

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes

fun getColor(context: Context, @AttrRes resId: Int): Int {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(resId, typedValue, true)
    return typedValue.data
}