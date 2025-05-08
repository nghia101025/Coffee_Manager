package com.example.coffee_manager.Model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val idUser: String = "0",
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val age: Int = 0,
    val phone: String = "",
    val role: String = "",
    val imageUrl: String = ""
)
