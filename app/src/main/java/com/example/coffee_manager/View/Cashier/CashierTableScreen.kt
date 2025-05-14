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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
    onTableOccupied: (tableId: String, billId: String,tableNumber:Int) -> Unit
) {
    val TAG = "CashierTableScreen"
    val tableController = remember { TableController() }
    val scope = rememberCoroutineScope()

    var tables by remember { mutableStateOf<List<Table>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }

    // Load danh sách bàn
    LaunchedEffect(Unit) {
        loading = true
        tableController.getAllTables()
            .onSuccess { tables = it }
            .onFailure { message = it.message }
        loading = false
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
                        // Chọn màu theo trạng thái
                        val bg = when (table.status) {
                            Table.Status.EMPTY    -> Color(0xFFB8E986)
                            Table.Status.OCCUPIED -> Color(0xFFFFD966)
                            Table.Status.DAMAGED -> Color.Red
                        }
                        Card(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable {
                                    if (table.status == Table.Status.EMPTY) {
                                        Log.d(TAG, "Bấm bàn trống: idTable=${table.idTable}, number=${table.number}")
                                        onTableSelected(table.idTable)
                                        SessionManager.idTable = table.idTable
                                        SessionManager.numberTable = table.number

                                    } else {
                                        table.currentBillId?.let { billId ->
                                            Log.d(
                                                TAG,
                                                "Bấm bàn có khách: idTable=${table.idTable}, billId=$billId"
                                            )
                                            onTableOccupied(table.idTable, billId,table.number)
                                        } ?: run {
                                            Log.w(TAG, "Bàn OCCUPIED nhưng currentBillId == null (idTable=${table.idTable})")
                                        }
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = bg),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Bàn ${table.number}",
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
