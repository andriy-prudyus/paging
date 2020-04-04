package com.example.paging.architecture.adapter

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView

class ItemsDecoration(
    context: Context,
    @DimenRes
    offsetResLeft: Int? = null,
    @DimenRes
    offsetResTop: Int? = null,
    @DimenRes
    offsetResRight: Int? = null,
    @DimenRes
    offsetResBottom: Int? = null
) : RecyclerView.ItemDecoration() {

    private var offsetPxLeft = 0
    private var offsetPxTop = 0
    private var offsetPxRight = 0
    private var offsetPxBottom = 0

    init {
        offsetPxLeft = offsetResLeft?.let { context.resources.getDimensionPixelSize(it) } ?: 0
        offsetPxTop = offsetResTop?.let { context.resources.getDimensionPixelSize(it) } ?: 0
        offsetPxRight = offsetResRight?.let { context.resources.getDimensionPixelSize(it) } ?: 0
        offsetPxBottom = offsetResBottom?.let { context.resources.getDimensionPixelSize(it) } ?: 0
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(offsetPxLeft, offsetPxTop, offsetPxRight, offsetPxBottom)
    }
}