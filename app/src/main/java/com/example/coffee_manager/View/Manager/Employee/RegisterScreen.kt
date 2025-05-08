package com.example.coffee_manager.View.Manager.Employee

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Admin.EmployeeController
import com.example.coffee_manager.Controller.toBase64
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
    var role by remember { mutableStateOf("Phục vụ") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBase64 by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf<String?>(null) }

    val employeeController = EmployeeController()
    val scrollState = rememberScrollState()

    // Launcher để chọn ảnh
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            val bitmap: Bitmap =
                MediaStore.Images.Media.getBitmap(navController.context.contentResolver, it)
            imageBase64 = bitmap.toBase64()
        }
    }

    fun clearFields() {
        email = ""
        password = ""
        name = ""
        age = ""
        phoneNumber = ""
        role = "Phục vụ"
        imageUri = null
        imageBase64 = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PopupMessage(
            show = showDialog,
            message = message,
            onDismiss = { showDialog = false }
        )
        CommonTopBar(navController = navController, title = "Thêm nhân viên")
        Spacer(Modifier.height(16.dp))

        // Email
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))

        // Mật khẩu
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))

        // Tên
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Tên") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))

        // Tuổi
        TextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Tuổi") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number // Chỉ hiển thị bàn phím số
            ),
            keyboardActions = KeyboardActions(onDone = { /* no-op */ }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))

        // Số điện thoại
        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Số điện thoại") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number // Chỉ hiển thị bàn phím số
            ),
            keyboardActions = KeyboardActions(onDone = { /* no-op */ }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))

        // Chọn chức vụ
        RoleDropdown(selectedRole = role, onRoleSelected = { role = it })
        Spacer(Modifier.height(10.dp))

        // Chọn ảnh
        Button(
            onClick = { pickImage.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Chọn ảnh")
        }
        Spacer(Modifier.height(10.dp))

        // Hiển thị ảnh nếu đã chọn
        imageUri?.let {
            val bitmap: Bitmap =
                MediaStore.Images.Media.getBitmap(navController.context.contentResolver, it)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .background(Color.LightGray)
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.height(10.dp))
        }

        // Nút Xác nhận
        Button(
            onClick = {
                val ageInt = age.toIntOrNull()
                val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
                val isPasswordValid = password.length >= 8
                val isPhoneValid = phoneNumber.matches(Regex("^0\\d{9}\$"))

                when {
                    email.isBlank() || password.isBlank() ||imageBase64.isBlank() ||
                            name.isBlank() || age.isBlank() || phoneNumber.isBlank() -> {
                        message = "Vui lòng điền đầy đủ thông tin."
                        showDialog = true
                    }
                    !isEmailValid -> {
                        message = "Email không hợp lệ."
                        showDialog = true
                    }
                    !isPasswordValid -> {
                        message = "Mật khẩu phải từ 8 ký tự trở lên."
                        showDialog = true
                    }
                    ageInt == null || ageInt <= 0 -> {
                        message = "Tuổi không hợp lệ."
                        showDialog = true
                    }
                    !isPhoneValid -> {
                        message = "Số điện thoại phải bắt đầu bằng số 0 và gồm đúng 10 chữ số."
                        showDialog = true
                    }

                    else -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            val exists = employeeController.isEmailRegistered(email)
                            if (exists) {
                                message = "Email đã được sử dụng."
                                showDialog = true
                            } else {
                                val newUser = User(
                                    email = email,
                                    password = password,
                                    name = name,
                                    age = ageInt,
                                    phone = phoneNumber,
                                    role = role,
                                    imageUrl = imageBase64
                                )
                                val result = employeeController.addEmployee(newUser)
                                result.onSuccess {
                                    message = "Đăng ký thành công"
                                    showDialog = true
                                    clearFields()
                                }.onFailure {
                                    message = it.message ?: "Lỗi không xác định"
                                    showDialog = true
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
        ) {
            Text("Xác nhận", color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDropdown(selectedRole: String, onRoleSelected: (String) -> Unit) {
    val roles = listOf("Quản lý", "Đầu bếp", "Phục vụ", "Thu Ngân")
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
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
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
