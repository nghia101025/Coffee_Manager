// View/Brewing/BrewingScreen.kt
package com.example.coffee_manager.View.Brewing

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import com.example.coffee_manager.Model.Table
import com.example.coffee_manager.R
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrewingScreen(
    navController: NavController,
    billController: BillController = remember { BillController() },
    tableController: TableController = remember { TableController() }
) {
    var entries by remember { mutableStateOf<List<Pair<Bill, Table?>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // 1) Load all bills where isPaid=false AND isProcessed=false
    LaunchedEffect(Unit) {
        isLoading = true
        billController.getAllBills()
            .onSuccess { all ->
                val toMake = all.filter {!it.processed }
                val pairs = toMake.map { bill ->
                    async {
                        val tbl = tableController.getTableById(bill.idTable)
                            .getOrNull()
                        bill to tbl
                    }
                }.awaitAll()
                entries = pairs
            }
            .onFailure {
                errorMsg = "Lỗi tải hoá đơn: ${it.message}"
            }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh sách pha chế") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_account),
                            contentDescription = "User Profile",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Tìm kiếm ID hoá đơn hoặc số bàn") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
                errorMsg != null -> {
                    Box(Modifier.fillMaxSize()) {
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                else -> {
                    // 2) Filter theo searchQuery
                    val filtered = entries.filter { (bill, tbl) ->
                        val matchId = bill.idBill.contains(searchQuery, ignoreCase = true)
                        val matchTable = tbl?.number?.toString()?.contains(searchQuery, ignoreCase = true) ?: false
                        matchId || matchTable
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filtered) { (bill, tbl) ->
                            BrewingBillItem(
                                bill = bill,
                                tableNumber = tbl?.number ?: -1,
                                onClick = {
                                    navController.navigate("brewing_detail/${bill.idBill}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrewingBillItem(
    bill: Bill,
    tableNumber: Int,
    onClick: () -> Unit          // <-- thêm tham số onClick
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),   // <-- gắn clickable
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = "Hoá đơn: ${bill.idBill}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Bàn số: ${if (tableNumber >= 0) tableNumber else "?"}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Tổng: ${bill.totalPrice}₫", style = MaterialTheme.typography.bodyMedium)
            if (bill.note.isNotBlank()) {
                Text(text = "Ghi chú: ${bill.note}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
