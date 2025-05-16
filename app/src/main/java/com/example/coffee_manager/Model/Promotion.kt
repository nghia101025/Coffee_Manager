package com.example.coffee_manager.Model

import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.*

@IgnoreExtraProperties
data class Promotion(
    val id: String = "",
    val code: String = "",
    val discountPercent: Int = 0,
    val expiresAt: Date = Date()
)
