package com.example.coffee_manager.View.Admin

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.UserController
import com.example.coffee_manager.Model.User
import com.example.coffee_manager.View.RegisterScreen

@Composable
fun HomeAdminScreen(navController: NavController) {
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Trang Quản Lý", fontSize = 24.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(20.dp))

        // Nút điều hướng đến màn đăng ký người dùng mới
        Button(
            onClick = { navController.navigate("register") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Thêm Người Dùng Mới")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Nút điều hướng đến màn danh sách nhân viên
        Button(
            onClick = { navController.navigate("user_list") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Xem Tất Cả Nhân Viên")
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (message.isNotEmpty()) {
            Text(text = message, color = Color.Red)
        }
    }
}