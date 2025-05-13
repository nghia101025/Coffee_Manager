// View/Order/PaymentScreen.kt
package com.example.coffee_manager.View.Order

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.coffee_manager.Controller.Admin.FoodController
import com.example.coffee_manager.Controller.Admin.TableController
import com.example.coffee_manager.Controller.Order.BillController
import com.example.coffee_manager.Model.Bill
import com.example.coffee_manager.Model.BillItem
import com.example.coffee_manager.Model.CartItem
import com.example.coffee_manager.Model.Food
import com.example.coffee_manager.View.CommonTopBar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

private const val TAG = "PaymentScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    controller: BillController = BillController(),
    tableController: TableController = TableController(),
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
    val scope = rememberCoroutineScope()

    // 1) Load cart and foods
    LaunchedEffect(Unit) {
        Log.d(TAG, "Loading cart…")
        isLoading = true
        controller.getCart()
            .onSuccess { cis ->
                Log.d(TAG, "Cart loaded: ${cis.size}")
                cartItems = cis
                // fetch foods
                val foods = cis.map { it.foodId }
                    .distinct()
                    .mapNotNull { id ->
                        foodController.getFoodById(id).getOrNull().also {
                            Log.d(TAG, "Food $id -> $it")
                        }
                    }
                foodsMap = foods.associateBy { it.idFood.toString() }
                Log.d(TAG, "foodsMap keys=${foodsMap.keys}")
            }
            .onFailure {
                message = "Lỗi tải giỏ hàng: ${it.message}"
                Log.e(TAG, it.message ?: "", it)
            }
        isLoading = false
    }

    // 2) Compute totals
    val subtotal = cartItems.sumOf { ci ->
        (foodsMap[ci.foodId]?.price ?: 0L) * ci.quantity
    }
    val discountPercent = promoCode.toIntOrNull() ?: 0
    val discountAmount = subtotal * discountPercent / 100
    val totalPrice = subtotal - discountAmount
    Log.d(TAG, "subtotal=$subtotal discount=$discountAmount total=$totalPrice")

    Scaffold(
        topBar = { CommonTopBar(navController, title = "Thanh toán") },
        bottomBar = {
            Box(Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        if (tableNumber.isBlank()) {
                            message = "Vui lòng nhập số bàn"
                            return@Button
                        }
                        scope.launch {
                            isLoading = true
                            // check table status
                            runCatching {
                                tableController.getTableStatus(tableNumber)
                            }.fold(onSuccess = { status ->
                                Log.d(TAG, "Table $tableNumber status=$status")
                                if (status != "EMPTY") {
                                    message = "Bàn $tableNumber không trống"
                                    isLoading = false
                                    return@launch
                                }
                            }, onFailure = {
                                message = "Lỗi lấy trạng thái bàn: ${it.message}"
                                isLoading = false
                                return@launch
                            })

                            // build items & bill
                            val items = cartItems.map { ci ->
                                val f = foodsMap[ci.foodId]!!
                                BillItem(ci.foodId, f.name, f.price.toLong(), ci.quantity)
                            }
                            val bill = Bill(
                                idBill = "",
                                idTable = tableNumber,
                                items = items,
                                note = selectedMethod,
                                discountPercent = discountPercent,
                                totalPrice = totalPrice,
                                isPaid = true,
                                isProcessed = false
                            )
                            // create bill
                            controller.createBill(bill)
                                .onSuccess { id ->
                                    Log.d(TAG, "Bill created id=$id")
                                    // update table
                                    tableController.updateTableStatus(tableNumber, "OCCUPIED")
                                        .onSuccess { Log.d(TAG, "Table updated") }
                                        .onFailure { Log.e(TAG, "Error updating table", it) }
                                    // clear cart
                                    controller.clearCart()
                                        .onSuccess { Log.d(TAG, "Cart cleared") }
                                        .onFailure { Log.e(TAG, "Error clearing cart", it) }
                                    // navigate success
                                    navController.navigate("orderSuccess/$id") {
                                        popUpTo("payment") { inclusive = true }
                                    }
                                }
                                .onFailure {
                                    message = "Lỗi tạo đơn: ${it.message}"
                                    Log.e(TAG, "createBill failed", it)
                                }
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Lên đơn • ${fmt.format(totalPrice)}₫")
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
            message?.let { msg ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) { Text(msg) }
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = tableNumber,
                    onValueChange = { tableNumber = it },
                    label = { Text("Số bàn") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    Modifier.fillMaxWidth(),
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
                            val bmp = runCatching {
                                Base64.decode(food.imageUrl, Base64.DEFAULT)
                                    .let { bytes -> BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
                            }.getOrNull()

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF7F7F7))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (bmp != null) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = food.name,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = rememberAsyncImagePainter(food.imageUrl),
                                        contentDescription = food.name,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(food.name, style = MaterialTheme.typography.bodyLarge)
                                    Text("x${ci.quantity}", style = MaterialTheme.typography.bodySmall)
                                }
                                Text(
                                    "${fmt.format((food.price * ci.quantity).toLong())}₫",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = promoCode,
                    onValueChange = { promoCode = it },
                    label = { Text("Mã giảm giá (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Phương thức", style = MaterialTheme.typography.bodyLarge)
                    Text("${fmt.format(totalPrice)}₫", style = MaterialTheme.typography.bodyLarge)
                }

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
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tạm tính", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${fmt.format(subtotal)}₫",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Giảm giá", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "-${fmt.format(discountAmount)}₫",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Divider(Modifier.padding(vertical = 4.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tổng cộng", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${fmt.format(totalPrice)}₫",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
