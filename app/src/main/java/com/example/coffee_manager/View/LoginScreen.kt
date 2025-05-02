package com.example.coffee_manager.View

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.coffee_manager.Controller.UserController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navController: NavController) {
    // State to hold form input values
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    // Layout for login form
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Đăng nhập", fontSize = 24.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(20.dp))

        // Email input
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Password input
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Login button
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            if (uid != null) {
                                FirebaseFirestore.getInstance().collection("users").document(uid)
                                    .get()
                                    .addOnSuccessListener { document ->
                                        if (document != null && document.exists()) {
                                            val role = document.getString("role") ?: "Order"
                                            when (role) {
                                                "Admin" -> navController.navigate("home_admin")
                                                "Order" -> navController.navigate("home_order")
                                                "Thu ngân" -> navController.navigate("home_thungan")
                                                "Đầu bếp" -> navController.navigate("home_bep")
                                                else -> navController.navigate("home_order") // fallback
                                            }
                                        } else {
                                            message = "Không tìm thấy thông tin người dùng"
                                        }
                                    }
                                    .addOnFailureListener {
                                        message = "Lỗi khi lấy thông tin người dùng: ${it.message}"
                                    }
                            }
                        }
                        .addOnFailureListener {
                            message = "Sai email hoặc mật khẩu: ${it.message}"
                        }
                } else {
                    message = "Vui lòng nhập đầy đủ thông tin"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Đăng nhập")
        }


        Spacer(modifier = Modifier.height(10.dp))

        // Register button
        TextButton(onClick = {
            navController.navigate("register") // Navigate to Register screen
        }) {
            Text("Chưa có tài khoản? Đăng ký")
        }

        // Display error or success message
        Text(
            text = message,
            color = if (message.contains("thành công")) Color.Green else Color.Red,
            fontSize = 16.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}
