package com.example.paging.ui.items.list.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.paging.architecture.adapter.PagedRecyclerViewAdapter
import com.example.paging.databinding.ListItemBinding
import com.example.paging.ui.items.list.model.Item
import com.example.paging.utils.load

class ItemListAdapter(
    itemCallback: DiffUtilItemCallback
) : PagedRecyclerViewAdapter<Item, ItemListAdapter.ItemViewHolder>(itemCallback) {

    interface ActionListener {
        fun onItemClicked(item: Item)
    }

    var listener: ActionListener? = null

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
        private val binding: ListItemBinding
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var item: Item

        init {
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
    }
}