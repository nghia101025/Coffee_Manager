package com.example.coffee_manager.View.Manager.Food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Admin.CategoryController
import com.example.coffee_manager.Model.Category
import com.example.coffee_manager.View.CommonTopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    navController: NavController
) {
    val controller = remember { CategoryController() }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        val result: Result<List<Category>> = controller.getAllCategories()
        result
            .onSuccess { categories = it }
            .onFailure { message = it.message }
        isLoading = false
    }
    Scaffold(
    ) { padding ->
        Box(Modifier.padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                CommonTopBar(navController = navController, title = "Quản lý danh mục")
                ListCategory(
                    categories = categories,
                    onFilterCategory = { /* TODO: lọc theo danh mục nếu cần */ },
                    onAddCategory = { name ->
                        coroutineScope.launch {
                            controller.addCategory(name)
                                .onSuccess {
                                    controller.getAllCategories()
                                        .onSuccess { categories = it }
                                        .onFailure { message = it.message }
                                }
                                .onFailure { message = it.message }
                        }
                    },
                    onDeleteCategory = { idCat ->
                        coroutineScope.launch {
                            controller.deleteCategory(idCat)
                                .onFailure { message = it.message }
                                .onSuccess {
                                    controller.getAllCategories()
                                        .onSuccess { categories = it }
                                        .onFailure { message = it.message }
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            message?.let {
                Snackbar(Modifier.align(Alignment.BottomCenter)) {
                    Text(it)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListCategory(
    categories: List<Category>,
    onFilterCategory: (String?) -> Unit,
    onAddCategory: (String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var toDeleteCategory by remember { mutableStateOf<Category?>(null) }

    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Spacer(Modifier.height(40.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            item {
                AssistChip(
                    onClick = { onFilterCategory(null) },
                    label = { Text("Tất cả") }
                )
            }
            items(categories) { cat ->
                AssistChip(
                    onClick = { toDeleteCategory = cat },
                    label = { Text(cat.name) }
                )
            }

            item {
                AssistChip(
                    onClick = {
                        showDialog = true
                        newName = ""
                        errorMsg = null
                    },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                    label = { Text("Thêm") }
                )
            }
        }

        // Dialog thêm danh mục
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Thêm danh mục mới") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = {
                                newName = it
                                errorMsg = null
                            },
                            label = { Text("Tên danh mục") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        errorMsg?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val name = newName.trim()
                        when {
                            name.isEmpty() -> errorMsg = "Tên không được để trống"
                            categories.any { it.name == name } -> errorMsg = "Danh mục đã tồn tại"
                            else -> {
                                onAddCategory(name)
                                showDialog = false
                            }
                        }
                    }) {
                        Text("Ok")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }

        // Dialog xác nhận xóa
        if (toDeleteCategory != null) {
            AlertDialog(
                onDismissRequest = { toDeleteCategory = null },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn có chắc chắn muốn xóa danh mục '${toDeleteCategory?.name}' không?") },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteCategory(toDeleteCategory!!.idCat)
                        toDeleteCategory = null
                    }) {
                        Text("Xóa", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { toDeleteCategory = null }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}
