package com.example.coffee_manager.View.Manager.Food

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
import androidx.navigation.NavBackStackEntry
import com.example.coffee_manager.Controller.Admin.CategoryController
import com.example.coffee_manager.Controller.Admin.FoodController
import com.example.coffee_manager.Controller.toBase64
import com.example.coffee_manager.Controller.base64ToBitmap
import com.example.coffee_manager.Model.Category
import com.example.coffee_manager.Model.Food
import com.example.coffee_manager.View.CommonTopBar
import com.example.coffee_manager.View.PopupMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateFoodScreen(
    navController: NavController,
    backStackEntry: NavBackStackEntry
) {
    val idArg = backStackEntry.arguments?.getString("idFood")
    val catController = CategoryController()
    val foodController = FoodController()
    var isLoading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    var cats by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedCat by remember { mutableStateOf<Category?>(null) }
    var catExpanded by remember { mutableStateOf(false) }
    var catError by remember { mutableStateOf<String?>(null) }

    // form state
    var name by remember { mutableStateOf("") }
    var recipe by remember { mutableStateOf("") }
    var rawPrice by remember { mutableStateOf("") }
    var displayPrice by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageBase64 by remember { mutableStateOf("") }
    var imageError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // load existing food
    LaunchedEffect(idArg) {
        try {
            val categoryResult = catController.getAllCategories()
            val categoryList = categoryResult.getOrThrow()
            cats = categoryList

            if (idArg != null) {
                val food = foodController.getFoodById(idArg).getOrThrow()

                name = food.name
                recipe = food.recipe
                rawPrice = food.price.toString()
                displayPrice = if (rawPrice.isNotEmpty()) {
                    NumberFormat.getNumberInstance(Locale("vi", "VN")).format(rawPrice.toLong()) + " ₫"
                } else ""
                imageBase64 = food.imageUrl

                // Cập nhật selectedCat khi đã có cats
                selectedCat = categoryList.find { it.name == food.category }
            }

        } catch (e: Exception) {
            message = "Lỗi khi tải dữ liệu: ${e.message}"
            showDialog = true
        } finally {
            isLoading = false
        }
    }



    // image picker
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
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

    Scaffold(
        topBar = { CommonTopBar(navController, title = "Chỉnh sửa thực đơn") }
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
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PopupMessage(show = showDialog, message = message, onDismiss = { showDialog = false })
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Tên món") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = recipe,
                        onValueChange = { recipe = it },
                        label = { Text("Công thức") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = displayPrice,
                        onValueChange = { input ->
                            rawPrice = input.filter { it.isDigit() }
                            displayPrice = if (rawPrice.isNotEmpty()) {
                                NumberFormat.getNumberInstance(Locale("vi","VN")).format(rawPrice.toLong()) + " ₫"
                            } else ""
                        },
                        label = { Text("Giá tiền (VNĐ)") },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))


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

                    Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                        Text("Chọn ảnh")
                    }
                    imageError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Start).padding(top=4.dp))
                    }
                    Spacer(Modifier.height(8.dp))

                    // show existing/base64 or new image
                    val bmpDisplay = imageBitmap ?: base64ToBitmap(imageBase64)
                    bmpDisplay?.let { bm ->
                        Image(
                            bitmap = bm.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            var valid = true
                            if (rawPrice.isBlank()) valid = false
                            if (name.isBlank()) valid = false
                            if (recipe.isBlank()) valid = false
                            if (bmpDisplay == null) { imageError = "Chọn ảnh"; valid = false }
                            if (!valid) return@Button

                            val finalImage = imageBitmap?.toBase64(50) ?: imageBase64
                            val updated = Food(
                                idFood = idArg ?: "",
                                name = name,
                                recipe = recipe,
                                price = rawPrice.toLong(),
                                available = true,
                                imageUrl = finalImage,
                                category = selectedCat?.name.orEmpty()
                            )
                            CoroutineScope(Dispatchers.IO).launch {
                                foodController.updateFood(updated)
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
                        colors = buttonColors(containerColor=Color(0xFF2196F3))
                    ) {
                        Text("Cập nhật", color=Color.White)
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}
