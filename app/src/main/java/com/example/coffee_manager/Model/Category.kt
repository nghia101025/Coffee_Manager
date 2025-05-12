
package com.example.coffee_manager.Model

/**
 * Entity Category lưu trên Firestore.
 */

data class Category(
    val idCat: String,
    val name: String,
    val iconName: String // lưu tên icon được chọn, ví dụ "Coffee" hoặc resource name
) {
    // Firebase yêu cầu constructor không tham số
    constructor() : this(
        idCat = "",
        name = "",
        iconName = ""
    )
}