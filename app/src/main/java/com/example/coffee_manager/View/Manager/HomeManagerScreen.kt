package com.example.coffee_manager.View.Manager

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.View.CommonTopBar

@Composable
fun HomeAdminScreen(navController: NavController) {
    var message by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        CommonTopBar(navController = navController, title = "Trang chủ")


        // Nội dung quản lý
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                onClick = { navController.navigate("register") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Thêm Người Dùng Mới")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { navController.navigate("user_list") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Danh sách Nhân Viên")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { navController.navigate("add_food") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Thêm Món Ăn")
            }


            Spacer(modifier = Modifier.height(20.dp))

            if (message.isNotEmpty()) {
                Text(text = message, color = Color.Red)
            }
        }
    }
}
