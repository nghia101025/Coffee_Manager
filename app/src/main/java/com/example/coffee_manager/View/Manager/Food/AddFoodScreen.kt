package com.example.coffee_manager.View.Manager.Food

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Admin.FoodController
import com.example.coffee_manager.Controller.toBase64
import com.example.coffee_manager.Model.Category
import com.example.coffee_manager.Model.Food
import com.example.coffee_manager.View.CommonTopBar
import com.example.coffee_manager.View.PopupMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(navController: NavController) {
    val context = LocalContext.current
    val controller = remember { FoodController() }
    val scope = rememberCoroutineScope()

    // Form state
    var name by remember { mutableStateOf("") }
    var recipe by remember { mutableStateOf("") }
    var rawPrice by remember { mutableStateOf("") }
    var displayPrice by remember { mutableStateOf("") }

    // Image state
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageError by remember { mutableStateOf<String?>(null) }

    // Category state
    var cats by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedCat by remember { mutableStateOf<Category?>(null) }
    var catExpanded by remember { mutableStateOf(false) }
    var catError by remember { mutableStateOf<String?>(null) }

    // Dialog / snackbar
    var message by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Load categories once
    LaunchedEffect(Unit) {
        controller.getAllCategories()
            .onSuccess {
                cats = it
                println("Loaded categories:")
                it.forEach { c -> println("ID: ${c.idCat}, Name: ${c.name}") }
            }
            .onFailure {
                message = "Không tải được danh mục: ${it.message}"
                showDialog = true
            }
    }




    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageError = null
        imageUri = uri
        uri?.let {
            imageBitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val src = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(src)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CommonTopBar(navController = navController, title = "Thêm thực đơn")

        PopupMessage(show = showDialog, message = message) { showDialog = false }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tên món
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên món") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // Công thức
            OutlinedTextField(
                value = recipe,
                onValueChange = { recipe = it },
                label = { Text("Công thức") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // Giá tiền với định dạng
            OutlinedTextField(
                value = displayPrice,
                onValueChange = { input ->
                    rawPrice = input.filter { it.isDigit() }
                    displayPrice = rawPrice.takeIf { it.isNotEmpty() }
                        ?.let { NumberFormat.getNumberInstance(Locale("vi","VN"))
                            .format(it.toLong()) + " ₫" }
                        ?: ""
                },
                label = { Text("Giá tiền (VNĐ)") },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // Dropdown danh mục
            ExposedDropdownMenuBox(
                expanded = catExpanded,
                onExpandedChange = { catExpanded = !catExpanded }
            ) {
                TextField(
                    value = selectedCat?.name.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Danh mục") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor() // ⚠️ BẮT BUỘC CÓ DÒNG NÀY
                )

                ExposedDropdownMenu(
                    expanded = catExpanded,
                    onDismissRequest = { catExpanded = false }
                ) {
                    cats.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.name) },
                            onClick = {
                                selectedCat = c
                                catExpanded = false
                                catError = null
                            }
                        )
                    }
                }
            }
            catError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(8.dp))

            // Image picker + preview
            Button(onClick = { launcher.launch("image/*") }, Modifier.fillMaxWidth()) {
                Text("Chọn ảnh món ăn")
            }
            imageError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(8.dp))
            imageBitmap?.let { bm ->
                Image(
                    bitmap = bm.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.height(16.dp))

            // Button Xác nhận
            Button(
                onClick = {
                    // Validate
                    var valid = true
                    if (name.isBlank()) valid = false
                    if (recipe.isBlank()) valid = false
                    if (rawPrice.isBlank()) valid = false
                    if (selectedCat == null) {
                        catError = "Chọn danh mục"
                        valid = false
                    }
                    if (imageBitmap == null) {
                        imageError = "Vui lòng chọn ảnh"
                        valid = false
                    }
                    if (!valid) return@Button

                    // Build model & send
                    val food = selectedCat?.let {
                        Food(
                            idFood = "",
                            name = name,
                            recipe = recipe,
                            price = rawPrice.toLong(),
                            available = true,
                            imageUrl = imageBitmap!!.toBase64(),
                            category = it.name,
                            soldCount = 0L
                        )
                    }
                    scope.launch(Dispatchers.IO) {
                        controller.addFood(food!!)
                            .onSuccess {
                                message = "Thêm món thành công"
                                showDialog = true
                                // clear
                                name = ""; recipe = ""
                                rawPrice = ""; displayPrice = ""
                                selectedCat = null
                                imageBitmap = null; imageUri = null
                            }
                            .onFailure {
                                message = "Lỗi: ${it.message}"
                                showDialog = true
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("Xác nhận", color = Color.White)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
