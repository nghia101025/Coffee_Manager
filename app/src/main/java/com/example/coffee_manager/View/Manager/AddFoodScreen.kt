package com.example.coffee_manager.View.Manager

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.AdminController
import com.example.coffee_manager.Model.Food
import com.example.coffee_manager.View.CommonTopBar
import androidx.compose.foundation.Image
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.rememberAsyncImagePainter
import com.example.coffee_manager.Controller.toBase64
import com.example.coffee_manager.View.PopupMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var recipe by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    val controller = AdminController()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current


    Column(modifier = Modifier.fillMaxSize()) {
        CommonTopBar(navController = navController, title = "Thêm thực đơn")
        PopupMessage(
            show = showDialog,
            message = message,
            onDismiss = { showDialog = false }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên món") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            TextField(
                value = recipe,
                onValueChange = { recipe = it },
                label = { Text("Công thức") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            TextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Giá tiền (VNĐ)") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Number // Chỉ hiển thị bàn phím số
                ),
                keyboardActions = KeyboardActions(onDone = { /* no-op */ }),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            var imageUri by remember { mutableStateOf<Uri?>(null) }
            var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

            // launcher để chọn ảnh từ gallery
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                imageUri = uri
                uri?.let {
                    // Dùng `context` thay vì LocalContext.current
                    imageBitmap = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    } else {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    }
                }
            }

            Column {
                // Nút chọn ảnh
                Button(onClick = { launcher.launch("image/*") }) {
                    Text("Chọn ảnh món ăn")
                }

                // Hiển thị preview
                imageBitmap?.let { bm ->
                    Image(
                        bitmap = bm.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Spacer(Modifier.height(15.dp))

                // ... các TextField cho name, ingredients, recipe, price, isAvailable ...
                Button(onClick = {
                    // 2. Chuyển Bitmap sang Base64
                    val base64Image = imageBitmap?.toBase64(quality = 50)
                    if (base64Image == null) {
                        // báo lỗi nếu chưa chọn ảnh
                        // showDialog = true; message = "Vui lòng chọn ảnh"
                        return@Button
                    }

                    // 3. Tạo model Food với trường imageBase64
                    val food = Food(
                        idFood = "",
                        name = name,
                        recipe = recipe,
                        price = price,
                        isAvailable = isAvailable,
                        imageUrl = base64Image
                    )
                    // Gọi controller để lưu
                    CoroutineScope(Dispatchers.IO).launch {
                        controller.addFood(food)
                            .onSuccess {
                                // thông báo thành công
                                message = "Thêm món thành công"
                                showDialog = true

                            }
                            .onFailure {
                                message = "Lỗi"
                                showDialog = true
                                // thông báo lỗi
                            }
                    }
                },
                    modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)))
                {
                    Text("Thêm món")
                }
            }
        }
    }
}
