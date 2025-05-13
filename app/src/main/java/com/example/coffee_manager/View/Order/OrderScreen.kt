// View/Order/OrderScreen.kt
package com.example.coffee_manager.View.Order

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Admin.CategoryController
import com.example.coffee_manager.Controller.Admin.FoodController
import com.example.coffee_manager.Model.Category
import com.example.coffee_manager.Model.Food
import com.example.coffee_manager.R
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(navController: NavController) {
    val foodController = remember { FoodController() }
    val categoryController = remember { CategoryController() }

    var foodList by remember { mutableStateOf<List<Food>>(emptyList()) }
    var bestSales by remember { mutableStateOf<List<Food>>(emptyList()) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf("Tất cả") }

    LaunchedEffect(Unit) {
        // Load foods
        foodController.getAllFoods()
            .onSuccess { list ->
                foodList = list
                bestSales = list.sortedByDescending { it.soldCount }.take(5)
            }
            .onFailure { message = it.message }

        // Load categories
        categoryController.getAllCategories()
            .onSuccess { cats ->
                categories = listOf(Category(idCat = "all", name = "Tất cả")) + cats
            }
            .onFailure { message = it.message }

        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu") },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_account),
                            contentDescription = "User Profile",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("cart") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Xem giỏ hàng"
                )
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Best Sales
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
                items(bestSales, key = { it.idFood }) { f ->
                    Card(
                        Modifier
                            .width(200.dp)
                            .height(180.dp)
                            .clickable { navController.navigate("foodDetail/${f.idFood}") },
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box {
                            val bmp = runCatching {
                                val bytes = Base64.decode(f.imageUrl, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            }.getOrNull()
                            if (bmp != null) {
                                Image(
                                    bmp.asImageBitmap(),
                                    contentDescription = f.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Text(
                                f.name,
                                color = Color.White,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .fillMaxWidth()
                                    .padding(6.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Categories
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat.name == selectedCategory
                    Text(
                        cat.name,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else
                                    Color.LightGray.copy(alpha = 0.3f)
                            )
                            .clickable { selectedCategory = cat.name }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Filtered grid
            val displayed = remember(selectedCategory, foodList) {
                if (selectedCategory == "Tất cả") foodList
                else foodList.filter { it.category == selectedCategory }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayed, key = { it.idFood }) { f ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable { navController.navigate("foodDetail/${f.idFood}") },
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box {
                            val bmp = runCatching {
                                val bytes = Base64.decode(f.imageUrl, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            }.getOrNull()
                            if (bmp != null) {
                                Image(
                                    bmp.asImageBitmap(),
                                    contentDescription = f.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column(
                                Modifier
                                    .align(Alignment.BottomStart)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text(
                                    f.name,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "${NumberFormat.getNumberInstance(Locale("vi", "VN")).format(f.price)}₫",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall
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


