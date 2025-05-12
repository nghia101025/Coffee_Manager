// View/Order/CartScreen.kt
package com.example.coffee_manager.View.Order

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Admin.FoodController
import com.example.coffee_manager.Controller.Order.BillController
import com.example.coffee_manager.Model.CartItem
import com.example.coffee_manager.Model.Food
import com.example.coffee_manager.View.CommonTopBar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController) {
    val billController = remember { BillController() }
    val foodController = remember { FoodController() }
    val scope = rememberCoroutineScope()

    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var foodsMap by remember { mutableStateOf<Map<String, Food>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }

    // Load giỏ hàng và thông tin món
    LaunchedEffect(Unit) {
        isLoading = true
        billController.getCart()
            .onSuccess { cis ->
                cartItems = cis
                // Lấy data food để map
                val foods = cis.map { it.foodId }
                    .distinct()
                    .mapNotNull { id ->
                        foodController.getFoodById(id).getOrNull()
                    }
                foodsMap = foods.associateBy { it.idFood }
            }
            .onFailure { message = it.message }
        isLoading = false
    }

    // Tổng tiền
    val totalPrice = cartItems.sumOf { ci ->
        val price = foodsMap[ci.foodId]?.price ?: 0L
        price * ci.quantity
    }

    Scaffold(
        topBar = {
            CommonTopBar(navController, title = "Giỏ hàng")
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tổng: ${
                            NumberFormat.getNumberInstance(Locale("vi", "VN"))
                                .format(totalPrice)
                        }₫",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(onClick = { /* TODO: Thanh toán */ }) {
                        Text("Thanh toán")
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                cartItems.isEmpty() -> Text("Giỏ hàng trống", Modifier.align(Alignment.Center))
                else -> {
                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(cartItems, key = { it.foodId }) { ci ->
                            val food = foodsMap[ci.foodId] ?: return@items
                            CartItemRow(
                                food = food,
                                quantity = ci.quantity,
                                onIncrease = {
                                    scope.launch {
                                        billController.updateQuantity(ci.foodId, ci.quantity + 1)
                                            .onSuccess {
                                                cartItems = cartItems.map {
                                                    if (it.foodId == ci.foodId) it.copy(quantity = it.quantity + 1)
                                                    else it
                                                }
                                            }
                                            .onFailure { message = "Cập nhật thất bại: ${it.message}" }
                                    }
                                },
                                onDecrease = {
                                    if (ci.quantity > 1) {
                                        scope.launch {
                                            billController.updateQuantity(ci.foodId, ci.quantity - 1)
                                                .onSuccess {
                                                    cartItems = cartItems.map {
                                                        if (it.foodId == ci.foodId) it.copy(quantity = it.quantity - 1)
                                                        else it
                                                    }
                                                }
                                                .onFailure { message = "Cập nhật thất bại: ${it.message}" }
                                        }
                                    }
                                },
                                onRemove = {
                                    scope.launch {
                                        billController.removeFromCart(ci.foodId)
                                            .onSuccess {
                                                cartItems = cartItems.filterNot { it.foodId == ci.foodId }
                                            }
                                            .onFailure { message = "Xóa thất bại: ${it.message}" }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            message?.let {
                Snackbar(Modifier.align(Alignment.BottomCenter)) { Text(it) }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    food: Food,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh
            val bmp = runCatching {
                val bytes = Base64.decode(food.imageUrl, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }.getOrNull()
            if (bmp != null) {
                Image(
                    bmp.asImageBitmap(),
                    contentDescription = food.name,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier
                        .size(64.dp)
                        .background(Color.LightGray)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(
                    "Giá: ${
                        NumberFormat.getNumberInstance(Locale("vi", "VN"))
                            .format(food.price)
                    }₫",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Row điều chỉnh số lượng
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrease, enabled = quantity > 1) {
                        Icon(Icons.Default.Remove, contentDescription = "Giảm")
                    }
                    Text("$quantity", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 8.dp))
                    IconButton(onClick = onIncrease) {
                        Icon(Icons.Default.Add, contentDescription = "Tăng")
                    }
                }

                Text(
                    "Tổng: ${
                        NumberFormat.getNumberInstance(Locale("vi", "VN"))
                            .format(food.price * quantity)
                    }₫",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa")
            }
        }
    }
}
