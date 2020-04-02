package com.example.paging.ui.items.list.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.paging.architecture.adapter.PagedRecyclerViewAdapter
import com.example.paging.architecture.delegate.AutoClearedValue
import com.example.paging.databinding.ListItemBinding
import com.example.paging.ui.items.list.model.Item
import com.example.paging.utils.load

class ItemListAdapter(
    lifecycleOwner: LifecycleOwner
) : PagedRecyclerViewAdapter<Item, ItemListAdapter.ItemViewHolder>(lifecycleOwner, itemCallback) {

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

    interface ActionListener {
        fun onItemClicked(item: Item)
    }

    var listener: ActionListener? = null

    private var itemBinding by AutoClearedValue<ListItemBinding> { lifecycleOwner }

    override fun onCreateRegularViewHolder(parent: ViewGroup): ItemViewHolder {
        itemBinding = ListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder()
    }

    override fun onBindRegularViewHolder(holder: ItemViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class ItemViewHolder :
        RecyclerView.ViewHolder(itemBinding.root),
        View.OnClickListener {

        private lateinit var item: Item

        init {
            itemBinding.root.setOnClickListener(this)
        }

        fun bind(item: Item) {
            this.item = item
            itemBinding.nameTextView.text = item.name
            itemBinding.imageView.load(item.imageUrl)
        }

        override fun onClick(v: View?) {
            listener?.onItemClicked(item)
        }
    }
}