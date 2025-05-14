package com.example.coffee_manager.Model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Table(
    val idTable: String = "",
    val number: Int = 0,
    val status: Status = Status.EMPTY,
    val currentBillId: String? = null
) {
    enum class Status {
        EMPTY,
        OCCUPIED,
        DAMAGED
    }
}
