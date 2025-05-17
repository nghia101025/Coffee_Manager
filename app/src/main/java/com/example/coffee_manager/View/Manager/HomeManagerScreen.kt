// View/Manager/HomeAdminScreen.kt
package com.example.coffee_manager.View.Manager

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Model.FeatureItem
import com.example.coffee_manager.Model.FeatureSection
import com.example.coffee_manager.R
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAdminScreen(navController: NavController) {
    val TAG = "HomeAdmin"
    val featureSections = listOf(
        FeatureSection(
            sectionTitle = "Quản lý nhân viên",
            items = listOf(
                FeatureItem("Thêm nhân viên", { Icon(Icons.Default.PersonAdd, null) }, "add_employee"),
                FeatureItem("Danh sách NV", { Icon(Icons.Default.List, null) }, "user_list")
            )
        ),
        FeatureSection(
            sectionTitle = "Quản lý thực đơn",
            items = listOf(
                FeatureItem("Thêm thực đơn", { Icon(Icons.Default.Fastfood, null) }, "add_food"),
                FeatureItem("DS thực đơn", { Icon(Icons.Default.ViewList, null) }, "food_list"),
                FeatureItem("Danh mục", { Icon(Icons.Default.Category, null) }, "category_list")
            )
        ),
        FeatureSection(
            sectionTitle = "Quản lý không gian",
            items = listOf(
                FeatureItem("Danh sách bàn", { Icon(Icons.Default.TableRestaurant, null) }, "table_list")
            )
        ),
        FeatureSection(
            sectionTitle = "Quản lý doanh thu",
            items = listOf(
                FeatureItem("Doanh thu", { Icon(Icons.Default.AttachMoney, null) }, "statistics"),
                FeatureItem("Hóa đơn", { Icon(Icons.Default.ReceiptLong, null) }, "bill_list"),
                FeatureItem("Khuyến mãi", { Icon(Icons.Default.LocalOffer, null) }, "promotions"),
                FeatureItem("QR", { Icon(Icons.Default.QrCodeScanner, null) }, "qr")
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu quản trị") },
                actions = {
                    IconButton(onClick = {
                        try {
                            navController.navigate("profile")
                        } catch (e: Exception) {
                            Log.e(TAG, "Unknown route: profile", e)
                        }
                    }) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_account),
                            contentDescription = "User Profile",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        // Phải dùng innerPadding đây chứ không phải bỏ qua
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            featureSections.forEach { section ->
                Text(section.sectionTitle, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))

                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    section.items.forEach { item ->
                        Card(
                            modifier = Modifier
                                .width(140.dp)
                                .clickable {
                                    try {
                                        navController.navigate(item.route)
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Unknown route: ${item.route}", e)
                                    }
                                },
                            elevation = CardDefaults.cardElevation(4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
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
