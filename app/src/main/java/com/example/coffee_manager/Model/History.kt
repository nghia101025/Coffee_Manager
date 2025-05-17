// Model/History.kt
package com.example.coffee_manager.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class History(
    val id: String = "",
    val userId: String = "",
    val action: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
