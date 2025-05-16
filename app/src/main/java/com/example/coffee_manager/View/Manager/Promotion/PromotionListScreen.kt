// View/Manager/Promotion/PromotionListScreen.kt
package com.example.coffee_manager.View.Manager.Promotion

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Admin.PromotionController
import com.example.coffee_manager.Model.Promotion
import com.example.coffee_manager.View.Admin.PromotionEditDialog
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionListScreen(navController: NavController) {
    val controller = remember { PromotionController() }
    var promotions by remember { mutableStateOf<List<Promotion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // trạng thái dialog
    var editingPromo by remember { mutableStateOf<Promotion?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    // load danh sách
    LaunchedEffect(Unit) {
        controller.getAllPromotions()
            .onSuccess { promotions = it }
            .onFailure { errorMsg = it.message }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Khuyến mãi") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home_admin") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingPromo = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm khuyến mãi")
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
                return@Box
            }
            if (errorMsg != null) {
                Text(
                    errorMsg!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                return@Box
            }

            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(promotions, key = { it.id }) { promo ->
                    Card(
                        Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(promo.code, style = MaterialTheme.typography.titleMedium)
                                Text("Giảm ${promo.discountPercent}%", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Hết hạn: ${
                                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                            .format(promo.expiresAt)
                                    }",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row {
                                IconButton(onClick = {
                                    editingPromo = promo
                                    showDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Sửa")
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        controller.deletePromotion(promo.id)
                                            .onSuccess {
                                                promotions = promotions.filterNot { it.id == promo.id }
                                            }
                                            .onFailure {
                                                // TODO: show error
                                            }
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Xóa")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            PromotionEditDialog(
                initial = editingPromo,
                onDismiss = { showDialog = false },
                onSave = { code, percent, expires ->
                    showDialog = false
                    scope.launch {
                        if (editingPromo == null) {
                            // thêm mới
                            controller.addPromotion(code, percent, expires)
                                .onSuccess {
                                    // reload
                                    controller.getAllPromotions()
                                        .onSuccess { promotions = it }
                                }
                                .onFailure {
                                    // TODO: thông báo lỗi
                                }
                        } else {
                            // cập nhật
                            val updated = editingPromo!!.copy(
                                code = code,
                                discountPercent = percent,
                                expiresAt = expires
                            )
                            controller.updatePromotion(updated)
                                .onSuccess {
                                    controller.getAllPromotions()
                                        .onSuccess { promotions = it }
                                }
                                .onFailure {
                                    // TODO: lỗi
                                }
                        }
                    }
                }
            )
        }
    }
}
