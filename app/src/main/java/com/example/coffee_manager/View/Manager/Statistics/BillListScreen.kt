// View/Order/BillListScreen.kt
package com.example.coffee_manager.View.Order

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Admin.BillController
import com.example.coffee_manager.Controller.Period
import com.example.coffee_manager.Model.Bill
import com.example.coffee_manager.View.CommonTopBar
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillListScreen(navController: NavController) {
    val controller = remember { BillController() }
    var bills by remember { mutableStateOf(emptyList<Bill>()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedPeriod by remember { mutableStateOf(Period.DAY) }

    LaunchedEffect(Unit) {
        controller.getAllBills()
            .onSuccess { bills = it }
            .onFailure { error = it.message }
        isLoading = false
    }

    Scaffold(
        topBar = { CommonTopBar(navController, title = "Danh sách hóa đơn") }
    ) { contentPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            PeriodDropdown(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { selectedPeriod = it }
            )

            // compute date filter
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)
            val start = when (selectedPeriod) {
                Period.DAY     -> today
                Period.WEEK    -> today.minusDays(6)
                Period.MONTH   -> today.minusMonths(1).plusDays(1)
                Period.QUARTER -> today.minusMonths(3).plusDays(1)
                Period.YEAR    -> today.minusYears(1).plusDays(1)
            }
            val filtered = bills.filter { bill ->
                val dt = Instant.ofEpochMilli(bill.createdAt).atZone(zone).toLocalDate()
                !dt.isBefore(start) && !dt.isAfter(today)
            }

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Lỗi: $error", color = MaterialTheme.colorScheme.error)
                }
                filtered.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Không có hóa đơn", style = MaterialTheme.typography.bodyMedium)
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered) { bill ->
                        BillItemCard(bill, onClick = {
                            navController.navigate("billDetail/${bill.idBill}")
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun BillItemCard(bill: Bill, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    val dateText = sdf.format(Date(bill.createdAt))

    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = bill.idBill,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(text = dateText, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Text(text = bill.note, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (bill.processed) AssistChip(
                        onClick = { /* no-op */ },
                        label = { Text("Đã xử lý") }
                    )
                    if (bill.paid) FilterChip(
                        selected = true,
                        onClick = { /* no-op */ },
                        label = { Text("Đã thanh toán") }
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "%sđ".format(NumberFormat.getNumberInstance(Locale("vi", "VN")).format(bill.totalPrice)),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodDropdown(
    selectedPeriod: Period,
    onPeriodSelected: (Period) -> Unit
) {
    val labelMap = mapOf(
        Period.DAY to "Ngày",
        Period.WEEK to "Tuần",
        Period.MONTH to "Tháng",
        Period.QUARTER to "3 Tháng",
        Period.YEAR to "Năm"
    )
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TextField(
            readOnly = true,
            value = labelMap[selectedPeriod] ?: selectedPeriod.name,
            onValueChange = { },
            label = { Text("Khoảng thời gian") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Period.values().forEach { period ->
                DropdownMenuItem(
                    text = { Text(labelMap[period] ?: period.name) },
                    onClick = {
                        onPeriodSelected(period)
                        expanded = false
                    }
                )
            }
        }
    }
}
