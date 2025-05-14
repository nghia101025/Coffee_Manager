// View/Cashier/CashierBillScreen.kt
package com.example.coffee_manager.View.Cashier

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Cashier.BillController
import com.example.coffee_manager.Controller.Cashier.TableController
import com.example.coffee_manager.Model.Bill
import com.example.coffee_manager.Model.BillItem
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierBillScreen(
    navController: NavController,
    tableId: String,
    tableNumber: Int,
    billId: String,
    billController: BillController = remember { BillController() },
    tableController: TableController = remember { TableController() }
) {
    val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    var bill by remember { mutableStateOf<Bill?>(null) }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(billId) {
        loading = true
        billController.getBill(billId)
            .onSuccess { bill = it }
            .onFailure { message = "Lỗi tải hoá đơn: ${it.message}" }
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hoá đơn #$billId") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        bottomBar = {
            bill?.let { b ->
                if (!b.isPaid || !b.isProcessed) {
                    Button(
                        onClick = {
                            scope.launch {
                                loading = true
                                billController.checkoutBill(billId)
                                    .onFailure { message = "Lỗi hoàn tất: ${it.message}" }

                                tableController.clearTable(tableId)
                                    .onFailure { message = "Lỗi xoá bàn: ${it.message}" }

                                // refresh hoá đơn để cập nhật finish & dateFinish
                                billController.getBill(billId)
                                    .onSuccess { bill = it }

                                navController.navigate("finish_success/$billId") {
                                    popUpTo("cashier_bill/{tableId}/{billId}/{tableNumber}") { inclusive = true }
                                }

                                loading = false
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(48.dp)
                    ) {
                        Text("Hoàn tất")
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
                return@Box
            }
            message?.let { msg ->
                Snackbar(Modifier.align(Alignment.BottomCenter)) { Text(msg) }
            }
            bill?.let { b ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Bàn: $tableNumber", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Thời gian: " + Date(b.createdAt).toLocaleString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Divider()
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(b.items) { item ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${item.name} x${item.quantity}")
                                Text(fmt.format(item.price * item.quantity) + "₫")
                            }
                        }
                    }
                    Divider()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tạm tính")
                        Text(fmt.format(b.totalPrice) + "₫")
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Giảm ${b.discountPercent}%")
                        val disc = b.totalPrice * b.discountPercent / 100
                        Text("-${fmt.format(disc)}₫")
                    }
                    Divider()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tổng cộng", style = MaterialTheme.typography.titleMedium)
                        val pay = b.totalPrice - (b.totalPrice * b.discountPercent / 100)
                        Text(fmt.format(pay) + "₫", style = MaterialTheme.typography.titleMedium)
                    }
                    Text("Phương thức: ${b.note}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Paid: ${b.isPaid} • Processed: ${b.isProcessed}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } ?: run {
                Text(
                    "Không tìm thấy hoá đơn",
                    Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
