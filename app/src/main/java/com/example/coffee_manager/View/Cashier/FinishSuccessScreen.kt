package com.example.coffee_manager.View.Cashier

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinishSuccessScreen(
    navController: NavController,
    billId: String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hoàn tất") },
                navigationIcon = {
                    // Ẩn nút back hoặc bạn có thể để tùy ý
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(64.dp))
            Text(
                text = "Thanh toán thành công!",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Mã hoá đơn: $billId",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    // Quay về màn hình chính thu ngân
                    navController.navigate("home_thungan") {
                        popUpTo("home_thungan") { inclusive = false }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Quay về trang chính")
            }
        }
    }
}
