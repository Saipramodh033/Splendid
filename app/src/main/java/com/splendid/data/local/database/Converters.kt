package com.splendid.data.local.database

import androidx.room.TypeConverter
import com.splendid.domain.model.IOUType

class Converters {
    
    @TypeConverter
    fun fromIOUType(value: IOUType): String {
        return value.name
    }
    
    @TypeConverter
    fun toIOUType(value: String): IOUType {
        // Handle backward compatibility with old enum values
        return when (value) {
            "OWE" -> IOUType.I_OWE
            "RECEIVE" -> IOUType.WILL_RECEIVE
            else -> IOUType.valueOf(value)
        }
    }
}
