// utils/MaterialIconMap.kt
package com.example.coffee_manager.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

object MaterialIconMap {
    // Ánh xạ tên logic → ImageVector
    val map = mapOf(
        "Cafe"      to Icons.Filled.LocalCafe,
        "Fastfood"  to Icons.Filled.Fastfood,
        "Cake"      to Icons.Filled.Cake,
        "Drink"     to Icons.Filled.LocalDrink,
        "Dessert"   to Icons.Filled.Icecream,
        "Pastry"    to Icons.Filled.BakeryDining
    )
    val names = map.keys.toList()
}
