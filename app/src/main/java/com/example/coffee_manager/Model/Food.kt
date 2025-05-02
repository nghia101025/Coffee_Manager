package com.example.coffee_manager.Model

data class Food(
    val IdFood: Int =0,
    val name: String,
    val ingredients: String,
    val recipe: String?,
    val price: Double
)
