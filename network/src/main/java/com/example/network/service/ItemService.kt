package com.example.network.service

import com.example.network.dto.GetItemsResponse
import retrofit2.http.POST
import retrofit2.http.Query

interface ItemService {

    @POST("/get_items")
    suspend fun loadItems(
        @Query("page_index") pageIndex: Int,
        @Query("page_size") pageSize: Int
    ): GetItemsResponse
}