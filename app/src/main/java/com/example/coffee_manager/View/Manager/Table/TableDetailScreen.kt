package com.example.coffee_manager.View.Manager.Table

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Admin.TableController
import com.example.coffee_manager.Model.Table
import com.example.coffee_manager.View.CommonTopBar
import com.example.coffee_manager.View.PopupMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableDetailScreen(navController: NavController, tableId: String) {
    val controller = remember { TableController() }
    var table by remember { mutableStateOf<Table?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMsg by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Load thông tin
    LaunchedEffect(tableId) {
        controller.getTableById(tableId)
            .onSuccess { table = it }
            .onFailure { message = "Không tải được thông tin"; showMsg = true }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết Bàn #${table?.number ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Xoá bàn", tint = Color.Red)
                    }
                }
            )
        }
    ) { padding ->
        table?.let { tbl ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // CARD THÔNG TIN CHUNG
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Bàn số ${tbl.number}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = when (tbl.status) {
                                Table.Status.EMPTY    -> "Trạng thái: Trống"
                                Table.Status.OCCUPIED -> "Trạng thái: Có khách"
                                Table.Status.DAMAGED  -> "Trạng thái: Hỏng"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Hóa đơn: ${tbl.billId}" ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )

                    }

                    // NÚT CHỨC NĂNG
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            when (tbl.status) {
                                Table.Status.EMPTY -> {
                                    ExtendedFloatingActionButton(
                                        onClick = {
                                            message = "Gọi món cho bàn ${tbl.number}"
                                            showMsg = true
                                        },
                                        icon = { Icon(Icons.Default.Fastfood, contentDescription = null) },
                                        text = { Text("Gọi món") }
                                    )
                                    IconButton(onClick = {
                                        scope.launch {
                                            controller.updateTableStatusByNumber(tbl.number, Table.Status.DAMAGED)
                                                .onSuccess {
                                                    message = "Đã báo hỏng bàn"
                                                    showMsg = true
                                                }
                                                .onFailure {
                                                    message = it.message ?: "Lỗi"
                                                    showMsg = true
                                                }
                                        }
                                    }) {
                                        Icon(Icons.Default.Build, contentDescription = "Báo hỏng", tint = Color.Red)
                                    }
                                }
                                Table.Status.OCCUPIED -> {
                                    ExtendedFloatingActionButton(
                                        onClick = {
                                            message = "Thanh toán bàn ${tbl.number}"
                                            showMsg = true
                                        },
                                        icon = { Icon(Icons.Default.Payment, contentDescription = null) },
                                        text = { Text("Thanh toán") }
                                    )
                                    IconButton(onClick = {
                                        scope.launch {
                                            controller.updateTableStatusByNumber(tbl.number, Table.Status.DAMAGED)
                                                .onSuccess {
                                                    message = "Đã báo hỏng bàn"
                                                    showMsg = true
                                                }
                                                .onFailure {
                                                    message = it.message ?: "Lỗi"
                                                    showMsg = true
                                                }
                                        }
                                    }) {
                                        Icon(Icons.Default.Build, contentDescription = "Báo hỏng", tint = Color.Red)
                                    }
                                }
                                Table.Status.DAMAGED -> {
                                    ExtendedFloatingActionButton(
                                        onClick = {
                                            scope.launch {
                                                controller.updateTableStatusByNumber(tbl.number, Table.Status.EMPTY)
                                                    .onSuccess {
                                                        message = "Hoàn tất sửa chữa"
                                                        showMsg = true
                                                    }
                                                    .onFailure {
                                                        message = it.message ?: "Lỗi"
                                                        showMsg = true
                                                    }
                                            }
                                        },
                                        icon = { Icon(Icons.Default.Build, contentDescription = null) },
                                        text = { Text("Hoàn tất sửa") },
                                        containerColor = Color(0xFFFF9800)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Popup thông báo
        PopupMessage(show = showMsg, message = message, onDismiss = { showMsg = false })

        // Xác nhận xóa bàn
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn có chắc chắn muốn xóa bàn này?") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            controller.deleteTable(tableId)
                                .onSuccess { navController.popBackStack() }
                                .onFailure {
                                    message = "Lỗi xóa bàn: ${it.message}"
                                    showMsg = true
                                }
                        }
                        showDeleteDialog = false
                    }) { Text("Xóa", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Hủy") }
                }
            )
        }
    }
}

