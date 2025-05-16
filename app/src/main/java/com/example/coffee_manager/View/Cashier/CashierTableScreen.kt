// View/Cashier/CashierTableScreen.kt
package com.example.coffee_manager.View.Cashier

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Snackbar
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Cashier.TableController
import com.example.coffee_manager.Model.SessionManager
import com.example.coffee_manager.Model.Table
import com.example.coffee_manager.R
import com.example.coffee_manager.View.CommonTopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CashierTableScreen(
    navController: NavController,
    onTableSelected: (tableId: String) -> Unit,
    onTableOccupied: (tableId: String, billId: String, tableNumber: Int) -> Unit
) {
    val TAG = "CashierTableScreen"
    val tableController = remember { TableController() }
    val scope = rememberCoroutineScope()

    var tables by remember { mutableStateOf<List<Table>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }

    // Hàm để load data
    fun loadTables() {
        scope.launch {
            loading = true
            tableController.getAllTables()
                .onSuccess { tables = it }
                .onFailure { message = it.message }
            loading = false
        }
    }

    // Load lần đầu
    LaunchedEffect(Unit) {
        loadTables()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thu ngân") },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { loadTables() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Tải lại danh sách bàn"
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(tables, key = { it.idTable }) { table ->
                        val bg = when (table.status) {
                            Table.Status.EMPTY    -> Color(0xFFB8E986)
                            Table.Status.OCCUPIED -> Color(0xFFFFD966)
                            Table.Status.DAMAGED  -> Color.Red
                        }
                        Card(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable {
                                    if (table.status == Table.Status.EMPTY) {
                                        onTableSelected(table.idTable)
                                        SessionManager.idTable = table.idTable
                                        SessionManager.numberTable = table.number
                                    } else {
                                        table.currentBillId?.let { billId ->
                                            onTableOccupied(table.idTable, billId, table.number)
                                        }
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = bg),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "Bàn ${table.number}",
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            message?.let { msg ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) { Text(msg) }
            }
        }
    }
}
