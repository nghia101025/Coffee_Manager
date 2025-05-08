package com.example.coffee_manager.View

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.coffee_manager.R
import com.example.coffee_manager.Controller.LoginController

private const val PREFS_NAME = "login_prefs"
private const val KEY_EMAIL = "key_email"
private const val KEY_PASSWORD = "key_password"
private const val KEY_REMEMBER = "key_remember"

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Load saved prefs once
    var email by remember { mutableStateOf(prefs.getString(KEY_EMAIL, "") ?: "") }
    var password by remember { mutableStateOf(prefs.getString(KEY_PASSWORD, "") ?: "") }
    var rememberPassword by remember { mutableStateOf(prefs.getBoolean(KEY_REMEMBER, false)) }
    var message by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    val controller = remember { LoginController() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Thông báo popup
            PopupMessage(
                show = showDialog,
                message = message,
                onDismiss = { showDialog = false }
            )

            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "PinkDragon Coffee",
                fontSize = 32.sp,
                color = Color(0xFFB8860B),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Mật khẩu") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Checkbox ghi nhớ mật khẩu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = rememberPassword,
                    onCheckedChange = { rememberPassword = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF2196F3)
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text("Ghi nhớ mật khẩu")
            }

            // Đăng nhập button
            Button(
                onClick = {
                    controller.loginUser(
                        email = email,
                        password = password,
                        onSuccess = { role ->
                            // Lưu prefs nếu chọn ghi nhớ
                            with(prefs.edit()) {
                                putBoolean(KEY_REMEMBER, rememberPassword)
                                if (rememberPassword) {
                                    putString(KEY_EMAIL, email)
                                    putString(KEY_PASSWORD, password)
                                } else {
                                    remove(KEY_EMAIL)
                                    remove(KEY_PASSWORD)
                                }
                                apply()
                            }
                            // Điều hướng theo role
                            when (role) {
                                "Admin" -> navController.navigate("home_admin")
                                "Order" -> navController.navigate("home_order")
                                "Thu ngân" -> navController.navigate("home_thungan")
                                "Đầu bếp" -> navController.navigate("home_bep")
                                else -> navController.navigate("home_order")
                            }
                        },
                        onFailure = { msg ->
                            message = msg
                            showDialog = true
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("Đăng nhập", color = Color.White)
            }
        }
    }
}
