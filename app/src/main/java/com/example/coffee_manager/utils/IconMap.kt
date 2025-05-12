// utils/MaterialIconMap.kt
package com.example.coffee_manager.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

// Ánh xạ tên logic → ImageVector
object MaterialIconMap {
    val map = mapOf(
        "Cafe"      to Icons.Default.LocalCafe,
        "Fastfood"  to Icons.Default.Fastfood,
        "Cake"      to Icons.Default.Cake,
        "Drink"     to Icons.Default.LocalDrink,
        "Dessert"   to Icons.Default.Icecream,
        "Bakery"    to Icons.Default.BakeryDining
    )
    val names = map.keys.toList()
}
