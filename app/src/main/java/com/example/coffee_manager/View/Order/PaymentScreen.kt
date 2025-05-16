// View/Order/EnhancedPaymentScreen.kt
package com.example.coffee_manager.View.Order

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Order.PromotionController
import com.example.coffee_manager.Controller.Order.BillController
import com.example.coffee_manager.Controller.Order.FoodController
import com.example.coffee_manager.Controller.Order.TableController
import com.example.coffee_manager.Model.Bill
import com.example.coffee_manager.Model.BillItem
import com.example.coffee_manager.Model.CartItem
import com.example.coffee_manager.Model.Food
import com.example.coffee_manager.Model.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    billController: BillController = remember { BillController() },
    tableController: TableController = remember { TableController() },
    foodController: FoodController = remember { FoodController() },
    promotionController: PromotionController= remember { PromotionController() }

) {
    val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var foodsMap by remember { mutableStateOf<Map<String, Food>>(emptyMap()) }
    var promoInput by remember { mutableStateOf("") }
    var discountPct by remember { mutableStateOf(0) }
    var selectedMethod by remember { mutableStateOf("Tiền mặt") }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Load cart + foods
    LaunchedEffect(Unit) {
        loading = true
        billController.getCart()
            .onSuccess { cis ->
                cartItems = cis
                val foods = cis.map { it.foodId }.distinct()
                    .mapNotNull { id -> foodController.getFoodById(id).getOrNull() }
                foodsMap = foods.associateBy { it.idFood }
            }
            .onFailure { message = it.message }
        loading = false
    }

    // Compute totals
    val subtotal = cartItems.sumOf { ci -> (foodsMap[ci.foodId]?.price ?: 0L) * ci.quantity }
    val discountAmount = subtotal * discountPct / 100
    val total = subtotal - discountAmount

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Xác nhận thanh toán") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Receipt, contentDescription = "Quay lại")
                    }
                }
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Table selector
            Button(
                onClick = { navController.navigate("table_select") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = SessionManager.numberTable?.let { "Bàn $it" } ?: "Chọn bàn",
                    fontWeight = FontWeight.Medium
                )
            }

            // Cart items
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems) { ci ->
                        val f = foodsMap[ci.foodId] ?: return@items
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val bmp = runCatching {
                                Base64.decode(f.imageUrl, Base64.DEFAULT).let { bytes ->
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                }
                            }.getOrNull()
                            if (bmp != null) {
                                Image(
                                    bmp.asImageBitmap(),
                                    contentDescription = f.name,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    Modifier
                                        .size(56.dp)
                                        .background(Color.LightGray, RoundedCornerShape(8.dp))
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(f.name, fontWeight = FontWeight.SemiBold)
                                Text("x${ci.quantity}", color = Color.Gray)
                            }
                            Text(fmt.format(f.price * ci.quantity) + "₫")
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = promoInput,
                    onValueChange = { promoInput = it },
                    label = { Text("Nhập mã khuyến mãi") },
                    singleLine = true,
                    modifier = Modifier.weight(0.7f) // chiếm 70%
                )
                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            // 1. Lấy promotion theo code
                            promotionController.getByCode(promoInput.trim())
                                .onSuccess { promo ->
                                    val now = System.currentTimeMillis()
                                    if (promo.expiresAt.time < now) {
                                        message = "Mã đã hết hạn"
                                    } else {
                                        discountPct = promo.discountPercent
                                        message = "Áp dụng thành công: −$discountPct%"
                                    }
                                }
                                .onFailure {
                                    message = "Mã không tồn tại"
                                }
                            loading = false
                        }
                    },
                    modifier = Modifier
                        .weight(0.3f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Áp dụng")
                }
            }

            // Payment method
            Text("Phương thức thanh toán", fontWeight = FontWeight.Medium)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf("Tiền mặt" to Icons.Default.LocalAtm, "Chuyển khoản" to Icons.Default.CreditCard).forEach { (method, icon) ->
                    ElevatedCard(
                        onClick = { selectedMethod = method },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedMethod == method)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, contentDescription = method)
                            Spacer(Modifier.width(8.dp))
                            Text(method)
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Summary and confirm
            Column(Modifier.fillMaxWidth()) {
                SummaryRow("Tạm tính", subtotal, fmt)
                if (discountPct > 0) SummaryRow("Giảm $discountPct%", discountAmount, fmt)
                Divider(Modifier.padding(vertical = 8.dp))
                SummaryRow("Tổng cộng", total, fmt, isTotal = true)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            // 1. Kiểm tra đã chọn bàn?
                            val idTable = SessionManager.idTable
                            if (idTable.isBlank()) {
                                message = "Vui lòng chọn bàn"
                                loading = false
                                return@launch
                            }
                            // 2. Xây bill items
                            val items = cartItems.map { ci ->
                                val f = foodsMap.getValue(ci.foodId)
                                BillItem(ci.foodId, f.name, f.price, ci.quantity)
                            }
                            // 3. Tạo Bill
                            val bill = Bill(
                                idBill = "",
                                idTable = idTable,
                                items = items,
                                note = selectedMethod,
                                discountPercent = discountPct,
                                totalPrice = total,
                                paid = true,
                                processed = false
                            )
                            // 4. Ghi lên Firestore
                            billController.createBill(bill)
                                .onSuccess { newBillId ->
                                    // 5. Đánh dấu bàn đã OCCUPIED
                                    tableController.updateTableStatus(idTable, "OCCUPIED", newBillId)
                                    // 6. Tăng soldCount
                                    items.forEach { bi ->
                                        launch {
                                            foodController.incrementSoldCount(bi.foodId, bi.quantity)
                                        }
                                    }
                                    // 7. Xóa cart
                                    billController.clearCart()
                                    // 8. Điều hướng
                                    navController.navigate("orderSuccess/$newBillId") {
                                        popUpTo("payment") { inclusive = true }
                                    }
                                }
                                .onFailure {
                                    message = "Đặt hàng thất bại: ${it.message}"
                                }
                            loading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    enabled = !loading
                ) {
                    Text("Lên đơn • ${fmt.format(total)}₫")
                }
            }

            message?.let {
                Snackbar { Text(it) }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: Long, fmt: NumberFormat, isTotal: Boolean = false) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (isTotal) FontWeight.SemiBold else FontWeight.Normal)
        Text(
            fmt.format(amount) + "₫",
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = if (isTotal) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
    }
}
