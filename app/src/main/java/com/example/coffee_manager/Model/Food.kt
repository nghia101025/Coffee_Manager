package com.example.coffee_manager.Model

import androidx.compose.runtime.Composable


data class Food(
    val idFood: String = "",
    val name: String,
    val recipe: String,
    val price: Long,
    val isAvailable: Boolean = true,
    val imageUrl: String = "",
    val category: String, // Dùng model Category
    val soldCount: Long = 0L
) {
    // Firebase yêu cầu constructor không tham số
    constructor() : this(
        idFood = "",
        name = "",
        recipe = "",
        price = 0L,
        isAvailable = true,
        imageUrl = "",
        category = "",
        soldCount = 0L
    )
}

// FeatureItem thì bạn đã có sẵn
data class FeatureItem(
    val title: String,
    val icon: @Composable () -> Unit,
    val route: String
)

// Thêm 1 lớp để nhóm theo danh mục
data class FeatureSection(
    val sectionTitle: String,
    val items: List<FeatureItem>
)






