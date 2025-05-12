
package com.example.coffee_manager.Model

/**
 * Entity Category lưu trên Firestore.
 */

data class Category(
    val idCat: String,
    val name: String,
) {
    // Firebase yêu cầu constructor không tham số
    constructor() : this(
        idCat = "",
        name = "",
    )
}