package com.example.coffee_manager.Model

data class PromoCode(
    val code: String = "",
    val type: Type = Type.AMOUNT,
    val value: Long = 0L
) {
    enum class Type { PERCENT, AMOUNT }
}