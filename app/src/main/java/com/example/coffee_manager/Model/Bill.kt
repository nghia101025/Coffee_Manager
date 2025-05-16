package com.example.coffee_manager.Model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class BillItem(
    val foodId: String = "",
    val name: String = "",
    val price: Long = 0L,
    val quantity: Int = 0
) {

}

@IgnoreExtraProperties
data class Bill(
    val idBill: String = "",
    val idTable: String = "",
    val items: List<BillItem> = emptyList(),
    val note: String = "",
    val discountPercent: Int = 0,
    val totalPrice: Long = 0L,
    val paid: Boolean = false,
    val processed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {

}
