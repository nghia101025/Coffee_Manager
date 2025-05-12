
package com.example.coffee_manager.View

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.coffee_manager.Controller.LoginController
import com.example.coffee_manager.R

private const val PREFS_NAME   = "login_prefs"
private const val KEY_EMAIL    = "key_email"
private const val KEY_PASSWORD = "key_password"
private const val KEY_REMEMBER = "key_remember"
private const val TAG = "LoginScreen"

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs   = remember {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    var email        by remember { mutableStateOf(prefs.getString(KEY_EMAIL, "") ?: "") }
    var password     by remember { mutableStateOf(prefs.getString(KEY_PASSWORD, "") ?: "") }
    var rememberPass by remember { mutableStateOf(prefs.getBoolean(KEY_REMEMBER, false)) }
    var message      by remember { mutableStateOf("") }
    var showDialog   by remember { mutableStateOf(false) }
    var loginRole    by remember { mutableStateOf<String?>(null) }
    val controller   = remember { LoginController() }

    // Observe role change to navigate
    LaunchedEffect(loginRole) {
        loginRole?.let { role ->
            Log.d(TAG, "Navigating to home for role: $role")
            // Save prefs if needed
            prefs.edit().apply {
                putBoolean(KEY_REMEMBER, rememberPass)
                if (rememberPass) {
                    putString(KEY_EMAIL, email)
                    putString(KEY_PASSWORD, password)
                } else {
                    remove(KEY_EMAIL)
                    remove(KEY_PASSWORD)
                }
                apply()
            }
            try {
                when (role.trim().lowercase()) {
                    "quản lý"     -> navController.navigate("home_admin")
                    "phục vụ"     -> navController.navigate("home_order")
                    "thu ngân"  -> navController.navigate("home_thungan")
                    "đầu bếp"   -> navController.navigate("home_bep")
                    else        -> {
                        Log.w(TAG, "Unknown role received: $role")
                        navController.navigate("login") // fallback
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Navigation error", e)
                showDialog = true
                message = "Lỗi điều hướng: ${e.localizedMessage}"
            }
            loginRole = null
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showDialog) {
                PopupMessage(
                    show = true,
                    message = message,
                    onDismiss = {
                        Log.d(TAG, "Popup dismissed")
                        showDialog = false
                    }
                )
            }

            // Logo and title
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = "PinkDragon Coffee",
                fontSize = 32.sp,
                color = Color(0xFFB8860B),
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    Log.d(TAG, "Email input: $email")
                },
                placeholder = { Text("Email") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    Log.d(TAG, "Password input length: ${it.length}")
                },
                placeholder = { Text("Mật khẩu") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            // Remember me
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = rememberPass,
                    onCheckedChange = {
                        rememberPass = it
                        Log.d(TAG, "Remember password toggled: $rememberPass")
                    },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2196F3))
                )
                Spacer(Modifier.width(8.dp))
                Text("Ghi nhớ mật khẩu")
            }
            // Login button
            Button(
                onClick = {
                    Log.d(TAG, "Login button clicked with email=$email")
                    controller.loginUser(
                        email = email.trim(),
                        password = password,
                        onSuccess = { rawRole ->
                            val role = rawRole.trim().lowercase()
                            Log.d(TAG, "Login success, role=$role")
                            loginRole = role
                        },
                        onFailure = { msg ->
                            Log.e(TAG, "Login failed: $msg")
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
