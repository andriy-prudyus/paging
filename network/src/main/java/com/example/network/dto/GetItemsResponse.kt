package com.example.network.dto

import com.google.gson.annotations.SerializedName

data class GetItemsResponse(
    @SerializedName("items")
    val items: List<ItemDto>,

    @SerializedName("items_count")
    val itemsCount: Int
)