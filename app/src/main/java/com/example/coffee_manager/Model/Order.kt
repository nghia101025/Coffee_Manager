package com.example.coffee_manager.Model

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val tableNumber: String = "",
    val promoCode: String = "",
    val paymentMethod: String = "",
    val subtotal: Long = 0L,
    val discount: Long = 0L,
    val total: Long = 0L,
    val timestamp: Long = 0L
)