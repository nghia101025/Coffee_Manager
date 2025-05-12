package com.example.coffee_manager.Model

import com.google.firebase.firestore.IgnoreExtraProperties
import java.time.LocalDate

@IgnoreExtraProperties
data class User(
    val idUser: String = "",
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val dateOfBirth: LocalDate?,
    val phone: String = "",
    val role: String = "",
    val imageUrl: String = ""
){
    // Firestore cần constructor không tham số
    constructor() : this(
        idUser = "",
        email = "",
        password = "",
        name = "",
        dateOfBirth = null,
        phone = "",
        role = "",
        imageUrl = ""
    )
}
object SessionManager {
    var currentUserId: String = ""
}