package com.splendid.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budget",
    indices = [Index(value = ["month", "year"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val month: Int, // 1-12
    val year: Int,
    val limitAmount: Double
)
