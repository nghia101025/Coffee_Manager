package com.example.coffee_manager.View.Manager.Employee

import android.app.DatePickerDialog
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Admin.EmployeeController
import com.example.coffee_manager.Controller.toBase64
import com.example.coffee_manager.Model.User
import com.example.coffee_manager.View.CommonTopBar
import com.example.coffee_manager.View.PopupMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

private const val TAG = "RegisterScreen"

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val employeeController = EmployeeController()

    // Form state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf<LocalDate?>(null) }
    var phoneNumber by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Phục vụ") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBase64 by remember { mutableStateOf("") }

    // UI state
    var message by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Log mỗi lần recomposition
    Log.d(TAG, "Recomposed RegisterScreen: dob=$dob, showDatePicker=$showDatePicker")

    // Android DatePickerDialog
    if (showDatePicker) {
        Log.d(TAG, "About to show DatePickerDialog")
        val todayCalendar = Calendar.getInstance()
        try {
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    Log.d(TAG, "DatePickerDialog.onDateSet: $day/${month+1}/$year")
                    dob = LocalDate.of(year, month + 1, day)
                    showDatePicker = false
                },
                todayCalendar.get(Calendar.YEAR),
                todayCalendar.get(Calendar.MONTH),
                todayCalendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                setOnCancelListener {
                    Log.d(TAG, "DatePickerDialog cancelled")
                    showDatePicker = false
                }
            }.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing DatePickerDialog", e)
            showDatePicker = false
        }
    }

    // Image picker launcher
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        Log.d(TAG, "pickImageLauncher returned uri=$uri")
        uri?.let {
            imageUri = it
            try {
                val bitmap: Bitmap =
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                imageBase64 = bitmap.toBase64()
                Log.d(TAG, "Image converted to base64, length=${imageBase64.length}")
            } catch (e: Exception) {
                Log.e(TAG, "Error converting image", e)
                message = "Không thể đọc ảnh: ${e.message}"
                showDialog = true
            }
        }
    }

    fun clearFields() {
        Log.d(TAG, "Clearing form fields")
        email = ""
        password = ""
        name = ""
        dob = null
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
        PopupMessage(show = showDialog, message = message) {
            Log.d(TAG, "PopupMessage dismissed")
            message = ""
            showDialog = false
        }
        CommonTopBar(navController = navController, title = "Thêm nhân viên")
        Spacer(Modifier.height(16.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                Log.d(TAG, "Email changed: $email")
            },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                Log.d(TAG, "Email changed: $password")
            },
            label = { Text("Mật khẩu") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                Log.d(TAG, "Name changed: $name")
            },
            label = { Text("Tên") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))

        // Phone
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                phoneNumber = it
                Log.d(TAG, "Phone changed: $phoneNumber")
            },
            label = { Text("Số điện thoại") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))

        // Role dropdown
        RoleDropdown(selectedRole = role, onRoleSelected = {
            role = it
            Log.d(TAG, "Role selected: $role")
        })
        Spacer(Modifier.height(10.dp))

        // Pick image
        Button(
            onClick = {
                Log.d(TAG, "Pick image button clicked")
                pickImageLauncher.launch("image/*")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Chọn ảnh")
        }
        Spacer(Modifier.height(10.dp))

        // Preview image
        imageUri?.let { uri ->
            Log.d(TAG, "Displaying selected image preview")
            val bitmap: Bitmap =
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(10.dp))
        }

        // Date of Birth
        var dob by remember { mutableStateOf<LocalDate?>(null) }

        DatePickerExample { selectedDate ->
            dob = selectedDate
        }
        Text(text = "Ngày sinh: ${dob?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Chưa chọn"}")

        Spacer(Modifier.height(10.dp))

        // Submit button
        Button(
            onClick = {
                // Validation
                val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
                val ageValid = dob?.let { Period.between(it, LocalDate.now()).years in 18..65 } == true
                val isPhoneValid = phoneNumber.matches(Regex("^0\\d{9}\$"))

                when {
                    email.isBlank() || password.isBlank() || name.isBlank()
                            || dob == null || phoneNumber.isBlank() || imageBase64.isBlank() -> {
                        message = "Vui lòng điền đầy đủ thông tin."
                        showDialog = true
                    }
                    !isEmailValid -> {
                        message = "Email không hợp lệ."
                        showDialog = true
                    }
                    password.length < 8 -> {
                        message = "Mật khẩu phải từ 8 ký tự trở lên."
                        showDialog = true
                    }
                    !ageValid -> {
                        message = "Tuổi phải từ 18 đến 65."
                        showDialog = true
                    }
                    !isPhoneValid -> {
                        message = "Số điện thoại không hợp lệ."
                        showDialog = true
                    }
                    else -> {
                        scope.launch {
                            val phoneExists = withContext(Dispatchers.IO) {
                                employeeController.isPhoneNumberExists(phoneNumber)
                            }

                            if (phoneExists) {
                                message = "Số điện thoại đã tồn tại."
                                showDialog = true
                                return@launch
                            }

                            val newUser = User(
                                idUser = "",
                                email = email,
                                password = password,
                                name = name,
                                dateOfBirth = dob,
                                phone = phoneNumber,
                                role = role,
                                imageUrl = imageBase64
                            )

                            val result = withContext(Dispatchers.IO) {
                                employeeController.addEmployee(newUser, password)
                            }

                            result
                                .onSuccess {
                                    Log.i(TAG, "Registration successful for $email")
                                    message = "Đăng ký thành công"
                                    showDialog = true
                                    clearFields()
                                }
                                .onFailure { ex ->
                                    Log.e(TAG, "Registration failed", ex)
                                    message = ex.message ?: "Lỗi không xác định"
                                    showDialog = true
                                }
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
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
        OutlinedTextField(
            value = selectedRole,
            onValueChange = { },
            readOnly = true,
            label = { Text("Chức vụ") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                Log.d(TAG, "RoleDropdown dismissed")
            }
        ) {
            roles.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role) },
                    onClick = {
                        onRoleSelected(role)
                        expanded = false
                        Log.d(TAG, "RoleDropdown selected: $role")
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DatePickerExample(
    initialDate: LocalDate? = null,            // ← thêm tham số này
    onDateSelected: (LocalDate) -> Unit
) {
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }

    if (showDialog.value) {
        // Dùng initialDate để khởi tạo Calendar, nếu null thì lấy now
        val calendar = java.util.Calendar.getInstance().apply {
            initialDate?.let {
                set(it.year, it.monthValue - 1, it.dayOfMonth)
            }
        }

        val datePickerDialog = android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val date = LocalDate.of(year, month + 1, dayOfMonth)
                onDateSelected(date)
                showDialog.value = false
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )

        // Huỷ dialog vẫn đóng đúng
        datePickerDialog.setOnCancelListener { showDialog.value = false }
        datePickerDialog.setOnDismissListener { showDialog.value = false }

        datePickerDialog.show()
    }

    IconButton(onClick = { showDialog.value = true }) {
        Icon(
            imageVector = Icons.Filled.DateRange,
            contentDescription = "Chọn ngày sinh",
            tint = Color.Gray
        )
    }
}
