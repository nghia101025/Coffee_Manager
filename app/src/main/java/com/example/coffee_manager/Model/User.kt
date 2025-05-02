package com.example.coffee_manager.Model

data class User(
    val IdUser: Int = 0,
    val email: String,
    val password: String,
    val name: String,
    val age: Int = 0,  // Đảm bảo là kiểu Int
    val address: String,
    val role: String
)
