// View/Order/PaymentScreen.kt
package com.example.coffee_manager.View.Order

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.coffee_manager.Controller.Admin.QrController
import com.example.coffee_manager.Controller.Order.*
import com.example.coffee_manager.Model.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    billController: BillController = remember { BillController() },
    tableController: TableController = remember { TableController() },
    foodController: FoodController = remember { FoodController() },
    promotionController: PromotionController = remember { PromotionController() },
    qrController: QrController = remember { QrController() }
) {
    val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    var cartItems by remember { mutableStateOf(emptyList<CartItem>()) }
    var foodsMap by remember { mutableStateOf<Map<String, Food>>(emptyMap()) }
    var promoInput by remember { mutableStateOf("") }
    var discountPct by remember { mutableStateOf(0) }
    var selectedMethod by remember { mutableStateOf("Tiền mặt") }
    var receiptBase64 by remember { mutableStateOf<String?>(null) }
    var qrBase64 by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }
    var showTransferDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Launcher chụp biên nhận
    val cameraLauncher = rememberLauncherForActivityResult(
        TakePicturePreview()
    ) { bmp ->
        bmp?.let {
            ByteArrayOutputStream().apply {
                it.compress(Bitmap.CompressFormat.JPEG, 80, this)
                receiptBase64 = Base64.encodeToString(toByteArray(), Base64.NO_WRAP)
                message = "Đã chụp biên nhận"
            }
        }
    }

    // 1) Load giỏ + món + QR
    LaunchedEffect(Unit) {
        loading = true
        billController.getCart()
            .onSuccess { cis ->
                cartItems = cis
                foodsMap = cis.map { it.foodId }.distinct()
                    .mapNotNull { id -> foodController.getFoodById(id).getOrNull() }
                    .associateBy { it.idFood }
            }
            .onFailure { message = it.message }
        qrController.fetchQrBase64()
            .onSuccess { qrBase64 = it }
            .onFailure { message = "Không tải được QR" }
        loading = false
    }

    // 2) Tính tiền
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
                return@Box
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Chọn bàn
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

                // Danh sách món
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cartItems) { ci ->
                        val food = foodsMap[ci.foodId]
                        if (food != null) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF0F0F0))
                                    .padding(8.dp),
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
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(food.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "${ci.quantity} x ${
                                            NumberFormat.getNumberInstance(Locale("vi", "VN")).format(food.price)
                                        }₫",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                val lineTotal = food.price * ci.quantity
                                Text(
                                    "${NumberFormat.getNumberInstance(Locale("vi", "VN")).format(lineTotal)}₫",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Mã khuyến mãi
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = promoInput,
                        onValueChange = { promoInput = it },
                        label = { Text("Mã khuyến mãi") },
                        modifier = Modifier.weight(0.7f)
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                promotionController.getByCode(promoInput.trim())
                                    .onSuccess { promo ->
                                        if (promo.expiresAt.time < System.currentTimeMillis()) {
                                            message = "Mã đã hết hạn"
                                        } else {
                                            discountPct = promo.discountPercent
                                            message = "Áp dụng −$discountPct%"
                                        }
                                    }
                                    .onFailure { message = "Mã không hợp lệ" }
                            }
                        },
                        modifier = Modifier.weight(0.3f).height(65.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Áp dụng")
                    }
                }

                // Phương thức
                Text("Phương thức thanh toán", fontWeight = FontWeight.Medium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf("Tiền mặt" to Icons.Default.LocalAtm, "Chuyển khoản" to Icons.Default.CreditCard)
                        .forEach { (method, icon) ->
                            ElevatedCard(
                                onClick = { selectedMethod = method },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedMethod == method)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    else Color.Transparent
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(icon, contentDescription = method)
                                    Spacer(Modifier.width(8.dp))
                                    Text(method)
                                }
                            }
                        }
                }

                // Nút Lên đơn
                Spacer(Modifier.weight(1f))
                SummaryRow("Tạm tính", subtotal, fmt)
                if (discountPct > 0) SummaryRow("Giảm $discountPct%", discountAmount, fmt)
                Divider(Modifier.padding(vertical = 8.dp))
                SummaryRow("Tổng cộng", total, fmt, isTotal = true)

                Button(
                    onClick = {
                        // Kiểm tra bàn
                        if (SessionManager.idTable.isNullOrBlank()) {
                            message = "Vui lòng chọn bàn"
                        } else if (selectedMethod == "Chuyển khoản") {
                            // show dialog chuyển khoản
                            showTransferDialog = true
                        } else {
                            // xử lý tiền mặt
                            scope.launch {
                                val items = cartItems.map { ci ->
                                    val f = foodsMap.getValue(ci.foodId)
                                    BillItem(ci.foodId, f.name, f.price, ci.quantity)
                                }
                                val bill = Bill(
                                    idBill = "",
                                    idTable = SessionManager.idTable!!,
                                    items = items,
                                    note = "Tiền mặt",
                                    discountPercent = discountPct,
                                    totalPrice = total,
                                    paid = true,
                                    processed = true,
                                    createdAt = System.currentTimeMillis()
                                )
                                billController.createBill(bill)
                                    .onSuccess { id ->
                                        tableController.updateTableStatus(SessionManager.idTable!!, "OCCUPIED", id)
                                        SessionManager.numberTable = null
                                        billController.clearCart()
                                        navController.navigate("orderSuccess/$id") {
                                            popUpTo("payment") { inclusive = true }
                                        }
                                    }
                                    .onFailure { message = "Tạo đơn thất bại: ${it.message}" }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Lên đơn • ${fmt.format(total)}₫")
                }

                // Snackbar
                LaunchedEffect(message) {
                    message?.let {
                        snackbarHostState.showSnackbar(it)
                        message = null
                    }
                }
            }
        }

        // Dialog chuyển khoản
        if (showTransferDialog) {
            AlertDialog(
                onDismissRequest = { showTransferDialog = false },
                title = { Text("Thanh toán chuyển khoản") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        qrBase64?.let { b64 ->
                            val data = Base64.decode(b64, Base64.DEFAULT)
                            Image(
                                BitmapFactory.decodeByteArray(data, 0, data.size).asImageBitmap(),
                                contentDescription = "QR chuyển khoản",
                                modifier = Modifier.size(200.dp),
                                contentScale = ContentScale.Crop
                            )
                        } ?: Text("Đang tải QR...", Modifier.padding(16.dp))

                        Spacer(Modifier.height(16.dp))

                        receiptBase64?.let { b64 ->
                            val data = Base64.decode(b64, Base64.DEFAULT)
                            Image(
                                BitmapFactory.decodeByteArray(data, 0, data.size).asImageBitmap(),
                                contentDescription = "Biên nhận",
                                modifier = Modifier.size(120.dp),
                                contentScale = ContentScale.Crop
                            )
                        } ?: Text("Chưa có biên nhận", color = Color.Gray)

                        Spacer(Modifier.height(8.dp))

                        Button(onClick = { cameraLauncher.launch(null) }) {
                            Text(if (receiptBase64 == null) "Chụp biên nhận" else "Chụp lại")
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = receiptBase64 != null,
                        onClick = {
                            scope.launch {
                                val items = cartItems.map { ci ->
                                    val f = foodsMap.getValue(ci.foodId)
                                    BillItem(ci.foodId, f.name, f.price, ci.quantity)
                                }
                                val bill = Bill(
                                    idBill = "",
                                    idTable = SessionManager.idTable!!,
                                    items = items,
                                    note = "Chuyển khoản",
                                    discountPercent = discountPct,
                                    totalPrice = total,
                                    paid = true,
                                    processed = false,
                                    createdAt = System.currentTimeMillis(),
                                    receiptImage = receiptBase64!!
                                )
                                billController.createBill(bill)
                                    .onSuccess { id ->
                                        tableController.updateTableStatus(SessionManager.idTable!!, "OCCUPIED", id)
                                        SessionManager.numberTable = null
                                        billController.clearCart()
                                        showTransferDialog = false
                                        navController.navigate("orderSuccess/$id") {
                                            popUpTo("payment") { inclusive = true }
                                        }
                                    }
                                    .onFailure { message = "Tạo đơn thất bại: ${it.message}" }
                            }
                        }
                    ) { Text("Xác nhận thanh toán") }
                },
                dismissButton = {
                    TextButton(onClick = { showTransferDialog = false }) { Text("Hủy") }
                }
            )
        }
    }
}

// Dòng tóm tắt
@Composable
private fun SummaryRow(label: String, amount: Long, fmt: NumberFormat, isTotal: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (isTotal) FontWeight.SemiBold else FontWeight.Normal)
        Text(
            fmt.format(amount) + "₫",
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = if (isTotal) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
    }
}
