package com.example.coffee_manager.View.Manager

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.coffee_manager.Controller.AdminController
import com.example.coffee_manager.Model.User
import com.example.coffee_manager.View.CommonTopBar
import com.example.coffee_manager.View.PopupMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun RegisterScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Order") }
    var message by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val adminController = AdminController()

    fun clearFields() {
        email = ""
        password = ""
        name = ""
        age = ""
        phoneNumber = ""
        role = "Order"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PopupMessage(
            show = showDialog,
            message = message,
            onDismiss = { showDialog = false }
        )
        CommonTopBar(navController = navController, title = "Thêm nhân viên")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Email
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            // Password
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            // Name
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            // Age
            TextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Tuổi") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {}),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            // Phone
            TextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Số điện thoại") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))

            // Role Dropdown
            RoleDropdown(selectedRole = role, onRoleSelected = { role = it })
            Spacer(Modifier.height(20.dp))
            // Submit button
            Button(
                onClick = {
                    val ageInt = age.toIntOrNull()
                    val isValidEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches()
                    val isValidPassword = password.length >= 8
                    val isPhoneValid = phoneNumber.all { it.isDigit() }

                    when {
                        email.isBlank() || password.isBlank() ||
                                name.isBlank() || age.isBlank() || phoneNumber.isBlank() -> {
                            message = "Vui lòng điền đầy đủ thông tin."
                            showDialog = true
                        }
                        !isValidEmail -> {
                            message = "Email không hợp lệ."
                            showDialog = true
                        }
                        !isValidPassword -> {
                            message = "Mật khẩu phải từ 8 ký tự trở lên."
                            showDialog = true
                        }
                        ageInt == null || ageInt <= 0 -> {
                            message = "Tuổi không hợp lệ."
                            showDialog = true
                        }
                        !isPhoneValid -> {
                            message = "Số điện thoại chỉ chứa chữ số."
                            showDialog = true
                        }
                        else -> {
                            // Kiểm tra email đã đăng ký
                            CoroutineScope(Dispatchers.IO).launch {
                                val isEmailExist = adminController.isEmailRegistered(email)
                                if (isEmailExist) {
                                    message = "Email đã được sử dụng."
                                    showDialog = true
                                } else {
                                    val result = adminController.addEmployee(
                                        email = email,
                                        password = password,
                                        name = name,
                                        age = ageInt ?: 0,
                                        phone = phoneNumber,
                                        role = role
                                    )
                                    result.onSuccess {
                                        message = "Đăng ký thành công"
                                        showDialog = true
                                        clearFields()
                                    }.onFailure {
                                        message = it.message ?: "Lỗi không xác định"
                                    }
                                    showDialog = true
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Đăng ký")
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDropdown(selectedRole: String, onRoleSelected: (String) -> Unit) {
    val roles = listOf("Admin", "Thu ngân", "Đầu bếp", "Order")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedRole,
            onValueChange = {},
            readOnly = true,
            label = { Text("Chọn chức vụ") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            roles.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role) },
                    onClick = {
                        onRoleSelected(role)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun RoleOption(roleName: String, selectedRole: String, onRoleSelected: (String) -> Unit) {
    Row(
        Modifier
            .padding(8.dp)
            .clickable { onRoleSelected(roleName) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selectedRole == roleName,
            onClick = { onRoleSelected(roleName) }
        )
        Spacer(Modifier.width(4.dp))
        Text(text = roleName)
    }
}
