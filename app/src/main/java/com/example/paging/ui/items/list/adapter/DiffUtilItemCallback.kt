package com.example.paging.ui.items.list.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.paging.ui.items.list.model.Item
import javax.inject.Inject

class DiffUtilItemCallback @Inject constructor() : DiffUtil.ItemCallback<Item>() {

    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean = oldItem == newItem
}