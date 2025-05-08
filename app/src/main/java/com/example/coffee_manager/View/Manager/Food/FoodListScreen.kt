package com.example.coffee_manager.View.Manager.Food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Admin.FoodController
import com.example.coffee_manager.Model.Food
import com.example.coffee_manager.View.CommonTopBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodListScreen(navController: NavController) {
    val adminController = remember { FoodController() }
    var foodList by remember { mutableStateOf<List<Food>>(emptyList()) }
    var filteredList by remember { mutableStateOf<List<Food>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }

    // State cho dialog xóa
    var showDeleteDialog by remember { mutableStateOf(false) }
    var foodToDelete by remember { mutableStateOf<Food?>(null) }

    LaunchedEffect(Unit) {
        val result = withContext(Dispatchers.IO) { adminController.getAllFoods() }
        result
            .onSuccess {
                foodList = it
                filteredList = it
            }
            .onFailure { message = "Không tải được danh sách món ăn: ${it.message}" }
        isLoading = false
    }

    // Hàm xóa món ăn
    fun delete(food: Food) {
        CoroutineScope(Dispatchers.IO).launch {
            adminController.deleteFood(food.idFood)
                .onSuccess {
                    // cập nhật lại UI thread
                    withContext(Dispatchers.Main) {
                        foodList = foodList.filter { it.idFood != food.idFood }
                        filteredList = filteredList.filter { it.idFood != food.idFood }
                    }
                }
                .onFailure {
                    withContext(Dispatchers.Main) {
                        message = "Xóa thất bại: ${it.message}"
                    }
                }
        }
    }

    Scaffold(
        topBar = { CommonTopBar(navController = navController, title = "Danh sách món ăn") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { q ->
                    searchQuery = q
                    filteredList = foodList.filter {
                        it.idFood.toString().contains(q, true) ||
                                it.name.contains(q, true)
                    }
                },
                label = { Text("Tìm theo ID hoặc Tên món") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                message != null -> Text(
                    text = message!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                filteredList.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không tìm thấy món ăn nào")
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredList) { food ->
                        FoodRow(
                            food = food,
                            onEdit = { navController.navigate("update_food/${food.idFood}") },
                            onDelete = {
                                foodToDelete = food
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Dialog xác nhận xóa
        if (showDeleteDialog && foodToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn có chắc chắn muốn xóa món \"${foodToDelete!!.name}\" không?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        delete(foodToDelete!!)
                    }) {
                        Text("OK")
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

@Composable
fun FoodRow(
    food: Food,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("ID: ${food.idFood}", fontWeight = FontWeight.Bold)
                Text("Tên: ${food.name}")
                Text("Giá: ${food.price}")
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Chỉnh sửa")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color.Red)
                }
            }
        }
    }
}
