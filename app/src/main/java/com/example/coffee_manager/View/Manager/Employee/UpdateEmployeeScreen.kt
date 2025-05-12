// View/Manager/Employee/UpdateEmployeeScreen.kt
package com.example.coffee_manager.View.Manager.Employee

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import androidx.annotation.RequiresApi
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Admin.EmployeeController
import com.example.coffee_manager.Controller.base64ToBitmap
import com.example.coffee_manager.Controller.toBase64
import com.example.coffee_manager.Model.User
import com.example.coffee_manager.View.CommonTopBar
import com.example.coffee_manager.View.PopupMessage
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import java.time.Period

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UpdateEmployeeScreen(
    navController: NavController,
    userId: String
) {
    val TAG = "UpdateEmployeeScreen"
    val context = LocalContext.current
    val controller = remember { EmployeeController() }
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()

    // state loading + dialog
    var isLoading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    // form fields
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var dob by remember { mutableStateOf<LocalDate?>(null) }
    var dobError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
    var phone by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var role by remember { mutableStateOf("") }
    var roleError by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBase64 by remember { mutableStateOf("") }
    var imageError by remember { mutableStateOf<String?>(null) }


    // load user once
    LaunchedEffect(userId) {
        controller.getEmployeeById(userId)
            .onSuccess { u ->
                email = u.email
                name = u.name
                dob = u.dateOfBirth
                phone = u.phone
                role = u.role
                imageBase64 = u.imageUrl
            }
            .onFailure {
                message = "Không tải được nhân viên: ${it.message}"
                showDialog = true
            }
        isLoading = false
    }

    // image picker
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            try {
                val bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                imageBase64 = bmp.toBase64()
            } catch (e: Exception) {
                imageError = "Lỗi đọc ảnh"
            }
        }
    }

    Scaffold(topBar = { CommonTopBar(navController, "Cập nhật nhân viên") }) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scroll)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PopupMessage(showDialog, message) { showDialog = false }

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; emailError = null },
                        label = { Text("Email") },
                        isError = emailError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.height(8.dp))

                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; nameError = null },
                        label = { Text("Họ và tên") },
                        isError = nameError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.height(8.dp))

                    // Phone
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it; phoneError = null },
                        label = { Text("Số điện thoại") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = phoneError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.height(8.dp))

                    // Role
                    RoleDropdownEmployee(selectedRole = role, onRoleSelected = { role = it; roleError = null })
                    roleError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.height(8.dp))

                    Spacer(Modifier.height(8.dp))
                    Text(text = "Ngày sinh:", style = MaterialTheme.typography.titleMedium)

                    DatePickerExample(
                        initialDate = dob,
                        onDateSelected = { selectedDate ->
                            dob = selectedDate
                            dobError = null
                        }
                    )

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = dob?.format(dateFormatter) ?: "Chưa chọn",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (dobError != null) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                    dobError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.height(8.dp))

                    // Avatar
                    val bmpFromBase64 = remember(imageBase64) { base64ToBitmap(imageBase64) }
                    Box(
                        Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            imageUri != null -> {
                                val bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri!!)
                                Image(bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            }
                            bmpFromBase64 != null -> {
                                Image(bmpFromBase64.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            }
                            else -> {
                                Text(text = "No Image", color = Color.Gray)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { pickImage.launch("image/*") }, Modifier.fillMaxWidth()) {
                        Text("Chọn ảnh")
                    }
                    imageError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.height(16.dp))

                    // Submit
                    Button(
                        onClick = {
                            var valid = true
                            if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "Email không hợp lệ"; valid = false
                            }
                            if (password.isNotBlank() && password.length < 8) {
                                passwordError = "Mật khẩu tối thiểu 8 ký tự"; valid = false
                            }
                            if (name.isBlank()) {
                                nameError = "Tên không được để trống"; valid = false
                            }
                            val age = dob?.let { Period.between(it, LocalDate.now()).years } ?: -1
                            if (age !in 18..65) {
                                dobError = "Tuổi phải từ 18 đến 65"; valid = false
                            }
                            if (!phone.matches(Regex("^0\\d{9}$"))) {
                                phoneError = "SĐT phải bắt đầu 0 và đủ 10 số"; valid = false
                            }
                            if (role.isBlank()) {
                                roleError = "Chọn chức vụ"; valid = false
                            }
                            if (imageBase64.isBlank()) {
                                imageError = "Chọn ảnh"; valid = false
                            }
                            if (!valid) {
                                message = "Vui lòng sửa các trường bị lỗi"
                                showDialog = true
                                return@Button
                            }
                            scope.launch {
                                // update email if changed
                                val current = controller.getCurrentAuthUser()
                                if (current?.email != email) {
                                    try {
                                        current?.updateEmail(email)?.await()
                                    } catch (e: Exception) {
                                        emailError = if (e.message?.contains("already in use") == true)
                                            "Email đã được sử dụng" else "Lỗi cập nhật email"
                                        message = emailError!!
                                        showDialog = true
                                        return@launch
                                    }
                                }
                                // build user
                                val updated = User(
                                    idUser = userId,
                                    email = email,
                                    password = if (password.isBlank()) "" else password,
                                    name = name,
                                    dateOfBirth = dob,
                                    phone = phone,
                                    role = role,
                                    imageUrl = imageBase64
                                )
                                // firestore update
                                val res = withContext(Dispatchers.IO) { controller.updateEmployee(updated) }
                                withContext(Dispatchers.Main) {
                                    res.onSuccess {
                                        message = "Cập nhật thành công"
                                        showDialog = true
                                    }.onFailure {
                                        message = "Lỗi: ${it.message}"
                                        showDialog = true
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Text("Xác nhận", color = Color.White)
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDropdownEmployee(selectedRole: String, onRoleSelected: (String) -> Unit) {
    val roles = listOf("Quản lý", "Đầu bếp", "Phục vụ", "Thu Ngân")
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedRole,
            onValueChange = {},
            label = { Text("Chức vụ") },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            roles.forEach { r ->
                DropdownMenuItem(text = { Text(r) }, onClick = {
                    onRoleSelected(r)
                    expanded = false
                })
            }
        }
    }
}


