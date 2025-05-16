// View/Order/FoodDetailScreen.kt
package com.example.coffee_manager.View.Order

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Order.BillController
import com.example.coffee_manager.Controller.Order.FoodController
import com.example.coffee_manager.Model.Food
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    navController: NavController,
    foodId: String
) {
    val foodController = remember { FoodController() }
    val billController = remember { BillController() }
    val scope = rememberCoroutineScope()

    var food by remember { mutableStateOf<Food?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var addingToCart by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Load dữ liệu món
    LaunchedEffect(foodId) {
        loading = true
        foodController.getFoodById(foodId)
            .onSuccess { food = it }
            .onFailure { error = it.message }
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(food?.name ?: "Chi tiết món") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("cart") }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Giỏ hàng")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                loading -> CircularProgressIndicator()
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                food != null -> {
                    val f = food!!
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Ảnh
                        val bmp = runCatching {
                            val bytes = Base64.decode(f.imageUrl, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        }.getOrNull()
                        if (bmp != null) {
                            Image(
                                bmp.asImageBitmap(),
                                contentDescription = f.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        // Tên & Giá
                        Text(f.name, style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${NumberFormat.getNumberInstance(Locale("vi", "VN")).format(f.price)}₫",
                            style = MaterialTheme.typography.titleMedium
                        )
                        // Category
                        Text("Danh mục: ${f.category}", style = MaterialTheme.typography.bodyMedium)
                        // Công thức
                        Text("Công thức:", style = MaterialTheme.typography.titleSmall)
                        Text(f.recipe, style = MaterialTheme.typography.bodySmall)
                        // Tình trạng
                        Text(
                            text = if (f.available) "Còn hàng" else "Hết hàng",
                            color = if (f.available) Color.Green else Color.Red,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        // Đã bán
                        Text("Đã bán: ${f.soldCount}", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(24.dp))

                        // Nút thêm vào giỏ hàng
                        Button(
                            onClick = {
                                if (!f.available) {
                                    // Nếu hết hàng, show snackbar
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Món này đã hết hàng!")
                                    }
                                    return@Button
                                }
                                addingToCart = true
                                scope.launch(Dispatchers.IO) {
                                    billController.addToCart(f.idFood, 1)
                                        .onFailure {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Lỗi thêm vào giỏ: ${it.message}")
                                            }
                                        }
                                    addingToCart = false
                                }
                            },
                            enabled = f.available && !addingToCart,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (addingToCart) "Đang thêm..." else "Thêm vào giỏ hàng")
                        }
                    }
                }
            }
        }
    }
}
