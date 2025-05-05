package com.example.coffee_manager.Model

data class Food(
    val idFood: String = "",
    val name: String = "",
    val ingredients: String = "",
    val recipe: String = "",
    val price: String = "",       // match kiểu trên Firestore
    val isAvailable: Boolean = true,
    val imageUrl: String = ""      // đúng tên field trong Firestore
)




