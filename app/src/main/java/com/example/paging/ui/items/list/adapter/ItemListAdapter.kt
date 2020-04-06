package com.example.paging.ui.items.list.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.paging.R
import com.example.paging.architecture.adapter.BaseViewHolder
import com.example.paging.architecture.adapter.PagedRecyclerViewAdapter
import com.example.paging.architecture.adapter.ViewHolderCreator
import com.example.paging.databinding.ListItemBinding
import com.example.paging.ui.items.list.model.Item
import com.example.paging.utils.load

class ItemListAdapter(
    lifecycleOwner: LifecycleOwner
) : PagedRecyclerViewAdapter<Item, ItemListAdapter.ItemViewHolder, ListItemBinding>(
    lifecycleOwner,
    itemCallback
) {

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

    override fun getRegularViewHolderCreator(
        lifecycleOwner: LifecycleOwner
    ): ViewHolderCreator<ItemViewHolder, ListItemBinding> {
        return object : ViewHolderCreator<ItemViewHolder, ListItemBinding>(lifecycleOwner) {

            override fun createBinding(parent: ViewGroup): ListItemBinding {
                return ListItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            }

            override fun createViewHolder(binding: ListItemBinding): ItemViewHolder {
                return ItemViewHolder(binding)
            }
        }
    }

    override fun onCreateRegularViewHolder(
        parent: ViewGroup,
        binding: ListItemBinding
    ): ItemViewHolder {
        return ItemViewHolder(binding)
    }

    override fun getPlaceholderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return PlaceholderViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_placeholder, parent, false)
        )
    }

    override fun onBindRegularViewHolder(holder: ItemViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class ItemViewHolder(
        binding: ListItemBinding
    ) : BaseViewHolder<ListItemBinding>(binding), View.OnClickListener {

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

    class PlaceholderViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
