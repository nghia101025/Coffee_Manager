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
import com.example.coffee_manager.Controller.Cashier.BillController
import com.example.coffee_manager.Model.Bill
import com.example.coffee_manager.Model.BillItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BillDetailScreen(navController: NavController, billId: String) {
    val controller = remember { BillController() }
    var bill by remember { mutableStateOf<Bill?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(billId) {
        isLoading = true
        controller.getBill(billId)
            .onSuccess { bill = it }
            .onFailure { error = it.message }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Hóa đơn ${bill?.idBill ?: "..."}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(it)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> Text(text = "Lỗi: $error", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                bill != null -> BillDetailContent(bill!!)
            }
        }
    }
}

@Composable
fun BillDetailContent(bill: Bill) {
    val sdf = SimpleDateFormat("dd 'thg' M, yyyy HH:mm", Locale.getDefault())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Table and date
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Bàn: ${bill.idTable}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Ngày tạo: ${sdf.format(Date(bill.createdAt))}", style = MaterialTheme.typography.bodyMedium)
            }
            // Status chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (bill.isProcessed) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Đã xử lý") }
                    )
                }
                if (bill.isPaid) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Đã thanh toán") }
                    )
                }
            }
        }

        Divider()

        // Items list
        Text(text = "Mặt hàng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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

        // Notes
        if (bill.note.isNotBlank()) {
            Text(text = "Ghi chú", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = bill.note, style = MaterialTheme.typography.bodyMedium)
        }

        Divider()

        // Discount and total
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
            Text(text = "Tổng cộng", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = "%.0fđ".format(bill.totalPrice.toDouble()), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
        Text(text = "x ${item.quantity}   ${item.price}đ", style = MaterialTheme.typography.bodyLarge)
    }
}
