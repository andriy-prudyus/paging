package com.example.paging.ui.items.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.paging.databinding.ListItemBinding
import com.example.paging.ui.items.list.model.Item
import com.example.paging.utils.load
import javax.inject.Inject

class ItemListAdapter @Inject constructor(
    itemCallback: DiffUtilItemCallback
) : PagedListAdapter<Item, ItemListAdapter.ItemViewHolder>(itemCallback) {

    interface ActionListener {
        fun onItemClicked(item: Item)
    }

    var listener: ActionListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class ItemViewHolder(
        private val binding: ListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item) {
            binding.nameTextView.text = item.name
            binding.imageView.load(item.imageUrl)
        }
    }
}