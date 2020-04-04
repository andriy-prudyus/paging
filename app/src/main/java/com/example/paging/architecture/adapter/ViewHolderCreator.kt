package com.example.paging.architecture.adapter

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.paging.architecture.delegate.AutoClearedValue

abstract class ViewHolderCreator<H : RecyclerView.ViewHolder, B : ViewBinding>(
    lifecycleOwner: LifecycleOwner
) {

    private var binding by AutoClearedValue<B> { lifecycleOwner }

    abstract fun createBinding(parent: ViewGroup): B
    abstract fun createViewHolder(binding: B): H

    fun createViewHolder(parent: ViewGroup): H {
        binding = createBinding(parent)
        return createViewHolder(binding)
    }
}
