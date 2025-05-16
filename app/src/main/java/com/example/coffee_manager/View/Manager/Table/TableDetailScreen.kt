package com.example.coffee_manager.View.Manager.Table

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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

    // 1. Load thông tin bàn
    LaunchedEffect(tableId) {
        controller.getTableById(tableId)
            .onSuccess { table = it }
            .onFailure {
                message = "Không tải được thông tin"
                showMsg = true
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết bàn #${table?.number ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa bàn", tint = Color.Red)
                    }
                }
            )
        }
    ) { padding ->
        table?.let { tbl ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Thông tin cơ bản
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Bàn số ${tbl.number}", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Trạng thái: ${
                                when (tbl.status) {
                                    Table.Status.EMPTY    -> "Trống"
                                    Table.Status.OCCUPIED -> "Có khách"
                                    Table.Status.DAMAGED  -> "Hỏng"
                                }
                            }",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Hoá đơn hiện tại: ${tbl.currentBillId ?: "–"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Nút chức năng theo trạng thái
                when (tbl.status) {
                    Table.Status.EMPTY -> {
                        Button(
                            onClick = {
                                scope.launch {
                                    controller.updateTableStatusByNumber(
                                        tbl.number,
                                        Table.Status.DAMAGED
                                    ).onSuccess {
                                        message = "Đã báo bàn #${tbl.number} hỏng"
                                        showMsg = true
                                        navController.navigate("table_list")

                                    }.onFailure {
                                        message = it.message ?: "Lỗi"
                                        showMsg = true
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Build, contentDescription = "Báo hỏng")
                            Spacer(Modifier.width(8.dp))
                            Text("Báo hỏng bàn")
                        }
                    }
                    Table.Status.DAMAGED -> {
                        Button(
                            onClick = {
                                scope.launch {
                                    controller.updateTableStatusByNumber(
                                        tbl.number,
                                        Table.Status.EMPTY
                                    ).onSuccess {
                                        message = "Đã hoàn tất sửa bàn #${tbl.number}"
                                        showMsg = true
                                        navController.navigate("table_list")
                                    }.onFailure {
                                        message = it.message ?: "Lỗi"
                                        showMsg = true
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Fastfood, contentDescription = "Hoàn tất sửa")
                            Spacer(Modifier.width(8.dp))
                            Text("Hoàn tất sửa bàn")
                        }
                    }
                    Table.Status.OCCUPIED -> {
                        // không hiển thị nút nào
                    }
                }

                // Hiện popup thông báo
                PopupMessage(show = showMsg, message = message) { showMsg = false }

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
                            }) {
                                Text("Xóa", color = Color.Red)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Hủy")
                            }
                        }
                    )
                }
            }
        }
    }
}
