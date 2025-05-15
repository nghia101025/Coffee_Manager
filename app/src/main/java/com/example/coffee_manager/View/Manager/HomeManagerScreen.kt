package com.example.coffee_manager.View.Manager

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Model.FeatureItem
import com.example.coffee_manager.Model.FeatureSection
import com.example.coffee_manager.R
import com.example.coffee_manager.View.CommonTopBar



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAdminScreen(navController: NavController) {
    val featureSections = listOf(
        FeatureSection(
            sectionTitle = "Quản lý nhân viên",
            items = listOf(
                FeatureItem(
                    "Thêm nhân viên",
                    { Icon(Icons.Default.PersonAdd, null) },
                    "add_employee"
                ),
                FeatureItem("Danh sách NV", { Icon(Icons.Default.List, null) }, "user_list")
            )
        ),
        FeatureSection(
            sectionTitle = "Quản lý thực đơn",
            items = listOf(
                FeatureItem("Thêm thực đơn", { Icon(Icons.Default.Fastfood, null) }, "add_food"),
                FeatureItem(
                    "Danh sách thực đơn",
                    { Icon(Icons.Default.ViewList, null) },
                    "food_list"
                ),
                FeatureItem("Danh mục", { Icon(Icons.Default.Category, null) }, "category_list")
            )
        ),
        FeatureSection(
            sectionTitle = "Quản lý không gian",
            items = listOf(
                FeatureItem(
                    "Danh sách bàn",
                    { Icon(Icons.Default.TableRestaurant, null) },
                    "table_list"
                )
            )
        ),
        FeatureSection(
            sectionTitle = "Quản lý doanh thu",
            items = listOf(
                FeatureItem(
                    "Doanh thu",
                    { Icon(Icons.Default.AttachMoney, null) },
                    "statistics"
                ),
                FeatureItem("Hóa đơn", { Icon(Icons.Default.ReceiptLong, null) }, "bill_list")
            )
        )
    )
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
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Spacer(Modifier.height(50.dp))
            featureSections.forEach { section ->
                Text(section.sectionTitle, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                val horizontalScroll = rememberScrollState()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScroll),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    section.items.forEach { item ->
                        Card(
                            modifier = Modifier
                                .width(140.dp)
                                .clickable { navController.navigate(item.route) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                item.icon()
                                Spacer(Modifier.height(8.dp))
                                Text(item.title, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
