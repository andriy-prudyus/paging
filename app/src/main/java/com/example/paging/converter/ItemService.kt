package com.example.paging.converter

import com.example.database.entity.item.DbItem
import com.example.network.dto.GetItemsResponse
import com.example.network.dto.ItemDto
import com.example.paging.ui.items.list.model.Item

fun GetItemsResponse.toDbEntities(): Pair<List<DbItem>, Int> {
    return items.map { it.toDbEntity() } to itemsCount
}

fun List<DbItem>.toItems(): List<Item> {
    return this.map { Item(it.id, it.name, it.imageUrl) }
}

private fun ItemDto.toDbEntity(): DbItem = DbItem(id, name, imageUrl)