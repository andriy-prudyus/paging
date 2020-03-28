package com.example.database.entity.item

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DbItem(
    @PrimaryKey
    val id: Long,
    val name: String,
    val imageUrl: String
)