package com.example.coffee_manager.View.Brewing

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Brewing.BillController
import com.example.coffee_manager.Controller.Brewing.TableController
import com.example.coffee_manager.Model.Bill
import com.example.coffee_manager.Model.BillItem
import com.example.coffee_manager.R
import com.example.coffee_manager.View.CommonTopBar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrewingBillDetailScreen(
    navController: NavController,
    billId: String,
    billController: BillController = remember { BillController() },
    tableController: TableController = remember { TableController() }

) {
    var bill by remember { mutableStateOf<Bill?>(null) }
    var tableNumber by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch bill
    LaunchedEffect(billId) {
        isLoading = true

        // 1) Load bill
        billController.getBillById(billId)
            .onSuccess { loaded ->
                bill = loaded
                // 2) Sau khi có bill, load tiếp bàn
                loaded.idTable.let { tblId ->
                    tableController.getTableById(tblId)
                        .onSuccess { tbl ->
                            tableNumber = tbl.number
                        }
                        .onFailure {
                            error = "Không lấy được số bàn: ${it.message}"
                        }
                }
            }
            .onFailure {
                error = "Không tải được hoá đơn: ${it.message}"
            }

        isLoading = false
    }

    Scaffold(
        topBar = { CommonTopBar(navController, title = "Thanh toán") },
        ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> Text("Lỗi: $error", color = MaterialTheme.colorScheme.error)
                bill != null -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("ID hóa đơn: ${bill!!.idBill}", style = MaterialTheme.typography.titleMedium)
                        Text("Số bàn: ${tableNumber}")
                        Text("Ghi chú: ${bill!!.note}")

                        Divider()

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(bill!!.items) { item ->
                                BillItemRow(item)
                            }
                        }

                        Divider()

                        val formattedPrice = NumberFormat.getNumberInstance(Locale("vi", "VN")).format(bill!!.totalPrice)
                        Text("Tổng cộng: $formattedPrice₫", style = MaterialTheme.typography.titleLarge)

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    billController.updateBill(bill!!.copy(processed = true))
                                        .onSuccess {
                                            bill = bill!!.copy(processed = true)
                                            // Gửi thông báo hoặc toast nếu cần
                                        }
                                        .onFailure { error = it.message }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !bill!!.processed
                        ) {
                            Text(if (bill!!.processed) "Đã lên món" else "Lên món")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BillItemRow(item: BillItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("${item.name} x${item.quantity}")
        val formattedPrice = NumberFormat.getNumberInstance(Locale("vi", "VN")).format(item.price)
        Text("$formattedPrice₫", style = MaterialTheme.typography.titleLarge)    }
}
