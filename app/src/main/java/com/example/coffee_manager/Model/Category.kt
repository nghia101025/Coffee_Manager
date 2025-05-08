
package com.example.coffee_manager.Model

/**
 * Entity Category lưu trên Firestore.
 */

data class Category(
    var idCat: String = "",    // Firestore document ID
    var name: String = ""   // Tên danh mục (ví dụ: "Trà sữa", "Coffee")
) {
    // Firebase yêu cầu constructor không tham số
    constructor() : this(
        idCat = "",
        name = ""
    )
}