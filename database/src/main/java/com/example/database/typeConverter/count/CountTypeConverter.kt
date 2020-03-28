package com.example.database.typeConverter.count

import androidx.room.TypeConverter

class CountTypeConverter {

    @TypeConverter
    fun fromValue(value: Int): CountType = CountType.values()[value]

    @TypeConverter
    fun toValue(type: CountType): Int = type.ordinal
}