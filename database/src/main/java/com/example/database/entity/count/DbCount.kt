package com.example.database.entity.count

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.database.typeConverter.count.CountType

@Entity
data class DbCount(
    @PrimaryKey
    val type: CountType,
    val count: Int
)