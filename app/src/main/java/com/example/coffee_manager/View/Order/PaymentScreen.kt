package com.example.coffee_manager.View.Order

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.coffee_manager.Controller.Order.BillController
import com.example.coffee_manager.Controller.Admin.FoodController
import com.example.coffee_manager.Model.CartItem
import com.example.coffee_manager.Model.Food
import com.example.coffee_manager.View.CommonTopBar
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    billController: BillController = BillController(),
    foodController: FoodController = FoodController()
) {
    val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var foodsMap by remember { mutableStateOf<Map<String, Food>>(emptyMap()) }
    var tableNumber by remember { mutableStateOf("") }
    var promoCode by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("Tiền mặt") }
    var message by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Load cart and food data
    LaunchedEffect(Unit) {
        isLoading = true
        billController.getCart()
            .onSuccess { cis ->
                cartItems = cis
                Log.d("PaymentScreen", "cartItems size: ${cis.size}")

                val foods = cis.map { it.foodId }
                    .distinct()
                    .mapNotNull { id ->
                        val foodResult = foodController.getFoodById(id).getOrNull()
                        Log.d("PaymentScreen", "Fetched food for ID $id: ${foodResult?.name}")
                        foodResult
                    }
                foodsMap = foods.associateBy { it.idFood }
                Log.d("PaymentScreen", "foodsMap size: ${foodsMap.size}")
            }
            .onFailure {
                message = it.message
                Log.e("PaymentScreen", "Error loading cart: ${it.message}")
            }
        isLoading = false
    }

    val subtotal = cartItems.sumOf { ci ->
        val price = foodsMap[ci.foodId]?.price ?: 0L
        price * ci.quantity
    }.toDouble()
    val discount = 0.0
    val total = subtotal - discount

    Scaffold(
        topBar = { CommonTopBar(navController, title = "Trang thanh toán") },
        bottomBar = {
            Button(
                onClick = { /* Xử lý đặt hàng */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Đặt hàng", style = MaterialTheme.typography.titleMedium)
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                message != null -> {
                    Text(
                        text = message!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Nhập số bàn
                        OutlinedTextField(
                            value = tableNumber,
                            onValueChange = { tableNumber = it },
                            label = { Text("Nhập số bàn") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Danh sách món
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            LazyColumn(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(cartItems) { ci ->
                                    val food = foodsMap[ci.foodId] ?: return@items
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF7F7F7))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(food.imageUrl),
                                            contentDescription = food.name,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(food.name, style = MaterialTheme.typography.bodyLarge)
                                            Text("x${ci.quantity}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text(
                                            "${fmt.format(food.price * ci.quantity)}₫",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }

                        // Nhập mã giảm giá
                        OutlinedTextField(
                            value = promoCode,
                            onValueChange = { promoCode = it },
                            label = { Text("Nhập mã giảm giá") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Phương thức + Tổng
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Phương thức thanh toán", style = MaterialTheme.typography.bodyLarge)
                            Text("${fmt.format(total)}₫", style = MaterialTheme.typography.bodyLarge)
                        }

                        // Chọn phương thức
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Tiền mặt", "Chuyển khoản").forEach { method ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedMethod = method }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedMethod == method,
                                            onClick = { selectedMethod = method }
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(method, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }

                        // Tóm tắt tính toán
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Tạm tính", style = MaterialTheme.typography.bodyMedium)
                                    Text("${fmt.format(subtotal)}₫", style = MaterialTheme.typography.bodyMedium)
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Giảm giá", style = MaterialTheme.typography.bodyMedium)
                                    Text("-${fmt.format(discount)}₫", style = MaterialTheme.typography.bodyMedium)
                                }
                                Divider(Modifier.padding(vertical = 4.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Tổng cộng", style = MaterialTheme.typography.titleMedium)
                                    Text("${fmt.format(total)}₫", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
