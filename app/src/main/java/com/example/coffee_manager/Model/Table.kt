package com.example.coffee_manager.Model

data class Table(
    val idTable: String = "",
    val number: Int = 0,
    val status: Status = Status.EMPTY,
    val billId: String? = null
    ) {
        enum class Status { EMPTY, OCCUPIED, DAMAGED }
    }

