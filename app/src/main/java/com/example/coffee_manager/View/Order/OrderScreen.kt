package com.example.coffee_manager.View.Order



import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Admin.FoodController
import com.example.coffee_manager.Controller.Order.BillController
import com.example.coffee_manager.Model.Bill
import com.example.coffee_manager.Model.BillItem
import com.example.coffee_manager.Model.Food
import com.example.coffee_manager.View.CommonTopBar
import com.example.coffee_manager.View.PopupMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(navController: NavController) {
    val foodController = remember { FoodController() }
    var foodList by remember { mutableStateOf<List<Food>>(emptyList()) }
    var bestSales by remember { mutableStateOf<List<Food>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var favorites by remember { mutableStateOf(setOf<String>()) }

    // Categories
    val categories = listOf(
        "Tất cả" to Icons.Default.GridView,
        "Trà sữa" to Icons.Default.LocalCafe,
        "Coffee" to Icons.Default.Coffee,
        "Trà" to Icons.Default.Eco,
        "Món ăn" to Icons.Default.Restaurant
    )
    var selectedCategory by remember { mutableStateOf("Tất cả") }

    LaunchedEffect(Unit) {
        foodController.getAllFoods()
            .onSuccess { list ->
                foodList = list
                // Giả sử controller có trả về best sale
                bestSales = list.sortedByDescending { it.soldCount }.take(5)
                message = null
            }
            .onFailure {
                message = "Không tải được menu: ${it.message}"
            }
        isLoading = false
    }


    Scaffold(
        topBar = {
            CommonTopBar(navController, title = "Best Sales")
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Best Sales banner
            Text(
                "Best Sales",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(start = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bestSales, key = { it.idFood }) { food ->
                    Card(
                        Modifier
                            .width(200.dp)
                            .height(180.dp)
                            .clickable { /* có thể điều hướng chi tiết */ },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Box {
                            val bmp = remember(food.imageUrl) {
                                kotlin.runCatching {
                                    val bytes = Base64.decode(food.imageUrl, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                }.getOrNull()
                            }
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = food.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Text(
                                text = food.name,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .fillMaxWidth()
                                    .padding(6.dp),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 2. Categories
            Text(
                "Danh mục",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories) { (label, icon) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(80.dp)
                            .clickable { selectedCategory = label }
                            .background(
                                if (selectedCategory == label)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(icon, contentDescription = label, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            label,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 3. Grid danh sách món
            val displayed = remember(selectedCategory, foodList) {
                if (selectedCategory == "Tất cả") foodList
                else foodList.filter { it.category == selectedCategory }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayed, key = { it.idFood }) { food ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clickable { /* thêm vào giỏ */ },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Box {
                            val bmp = remember(food.imageUrl) {
                                kotlin.runCatching {
                                    val bytes = Base64.decode(food.imageUrl, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                }.getOrNull()
                            }
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = food.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(Modifier.fillMaxSize().background(Color.LightGray))
                            }
                            Column(
                                Modifier
                                    .align(Alignment.BottomStart)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text(
                                    food.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${food.price}₫",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        message?.let {
            Snackbar(Modifier.padding(16.dp)) { Text(it) }
        }
    }
}



