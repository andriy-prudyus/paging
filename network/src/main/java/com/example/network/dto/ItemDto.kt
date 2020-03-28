package com.example.network.dto

import com.google.gson.annotations.SerializedName

data class ItemDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("image_url")
    val imageUrl: String
)