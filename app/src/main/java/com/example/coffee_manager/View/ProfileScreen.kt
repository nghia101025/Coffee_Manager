// ProfileScreen.kt
package com.example.coffee_manager.View

import android.graphics.BitmapFactory
import android.util.Base64
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.coffee_manager.Controller.LoginController
import com.example.coffee_manager.Controller.base64ToBitmap
import com.example.coffee_manager.Model.SessionManager
import com.example.coffee_manager.Model.User
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(navController: NavController) {
    var user by remember { mutableStateOf<User?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    val loginController = remember { LoginController() }

    LaunchedEffect(Unit) {
        loading = true
        loginController.fetchCurrentUser()
            .onSuccess { fetched -> user = fetched }
            .onFailure { ex -> error = ex.message }
        loading = false
    }

    Scaffold(
        topBar = { CommonTopBar(navController, title = "Thông tin cá nhân") }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                user != null -> {
                    val u = user!!
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Cột trái: avatar & vai trò
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Thử decode Base64
                            val bmp = base64ToBitmap(u.imageUrl)
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "User Avatar",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Fallback sang URL (hoặc mặc định)
                                AsyncImage(
                                    model = u.imageUrl,
                                    contentDescription = "User Avatar",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(text = u.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = u.role, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(text = u.phone, style = MaterialTheme.typography.bodySmall)
                        }

                        // Cột phải: thông tin chi tiết
                        Column(
                            modifier = Modifier
                                .weight(2f)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            InfoRow(label = "Tên", value = u.name)
                            InfoRow(label = "Email", value = u.email)
                            InfoRow(label = "Số điện thoại", value = u.phone)
                            InfoRow(label = "Chức vụ", value = u.role)
                            InfoRow(
                                label = "Ngày sinh",
                                value = u.dateOfBirth
                                    ?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                    ?: "Chưa có"
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    navController.navigate("update_employee/${SessionManager.currentUserId}")
                                }) {
                                    Text("Sửa")
                                    }
                                OutlinedButton(onClick = {
                                    SessionManager.currentUserId = ""
                                    navController.navigate("login") {
                                        popUpTo("profile") { inclusive = true }
                                    }
                                }) {
                                    Text("Đăng xuất")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
