package com.example.coffee_manager.View.Admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.UserController
import com.example.coffee_manager.Model.User

@Composable
fun UserListScreen(navController: NavController) {
    var userList by remember { mutableStateOf<List<User>>(emptyList()) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        UserController.getAllUsers(
            onSuccess = { userList = it },
            onError = { message = it }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Danh Sách Nhân Viên", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        if (message.isNotEmpty()) {
            Text(text = message, color = MaterialTheme.colorScheme.error)
        }

        LazyColumn {
            items(userList) { user ->
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
                        Text("Địa chỉ: ${user.address}")

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = {
                                },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Cập nhật")
                            }
                            Button(
                                onClick = {
                                    UserController.deleteUser(
                                        user.IdUser,
                                        onSuccess = {
                                            message = "Xóa thành công"
                                            // Load lại danh sách
                                            UserController.getAllUsers(
                                                onSuccess = { userList = it },
                                                onError = { message = it }
                                            )
                                        },
                                        onError = { message = it }
                                    )
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
