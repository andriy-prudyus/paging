package com.example.network.utils

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.ceil

fun fakeGetItemsResponse(pageIndex: Int, pageSize: Int): String {
    val itemsCount = 51
    val result = JSONObject().apply {
        put("items_count", itemsCount)
    }

    val items = JSONArray()

    val max = if (ceil(itemsCount.toDouble() / pageSize.toDouble()).toInt() == pageIndex) {
        (itemsCount % pageSize).let { if (it > 0) it else pageSize }
    } else {
        pageSize
    }

    for (i in 1..max) {
        val number = (pageIndex - 1) * pageSize + i

        items.put(JSONObject().apply {
            put("id", number)
            put("name", "Item $number")
            put("image_url", "https://live.staticflickr.com/7309/26589359933_935e70aed0_b.jpg")
        })
    }

    return result.apply {
        put("items", items)
    }.toString()
}