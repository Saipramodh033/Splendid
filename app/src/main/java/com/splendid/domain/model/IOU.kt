package com.splendid.domain.model

enum class IOUType {
    WILL_RECEIVE,
    I_OWE
}

data class IOU(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: IOUType,
    val date: Long,
    val isSettled: Boolean,
    val settledDate: Long? = null
)
