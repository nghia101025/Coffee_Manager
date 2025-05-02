package com.example.coffee_manager.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.coffee_manager.Controller.UserController
import com.example.coffee_manager.Model.User

@Composable
fun RegisterScreen(navController: NavController) {
    // States for user inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Order") } // Default role is "Order"
    var message by remember { mutableStateOf("") }

    // Layout for register screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Đăng ký tài khoản", fontSize = 24.sp, color = Color.Black)
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
        Spacer(modifier = Modifier.height(10.dp))

        // Name input
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Tên") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Age input
        TextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Tuổi") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { /* Handle done action */ }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Address input
        TextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Địa chỉ") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Role Selection (RadioButton or Dropdown)
        RoleDropdown(role) { selected ->
            role = selected
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                // Kiểm tra độ tuổi trước khi chuyển đổi
                val ageInt = try {
                    age.toInt() // Cố gắng chuyển đổi age sang Int
                } catch (e: NumberFormatException) {
                    null // Nếu không thể chuyển đổi, trả về null
                }

                if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && ageInt != null && address.isNotEmpty()) {
                    // Tạo đối tượng User từ các giá trị đầu vào
                    val newUser = User(
                        email = email,
                        password = password,
                        name = name,
                        age = ageInt,
                        address = address,
                        role = role
                    )

                    // Gọi hàm registerUser với đối tượng User
                    UserController.registerUser(
                        newUser,
                        onSuccess = {
                            message = "Đăng ký thành công!"
                            // Điều hướng đến màn hình login sau khi đăng ký thành công
                            navController.navigate("login") {
                                popUpTo("register") { inclusive = true } // Xóa màn hình đăng ký khỏi stack
                            }
                        },
                        onError = { error ->
                            message = error
                        }
                    )
                } else {
                    message = "Vui lòng điền đầy đủ thông tin hợp lệ."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Đăng ký")
        }



        // Display message
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = message,
            color = if (message.contains("thành công")) Color.Green else Color.Red,
            fontSize = 16.sp
        )
    }
}

@Composable
fun RoleOption(roleName: String, selectedRole: String, onRoleSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onRoleSelected(roleName) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selectedRole == roleName,
            onClick = { onRoleSelected(roleName) }
        )
        Text(text = roleName, modifier = Modifier.padding(start = 8.dp))
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
                .menuAnchor()
                .fillMaxWidth()
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


@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(navController = rememberNavController())
}
