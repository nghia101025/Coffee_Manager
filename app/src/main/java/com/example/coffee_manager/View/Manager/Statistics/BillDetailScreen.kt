// View/Statistics/BillDetailScreen.kt
package com.example.coffee_manager.View.Statistics

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Admin.TableController
import com.example.coffee_manager.Controller.Cashier.BillController
import com.example.coffee_manager.Model.Bill
import com.example.coffee_manager.Model.BillItem
import com.example.coffee_manager.Model.Table
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BillDetailScreen(navController: NavController, billId: String) {
    val billCtrl = remember { BillController() }
    val tableCtrl = remember { TableController() }

    var bill by remember { mutableStateOf<Bill?>(null) }
    var table by remember { mutableStateOf<Table?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // 1) Lấy hoá đơn
    LaunchedEffect(billId) {
        isLoading = true
        billCtrl.getBill(billId)
            .onSuccess { fetched ->
                bill = fetched
                // 2) Khi có bill, lấy bàn
                tableCtrl.getTableById(fetched.idTable)
                    .onSuccess { tbl -> table = tbl }
                    .onFailure { ex -> error = "Không tải được bàn: ${ex.message}" }
            }
            .onFailure { ex ->
                error = "Không tải được hoá đơn: ${ex.message}"
            }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Hoá đơn ${bill?.idBill ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                bill != null && table != null -> {
                    BillDetailContent(bill = bill!!, table = table!!)
                }
                else -> {
                    Text(
                        text = "Không tìm thấy dữ liệu",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun BillDetailContent(bill: Bill, table: Table) {
    val sdf = SimpleDateFormat("dd 'thg' M, yyyy HH:mm", Locale.getDefault())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header: bàn, thời gian, trạng thái
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bàn: ${table.number}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Ngày tạo: ${sdf.format(Date(bill.createdAt))}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (bill.processed) {
                    AssistChip(onClick = {}, label = { Text("Đã xử lý") })
                }
                if (bill.paid) {
                    AssistChip(onClick = {}, label = { Text("Đã thanh toán") })
                }
            }
        }
        Divider()

        // Danh sách mặt hàng
        Text(
            text = "Mặt hàng",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bill.items) { item ->
                BillItemRow(item)
            }
        }

        Divider()

        // Ghi chú (nếu có)
        if (bill.note.isNotBlank()) {
            Text(
                text = "Ghi chú",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = bill.note, style = MaterialTheme.typography.bodyMedium)
            Divider()
        }

        // Tổng tiền
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Giảm giá", style = MaterialTheme.typography.bodyLarge)
            Text(text = "−${bill.discountPercent}%", style = MaterialTheme.typography.bodyLarge)
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tổng cộng",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${bill.totalPrice}₫",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BillItemRow(item: BillItem) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = item.name, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = "x${item.quantity}   ${item.price}₫",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
