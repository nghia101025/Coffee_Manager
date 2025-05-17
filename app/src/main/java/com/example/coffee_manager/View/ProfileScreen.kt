package com.example.coffee_manager.View

import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.coffee_manager.Controller.HistoryController
import com.example.coffee_manager.Controller.LoginController
import com.example.coffee_manager.Controller.base64ToBitmap
import com.example.coffee_manager.Model.History
import com.example.coffee_manager.Model.SessionManager
import com.example.coffee_manager.Model.User
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(navController: NavController) {
    val loginCtrl   = remember { LoginController() }
    val historyCtrl = remember { HistoryController() }
    var user      by remember { mutableStateOf<User?>(null) }
    var histories by remember { mutableStateOf<List<History>>(emptyList()) }
    var loading   by remember { mutableStateOf(true) }
    var error     by remember { mutableStateOf<String?>(null) }

    // Load user
    LaunchedEffect(Unit) {
        loading = true
        loginCtrl.fetchCurrentUser()
            .onSuccess { user = it }
            .onFailure { error = it.message }
        loading = false
    }
    // Load lịch sử
    LaunchedEffect(user) {
        user?.let {
            loading = true
            historyCtrl.getHistoryForCurrentUser()
                .onSuccess { histories = it }
                .onFailure { error = it.message }
            loading = false
        }
    }

    Scaffold(
        topBar = { CommonTopBar(navController, title = "Thông tin cá nhân") }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                error != null -> Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                user == null && loading -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center)
                )
                user != null -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Avatar + Thông tin cơ bản
                        Row(Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Decode Base64 hoặc load URL
                                val bmp = base64ToBitmap(user!!.imageUrl)
                                if (bmp != null) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = rememberAsyncImagePainter(user!!.imageUrl),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(user!!.name, style = MaterialTheme.typography.titleMedium)
                                Text(user!!.email, style = MaterialTheme.typography.bodySmall)
                            }

                            Column(modifier = Modifier.weight(2f)) {
                                InfoRow("Tên", user!!.name)
                                InfoRow("Email", user!!.email)
                                InfoRow("SĐT", user!!.phone)
                                InfoRow("Chức vụ", user!!.role)
                                user!!.dateOfBirth?.let { dob ->
                                    InfoRow(
                                        "Ngày sinh",
                                        dob.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = {
                                        navController.navigate("update_employee/${SessionManager.currentUserId}")
                                    }) { Text("Sửa") }
                                    OutlinedButton(onClick = {
                                        SessionManager.currentUserId = ""
                                        navController.navigate("login") {
                                            popUpTo("profile") { inclusive = true }
                                        }
                                    }) { Text("Đăng xuất") }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Lịch sử thao tác
                        Text("Lịch sử thao tác", style = MaterialTheme.typography.titleMedium)
                        Divider(Modifier.padding(vertical = 8.dp))

                        if (loading) {
                            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                        } else if (histories.isEmpty()) {
                            Text(
                                "Chưa có hoạt động nào",
                                Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(histories) { h ->
                                    HistoryRow(h)
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
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HistoryRow(history: History) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(history.action, style = MaterialTheme.typography.bodyLarge)
            val dt = Instant.ofEpochMilli(history.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
            Text(
                dt.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
