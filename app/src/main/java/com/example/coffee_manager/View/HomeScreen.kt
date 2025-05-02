package com.example.coffee_manager.View
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun HomeScreen(navController: NavController) {
    // State to manage user information or any necessary data
    var message by remember { mutableStateOf("Chào mừng bạn đến với Coffee Manager!") }

    // Layout for home screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, fontSize = 24.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(20.dp))

        // View Menu Button
        Button(
            onClick = {
                navController.navigate("menu") // Navigate to the menu screen
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Xem Menu")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // View Orders Button
        Button(
            onClick = {
                navController.navigate("orders") // Navigate to the orders screen
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Lịch sử Đơn Hàng")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Log Out Button
        Button(
            onClick = {
                navController.navigate("login") { popUpTo("login") { inclusive = true } } // Navigate back to login
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Đăng Xuất")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
}
