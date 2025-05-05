package com.example.coffee_manager.View.Order

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.AdminController
import com.example.coffee_manager.Model.Food
import com.example.coffee_manager.View.CommonTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(navController: NavController) {
    val adminController = remember { AdminController() }
    var foodList by remember { mutableStateOf<List<Food>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Chạy suspend ngay trong LaunchedEffect
        val result = withContext(Dispatchers.IO) {
            adminController.getAllFoods()
        }
        result
            .onSuccess { list ->
                foodList = list
                message = null
            }
            .onFailure { error ->
                message = "Không tải được menu: ${error.message}"
            }
        isLoading = false
    }

    Column(Modifier.fillMaxSize()) {
        CommonTopBar(navController = navController, title = "Menu Coffee")

        // Khi đang load
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Column
        }

        // Nếu có lỗi
        message?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // Nếu không lỗi nhưng danh sách trống
        if (message == null && foodList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Chưa có món nào", fontSize = 16.sp, color = Color.Gray)
            }
            return@Column
        }

        // Hiển thị grid menu
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(foodList, key = { it.idFood }) { food ->
                // Decode Base64 → Bitmap với downsample
                val bitmap = remember(food.imageUrl) {
                    runCatching {
                        val bytes = Base64.decode(food.imageUrl, Base64.DEFAULT)
                        val opts = BitmapFactory.Options().apply {
                            inJustDecodeBounds = false
                            inSampleSize = 2
                        }
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                    }.getOrNull()
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* TODO: thêm món vào order */ },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(8.dp)
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = food.name,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No Image", fontSize = 12.sp)
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(text = food.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "${food.price}₫",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        if (!food.isAvailable) {
                            Text(
                                text = "Hết hàng",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
