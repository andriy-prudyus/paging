package com.example.paging.ui.items.list.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.paging.architecture.adapter.PagedRecyclerViewAdapter
import com.example.paging.databinding.ListItemBinding
import com.example.paging.ui.items.list.model.Item
import timber.log.Timber

class ItemListAdapter(
    lifecycleOwner: LifecycleOwner
) : PagedRecyclerViewAdapter<Item, ItemListAdapter.ItemViewHolder>(itemCallback),
    LifecycleObserver {

    companion object {
        private val itemCallback = object : DiffUtil.ItemCallback<Item>() {

            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem == newItem
            }
        }
    }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    interface ActionListener {
        fun onItemClicked(item: Item)
    }

    var listener: ActionListener? = null

//    private var lifecycleRegistry = LifecycleRegistry(this)

//    private var binding by AutoClearedValue<ItemViewHolder>()
//    private var binding: ListItemBinding? = null

    /*override fun getLifecycle(): Lifecycle {
        Timber.e("getLifecycle")
        return lifecycleRegistry
    }*/

    override fun onCreateRegularViewHolder(parent: ViewGroup): ItemViewHolder {
        return ItemViewHolder(
            ListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindRegularViewHolder(holder: ItemViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class ItemViewHolder(
        private var binding: ListItemBinding
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener, LifecycleObserver {

        private lateinit var item: Item

        init {
//            fragment.viewLifecycleOwner.lifecycle.addObserver(this)
            binding.root.setOnClickListener(this)
        }

        fun bind(item: Item) {
            this.item = item
            binding.nameTextView.text = item.name
            binding.imageView.load(item.imageUrl)
        }

        override fun onClick(v: View?) {
            listener?.onItemClicked(item)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            Timber.e("onDestroy")
            binding = null
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        Timber.e("onDestroy")
//        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
//        binding = null
    }
}