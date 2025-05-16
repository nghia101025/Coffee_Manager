package com.example.coffee_manager.View.Manager.Space

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Admin.TableController
import com.example.coffee_manager.Model.Table
import com.example.coffee_manager.View.CommonTopBar
import com.example.coffee_manager.View.PopupMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableManagementScreen(navController: NavController) {
    val controller = remember { TableController() }
    var tables by remember { mutableStateOf<List<Table>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var newNumber by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    var showMsg by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Load tables from Firestore
    LaunchedEffect(Unit) {
        controller.getAllTables()
            .onSuccess { tables = it }
            .onFailure { Log.e("TableMgmt", "load fail", it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý không gian") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home_admin") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm bàn")
            }
        }
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tables) { table ->
                    TableBox(table = table, onClick = {
                        navController.navigate("table_detail/${table.idTable}")
                    })
                }
            }

            // Dialog thêm bàn mới
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false; errorMsg = null; newNumber = "" },
                    title = { Text("Thêm bàn mới") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newNumber,
                                onValueChange = { newNumber = it; errorMsg = null },
                                label = { Text("Số bàn") },
                                singleLine = true
                            )
                            errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val num = newNumber.toIntOrNull()
                            when {
                                num == null -> errorMsg = "Số không hợp lệ"
                                tables.any { it.number == num } -> errorMsg = "Số bàn đã tồn tại"
                                else -> {
                                    scope.launch {
                                        controller.addTable(
                                            Table(
                                                idTable = "",
                                                number = num,
                                                status = Table.Status.EMPTY,
                                                currentBillId = null
                                            )
                                        ).onSuccess {
                                            controller.getAllTables().onSuccess { tables = it }
                                            message = "Thêm bàn thành công"
                                            newNumber = ""
                                            showMsg = true
                                        }.onFailure {
                                            message = "Lỗi: ${it.message}"
                                            showMsg = true
                                        }
                                    }
                                    showDialog = false
                                }
                            }
                        }) {
                            Text("Thêm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Hủy")
                        }
                    }
                )
            }

            // Popup thông báo
            PopupMessage(show = showMsg, message = message, onDismiss = { showMsg = false })
        }
    }
}

@Composable
fun TableBox(table: Table, onClick: () -> Unit) {
    val color = when (table.status) {
        Table.Status.EMPTY -> Color(0xFF66FF99)  // Màu xanh lá nhạt
        Table.Status.OCCUPIED -> Color(0xFFFF9900)  // Màu cam
        Table.Status.DAMAGED -> Color.Red
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Bàn ${table.number}",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}