package com.splendid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.splendid.domain.model.IOUType

@Entity(tableName = "iou")
data class IOUEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: IOUType,
    val isSettled: Boolean = false,
    val date: Long,
    val settledDate: Long? = null
)
