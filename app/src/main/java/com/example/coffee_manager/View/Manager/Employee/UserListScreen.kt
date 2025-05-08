package com.example.coffee_manager.View.Manager.Employee

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
import com.example.coffee_manager.Controller.Admin.EmployeeController
import com.example.coffee_manager.Model.User
import com.example.coffee_manager.View.CommonTopBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(navController: NavController) {
    val employeeController = remember { EmployeeController() }
    var userList by remember { mutableStateOf<List<User>>(emptyList()) }
    var filteredList by remember { mutableStateOf<List<User>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }

    // State để hiển thị dialog xóa
    var showDeleteDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    // Load danh sách
    LaunchedEffect(Unit) {
        val result = withContext(Dispatchers.IO) {
            employeeController.getAllEmployee()
        }
        result
            .onSuccess {
                userList = it
                filteredList = it
            }
            .onFailure {
                message = "Không tải được danh sách nhân viên: ${it.message}"
            }
        isLoading = false
    }

    // Hàm xóa user
    fun deleteUser(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            employeeController.deleteEmployee(user.idUser)
                .onSuccess {
                    withContext(Dispatchers.Main) {
                        userList = userList.filter { it.idUser != user.idUser }
                        filteredList = filteredList.filter { it.idUser != user.idUser }
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
        topBar = { CommonTopBar(navController = navController, title = "Danh sách nhân viên") }
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
                    filteredList = userList.filter {
                        it.phone.contains(q, true) || it.email.contains(q, true)
                    }
                },
                label = { Text("Tìm theo SĐT hoặc Email") },
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
                    Text("Không tìm thấy nhân viên nào")
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredList) { user ->
                        UserRow(
                            user = user,
                            onEdit = { navController.navigate("update_employee/${user.idUser}") },
                            onDelete = {
                                userToDelete = user
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Dialog xác nhận xóa
        if (showDeleteDialog && userToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn có chắc chắn muốn xóa nhân viên \"${userToDelete!!.name}\" không?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        deleteUser(userToDelete!!)
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
fun UserRow(user: User, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Tên: ${user.name}", fontWeight = FontWeight.Bold)
                Text("Email: ${user.email}")
                Text("SĐT: ${user.phone}")
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
