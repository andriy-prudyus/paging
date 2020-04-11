package com.example.paging.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.PerformException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.util.HumanReadables

class RecyclerItemViewAssertion<T>(
    private val position: Int,
    private val item: T,
    private val itemViewAssertion: RecyclerViewInteraction.ItemViewAssertion<T>
) : ViewAssertion {

    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        val viewHolder = (view as RecyclerView).findViewHolderForLayoutPosition(position)

        if (viewHolder == null) {
            throw PerformException.Builder()
                .withActionDescription(toString())
                .withViewDescription(HumanReadables.describe(view))
                .withCause(IllegalStateException("No view holder at position: $position"))
                .build()
        } else {
            itemViewAssertion.check(item, viewHolder.itemView, noViewFoundException)
        }
    }
}