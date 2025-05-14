package com.example.coffee_manager.View.Order

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.coffee_manager.Controller.Order.TableController
import com.example.coffee_manager.Model.SessionManager
import com.example.coffee_manager.Model.Table
import com.example.coffee_manager.View.CommonTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableSelectionScreen(navController: NavController) {
    val controller = remember { TableController() }
    var tables by remember { mutableStateOf<List<Table>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        controller.getAllTables()
            .onSuccess { tables = it }
            .onFailure { /* show error */ }
    }

    Scaffold(
        topBar = { CommonTopBar(navController, title = "Chọn bàn") }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding)
        ) {
            items(tables) { table ->
                val color = when(table.status) {
                    Table.Status.EMPTY    -> Color(0xFF66FF99)
                    Table.Status.OCCUPIED -> Color(0xFFFF9900)
                    Table.Status.DAMAGED  -> Color.Red
                }
                Card(
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth()
                        .clickable {
                            if (table.status == Table.Status.EMPTY) {
                                SessionManager.idTable = table.idTable
                                SessionManager.numberTable = table.number
                                navController.popBackStack()  // quay lại PaymentScreen
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = color)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("Bàn ${table.number}", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

