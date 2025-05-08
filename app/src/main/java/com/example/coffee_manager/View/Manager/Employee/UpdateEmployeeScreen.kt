package com.example.coffee_manager.View.Manager.Employee

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavBackStackEntry
import com.example.coffee_manager.Controller.Admin.EmployeeController
import com.example.coffee_manager.Controller.base64ToBitmap
import com.example.coffee_manager.Controller.toBase64
import com.example.coffee_manager.Model.User
import com.example.coffee_manager.View.CommonTopBar
import com.example.coffee_manager.View.PopupMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateEmployeeScreen(
    navController: NavController,
    backStackEntry: NavBackStackEntry
) {
    // Lấy idUser từ args
    val userIdArg = backStackEntry.arguments?.getString("idUser")
    Log.d("UpdateEmployeeScreen", "userIdArg: $userIdArg")


    val employeeController = remember { EmployeeController() }
    var id by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    // Form state và lỗi
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var age by remember { mutableStateOf("") }
    var ageError by remember { mutableStateOf<String?>(null) }
    var phone by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var role by remember { mutableStateOf("") }
    var roleError by remember { mutableStateOf<String?>(null)}
    var imageError by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBase64 by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    // Load dữ liệu user
    LaunchedEffect(userIdArg) {
        employeeController.getEmployeeById(userIdArg.toString())
            .onSuccess { u ->
                email = u.email
                password = u.password
                name = u.name
                age = u.age.toString()
                phone = u.phone
                role = u.role
                imageBase64 = u.imageUrl
            }
            .onFailure {
                message = "Không tải được thông tin nhân viên: ${it.message}"
                showDialog = true
            }
        isLoading = false
    }

    // Image picker
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            val bmp: Bitmap =
                MediaStore.Images.Media.getBitmap(navController.context.contentResolver, it)
            imageBase64 = bmp.toBase64()
        }
    }

    Scaffold(
        topBar = { CommonTopBar(navController, title = "Chỉnh sửa nhân viên") }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PopupMessage(showDialog, message, onDismiss = { showDialog = false })
                    Spacer(Modifier.height(16.dp))


                    // EMAIL
                    TextField(
                        value = email,
                        onValueChange = { email = it; emailError = null },
                        label = { Text("Email") },
                        isError = emailError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.height(8.dp))

                    // PASSWORD
                    TextField(
                        value = password,
                        onValueChange = { password = it; passwordError = null },
                        label = { Text("Mật khẩu") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = passwordError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.height(8.dp))

                    // NAME
                    TextField(
                        value = name,
                        onValueChange = { name = it; nameError = null },
                        label = { Text("Tên") },
                        isError = nameError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.height(8.dp))

                    // AGE
                    TextField(
                        value = age,
                        onValueChange = { age = it; ageError = null },
                        label = { Text("Tuổi") },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        ),
                        isError = ageError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    ageError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.height(8.dp))

                    // PHONE
                    TextField(
                        value = phone,
                        onValueChange = { phone = it; phoneError = null },
                        label = { Text("Số điện thoại") },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        ),
                        isError = phoneError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.height(8.dp))

                    // ROLE
                    ExposedDropdownMenuBox(expanded = false, onExpandedChange = {}) {}
                    RoleDropdownEmployee(
                        selectedRole = role,
                        onRoleSelected = { role = it; roleError = null }
                    )
                    roleError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.height(8.dp))

                    // IMAGE PICKER
                    // HIỂN THỊ ẢNH NHÂN VIÊN
                    val bitmap = remember(imageBase64) {
                        base64ToBitmap(imageBase64)
                    }

                    Spacer(Modifier.height(4.dp))

                    if (imageUri != null) {
                        val bmp: Bitmap = MediaStore.Images.Media.getBitmap(
                            navController.context.contentResolver,
                            imageUri
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .background(Color.LightGray)
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Ảnh đã chọn",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        bitmap?.let {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .background(Color.LightGray)
                            ) {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "Ảnh từ hệ thống",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(15.dp))

                    Button(
                        onClick = { pickImage.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Chọn ảnh")
                    }
                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            var valid = true
                            if (imageBase64.isBlank()) {
                                imageError = "Vui lòng chọn ảnh"
                                showDialog = true
                                valid = false
                            }
                            if (email.isBlank()) {
                                emailError = "Email không được bỏ trống";showDialog = true; valid = false
                            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "Email không hợp lệ";showDialog = true; valid = false
                            }
                            if (password.length < 8) {
                                passwordError = "Mật khẩu tối thiểu 8 ký tự";showDialog = true; valid = false
                            }
                            if (name.isBlank()) {
                                nameError = "Tên không được bỏ trống";showDialog = true; valid = false
                            }
                            val ageInt = age.toIntOrNull()
                            if (ageInt == null || ageInt <= 0) {
                                ageError = "Tuổi không hợp lệ";showDialog = true; valid = false
                            }
                            if (!phone.matches(Regex("^0\\d{9}\$"))) {
                                phoneError = "SĐT phải bắt đầu 0 và đủ 10 số";showDialog = true ;valid = false
                            }
                            if (role.isBlank()) {
                                roleError = "Chọn chức vụ";showDialog = true; valid = false
                            }
                            if (!valid) return@Button
                            val update = User(
                                idUser = userIdArg.toString(),
                                email = email,
                                password = password,
                                name = name,
                                age = age.toInt(),
                                phone = phone,
                                role = role,
                                imageUrl = imageBase64
                            )
                            CoroutineScope(Dispatchers.IO).launch {
                                val exists = employeeController.isEmailRegistered(email)
                                if (exists) {
                                    message = "Email đã được sử dụng."
                                    showDialog = true
                                } else
                                    employeeController.updateEmployee(update)
                                        .onSuccess {
                                            message = "Cập nhật thành công"
                                            showDialog = true
                                        }
                                        .onFailure {
                                            message = "Lỗi: ${it.message}"
                                            showDialog = true
                                        }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
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
    ExposedDropdownMenuBox(expanded=expanded,onExpandedChange={ expanded=!expanded }) {
        TextField(value=selectedRole,onValueChange={},readOnly=true,label={Text("Chức vụ")},trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(expanded=expanded)},modifier=Modifier.fillMaxWidth().menuAnchor())
        ExposedDropdownMenu(expanded=expanded,onDismissRequest={expanded=false}){
            roles.forEach{r->
                DropdownMenuItem(text={Text(r)},onClick={onRoleSelected(r);expanded=false})
            }
        }
    }
}
