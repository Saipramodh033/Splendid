package com.splendid.domain.model

data class Category(
    val id: Int = 0,
    val name: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val sortOrder: Int
)
