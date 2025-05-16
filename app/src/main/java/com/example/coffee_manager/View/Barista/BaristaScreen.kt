package com.example.coffee_manager.View.Barista

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Barista.FoodController
import com.example.coffee_manager.Model.Food
import com.example.coffee_manager.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaristaScreen(
    navController: NavController,
    foodController: FoodController = remember { FoodController() }
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var allFoods by remember { mutableStateOf<List<Food>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    // 1) Load all foods
    LaunchedEffect(Unit) {
        foodController.getAllFoods()
            .onSuccess { allFoods = it }
            .onFailure {
                scope.launch {
                    snackbarHostState.showSnackbar("Không tải được món: ${it.message}")
                }
            }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pha chế - Báo hết món") },
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 2) Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Tìm theo tên món") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // 3) List
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val filtered = allFoods.filter {
                    it.name.contains(searchQuery.trim(), ignoreCase = true)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered) { food ->
                        FoodRow(
                            food = food,
                            onReportOut = {
                                scope.launch {
                                    foodController.updateAvailability(food.idFood, false)
                                        .onSuccess {
                                            // update locally
                                            allFoods = allFoods.map { f ->
                                                if (f.idFood == food.idFood) f.copy(available = false) else f
                                            }
                                            snackbarHostState.showSnackbar("Đã báo hết ${food.name}")
                                        }
                                        .onFailure {
                                            snackbarHostState.showSnackbar("Lỗi: ${it.message}")
                                        }
                                }
                            },
                            onReportIn = {
                                scope.launch {
                                    foodController.updateAvailability(food.idFood, true)
                                        .onSuccess {
                                            allFoods = allFoods.map { f ->
                                                if (f.idFood == food.idFood) f.copy(available = true) else f
                                            }
                                            snackbarHostState.showSnackbar("Đã báo có lại ${food.name}")
                                        }
                                        .onFailure {
                                            snackbarHostState.showSnackbar("Lỗi: ${it.message}")
                                        }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodRow(food: Food, onReportOut: () -> Unit, onReportIn: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        colors = CardDefaults.cardColors(
            containerColor = if (!food.available)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(food.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = if (food.available) "Còn hàng" else "Hết hàng",
                    color = if (food.available) Color.Green else Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (food.available) {
                // báo hết món
                IconButton(onClick = onReportOut) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Báo hết món"
                    )
                }
            } else {
                // báo có lại món
                IconButton(onClick = onReportIn) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Báo có lại món"
                    )
                }
            }
        }
    }
}

