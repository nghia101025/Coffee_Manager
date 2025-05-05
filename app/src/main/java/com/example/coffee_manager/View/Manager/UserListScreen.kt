package com.example.coffee_manager.View.Manager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.AdminController
import com.example.coffee_manager.Model.User
import com.example.coffee_manager.View.CommonTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun UserListScreen(navController: NavController) {
    val adminController = remember { AdminController() }
    var userList by remember { mutableStateOf<List<User>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Hàm load lại danh sách nhân viên
    fun loadUsers() {
        scope.launch(Dispatchers.IO) {
            val result = runCatching { adminController.getAllEmployees() }
            result.onSuccess { users ->
                userList = users
                message = null
            }.onFailure {
                message = it.message ?: "Không thể tải danh sách"
            }
        }
    }

    // Khi lần đầu vào màn hình
    LaunchedEffect(Unit) {
        loadUsers()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CommonTopBar(navController = navController, title = "Danh sách nhân viên")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            message?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            LazyColumn {
                items(userList, key = { it.idUser }) { user ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Tên: ${user.name}")
                            Text("Email: ${user.email}")
                            Text("Vai trò: ${user.role}")
                            Text("Tuổi: ${user.age}")
                            Text("Số điện thoại: ${user.phone}")

                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        // TODO: navigate tới màn hình cập nhật, truyền user.uid
                                    },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text("Cập nhật")
                                }
                                Button(
                                    onClick = {
                                        scope.launch(Dispatchers.IO) {
                                            val delResult = adminController.deleteEmployee(user.idUser.toString())
                                            delResult.onSuccess {
                                                message = "Xóa thành công"
                                                loadUsers()
                                            }.onFailure {
                                                message = it.message ?: "Lỗi khi xóa"
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Xóa", color = MaterialTheme.colorScheme.onError)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
