package com.example.coffee_manager.Model

data class Table(

    val IdTable: Int = 0,
    val seatCount: Int,             // số lượng khách ngồi
    val isAvailable: Boolean,       // còn trống hay không
    val note: String
)
