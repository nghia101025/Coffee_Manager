package com.example.coffee_manager.Model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class CartItem(
    val id: String = "",
    val userId: String = "",
    val foodId: String = "",
    val quantity: Int = 0
)
