package com.example.paging.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import org.hamcrest.Matcher

class RecyclerViewInteraction<T, out V : RecyclerView.ViewHolder>(
    private val viewMatcher: Matcher<View>
) {

    companion object {
        fun <T, V : RecyclerView.ViewHolder> onRecyclerView(
            viewMatcher: Matcher<View>
        ): RecyclerViewInteraction<T, V> {
            return RecyclerViewInteraction(viewMatcher)
        }
    }

    private lateinit var items: List<T>

    fun withItems(items: List<T>): RecyclerViewInteraction<T, V> {
        this.items = items
        return this
    }

    fun check(itemViewAssertion: ItemViewAssertion<T>): RecyclerViewInteraction<T, V> {
        items.forEachIndexed { index, item ->
            onView(viewMatcher)
                .perform(scrollToPosition<V>(index))
                .check(RecyclerItemViewAssertion(index, item, itemViewAssertion))
        }

        return this
    }

    interface ItemViewAssertion<T> {
        fun check(item: T, view: View, e: NoMatchingViewException?)
    }
}