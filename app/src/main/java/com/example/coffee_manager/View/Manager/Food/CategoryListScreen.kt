// View/Manager/Food/CategoryListScreen.kt
package com.example.coffee_manager.View.Manager.Food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.layout.FlowRow
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
import com.example.coffee_manager.View.PopupMessage
import com.example.coffee_manager.utils.MaterialIconMap
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(navController: NavController) {
    val controller = remember { CategoryController() }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }

    // State dialogs
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var toDelete by remember { mutableStateOf<Category?>(null) }

    val scope = rememberCoroutineScope()

    // Load categories once
    LaunchedEffect(Unit) {
        controller.getAllCategories()
            .onSuccess { categories = it }
            .onFailure { message = "Không tải được danh mục: ${it.message}" }
        isLoading = false
    }

    Scaffold(
        topBar = { CommonTopBar(navController = navController, title = "Quản lý danh mục") }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Hiển thị chip cho từng danh mục
                        categories.forEach { cat ->
                            AssistChip(
                                onClick = { toDelete = cat },
                                label = { Text(cat.name) }
                            )
                        }
                        // Chip Thêm
                        AssistChip(
                            onClick = { showAddDialog = true },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                            label = { Text("Thêm") }
                        )
                    }

                    // Thông báo
                    message?.let {
                        PopupMessage(show = true, message = it) { message = null }
                    }

                    // Dialog: thêm danh mục
                    if (showAddDialog) {
                        AlertDialog(
                            onDismissRequest = { showAddDialog = false; errorMsg = null },
                            title = { Text("Thêm danh mục") },
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
                                    errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    val name = newName.trim()
                                    when {
                                        name.isBlank() -> errorMsg = "Tên không được để trống"
                                        categories.any { it.name == name }
                                            -> errorMsg = "Danh mục đã tồn tại"
                                        else -> {
                                            scope.launch {
                                                controller.addCategory(name, "")
                                                    .onSuccess {
                                                        // reload
                                                        controller.getAllCategories()
                                                            .onSuccess { categories = it }
                                                            .onFailure { message = it.message }
                                                        message = "Thêm thành công"
                                                    }
                                                    .onFailure { message = it.message }
                                            }
                                            showAddDialog = false
                                        }
                                    }
                                }) { Text("Xác nhận") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showAddDialog = false }) {
                                    Text("Hủy")
                                }
                            }
                        )
                    }

                    // Dialog: xóa danh mục
                    if (toDelete != null) {
                        AlertDialog(
                            onDismissRequest = { toDelete = null },
                            title = { Text("Xác nhận xóa") },
                            text = { Text("Bạn có chắc chắn muốn xóa “${toDelete!!.name}”?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    val cat = toDelete!!
                                    scope.launch {
                                        runCatching {
                                            controller.deleteCategory(cat.idCat)
                                        }.onSuccess {
                                            // reload sau khi xóa thành công
                                            controller.getAllCategories()
                                                .onSuccess { categories = it }
                                                .onFailure { message = it.message }
                                            message = "Đã xóa “${cat.name}”"
                                        }.onFailure { e ->
                                            message = "Xóa thất bại: ${e.message}"
                                        }
                                    }
                                    toDelete = null
                                }) {
                                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { toDelete = null }) {
                                    Text("Hủy")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
