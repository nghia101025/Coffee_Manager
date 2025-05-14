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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.coffee_manager.Controller.Order.FoodController
import com.example.coffee_manager.Controller.Order.TableController
import com.example.coffee_manager.Controller.Order.BillController
import com.example.coffee_manager.Model.Bill
import com.example.coffee_manager.Model.BillItem
import com.example.coffee_manager.Model.CartItem
import com.example.coffee_manager.Model.Food
import com.example.coffee_manager.Model.SessionManager
import com.example.coffee_manager.View.CommonTopBar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

private const val TAG = "PaymentScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    billController: BillController = BillController(),
    tableController: TableController = TableController(),
    foodController: FoodController = FoodController()
) {
    val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))

    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var foodsMap  by remember { mutableStateOf<Map<String, Food>>(emptyMap()) }
    var promoPct  by remember { mutableStateOf("") }
    var method    by remember { mutableStateOf("Tiền mặt") }
    var message   by remember { mutableStateOf<String?>(null) }
    var loading   by remember { mutableStateOf(true) }
    val scope     = rememberCoroutineScope()

    // 1) Load cart + food details
    LaunchedEffect(Unit) {
        loading = true
        billController.getCart()
            .onSuccess { cis ->
                cartItems = cis
                // fetch info của từng món
                val foods = cis.map { it.foodId }
                    .distinct()
                    .mapNotNull { id ->
                        foodController.getFoodById(id).getOrNull()
                    }
                foodsMap = foods.associateBy { it.idFood }
            }
            .onFailure {
                message = "Không tải được giỏ hàng: ${it.message}"
                Log.e(TAG, it.message ?: "", it)
            }
        loading = false
    }

    // 2) Tính toán subtotal, discount, total
    val subtotal = cartItems.sumOf { ci ->
        (foodsMap[ci.foodId]?.price ?: 0L) * ci.quantity
    }
    val discountPct    = promoPct.toIntOrNull() ?: 0
    val discountAmount = subtotal * discountPct / 100
    val totalPrice     = subtotal - discountAmount

    Scaffold(
        topBar = { CommonTopBar(navController, title = "Thanh toán") },
        bottomBar = {
            Box(Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        val idTable = SessionManager.idTable
                        if (idTable == "") {
                            message = "Vui lòng chọn bàn trước"
                            return@Button
                        }
                        scope.launch {
                            loading = true
                            // 3) Kiểm tra bàn EMPTY
                            val raw = runCatching {
                                tableController.getTableStatus(idTable)
                            }
                            val emptyOk = raw.fold(
                                onSuccess = { it.trim().equals("EMPTY", ignoreCase = true) },
                                onFailure = {
                                    message = "Lỗi kiểm tra bàn: ${it.message}"
                                    false
                                }
                            )
                            if (!emptyOk) {
                                message = "Bàn ${SessionManager.numberTable} không trống"
                                loading = false
                                return@launch
                            }
                            // 4) Tạo Bill và ghi FS
                            val items = cartItems.map { ci ->
                                val f = foodsMap.getValue(ci.foodId)
                                BillItem(ci.foodId, f.name, f.price, ci.quantity)
                            }
                            val bill = Bill(
                                idBill = "",
                                idTable = idTable.toString(),
                                items = items,
                                note = method,
                                discountPercent = discountPct,
                                totalPrice = totalPrice,
                                isPaid = true,       // đã thanh toán
                                isProcessed = false
                            )
                            billController.createBill(bill).fold(onSuccess = { bid ->
                                // 5) Đánh dấu bàn OCCUPIED
                                tableController.updateTableStatus(idTable, "OCCUPIED", bid)
                                SessionManager.idTable = ""
                                SessionManager.numberTable = null

                                // 6) Tăng soldCount cho món
                                items.forEach { bi ->
                                    scope.launch {
                                        foodController.incrementSoldCount(bi.foodId, bi.quantity)
                                    }
                                }
                                // 7) Clear cart
                                billController.clearCart()
                                // 8) Điều hướng thành công
                                navController.navigate("orderSuccess/$bid") {
                                    popUpTo("payment") { inclusive = true }
                                }
                            }, onFailure = {
                                message = "Đặt hàng thất bại: ${it.message}"
                            })
                            loading = false
                        }
                    },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Lên đơn • ${fmt.format(totalPrice)}₫")
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
            message?.let {
                Snackbar(Modifier.align(Alignment.BottomCenter).padding(16.dp)) { Text(it) }
            }
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Button chọn bàn
                Button(onClick = { navController.navigate("table_select") }) {
                    Text(SessionManager.numberTable?.let { "Bàn $it" } ?: "Chọn bàn")
                }

                // Danh sách món
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                    LazyColumn(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(cartItems) { ci ->
                            val f = foodsMap[ci.foodId] ?: return@items
                            Row(Modifier.fillMaxWidth().background(Color(0xFFF7F7F7)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                // decode hoặc xài Coil
                                val bmp = runCatching {
                                    Base64.decode(f.imageUrl, Base64.DEFAULT).let { bytes ->
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    }
                                }.getOrNull()
                                if (bmp != null) {
                                    Image(bmp.asImageBitmap(), null, Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
                                } else {
                                    Image(rememberAsyncImagePainter(f.imageUrl), null, Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(f.name, style = MaterialTheme.typography.bodyLarge)
                                    Text("x${ci.quantity}", style = MaterialTheme.typography.bodySmall)
                                }
                                Text("${fmt.format(f.price * ci.quantity)}₫", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }

                // Giảm giá %
                OutlinedTextField(
                    value = promoPct,
                    onValueChange = { promoPct = it },
                    label = { Text("Giảm giá (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Chọn phương thức & hiển thị tổng
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Phương thức", style = MaterialTheme.typography.bodyLarge)
                    Text(fmt.format(totalPrice) + "₫", style = MaterialTheme.typography.bodyLarge)
                }

                // Radio chọn phương thức
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Tiền mặt", "Chuyển khoản").forEach { mtd ->
                            Row(Modifier.fillMaxWidth().clickable { method = mtd }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = method==mtd, onClick = { method = mtd })
                                Spacer(Modifier.width(8.dp))
                                Text(mtd, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
