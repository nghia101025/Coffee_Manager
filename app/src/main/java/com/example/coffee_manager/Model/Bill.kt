package com.example.coffee_manager.Model

data class BillItem(
    val foodId: String = "",
    val name: String = "",
    val price: Long = 0L,
    val quantity: Int = 1
)

data class Bill(
    val idBill: String = "",
    val idTable: String = "",
    val items: List<BillItem> = emptyList(),
    val note: String = "",
    val discountPercent: Int = 0,
    val totalPrice: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
