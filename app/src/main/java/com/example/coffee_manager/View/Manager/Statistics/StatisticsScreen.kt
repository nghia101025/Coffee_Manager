// View/Statistics/StatisticsScreen.kt
package com.example.coffee_manager.View.Statistics

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Period
import com.example.coffee_manager.Controller.StatisticsController
import com.example.coffee_manager.View.CommonTopBar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(navController: NavController) {
    val controller = remember { StatisticsController() }
    var selectedPeriod by remember { mutableStateOf(Period.DAY) }
    var data by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()


    // Load data on period change
    LaunchedEffect(selectedPeriod) {
        isLoading = true
        scope.launch {
            data = controller.fetchRevenue(selectedPeriod)
            isLoading = false
        }
    }

    // Currency formatter
    val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN")).apply { maximumFractionDigits = 0 }

    Scaffold(
        topBar = {
            CommonTopBar(navController, title = "Thanh toán")
        },
        bottomBar = {
            // Placeholder bottom button
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period selector
            PeriodDropdown(selectedPeriod) { selectedPeriod = it }

            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp)) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            } else {
                // Summary cards
                val total = data.values.sum()
                val discount = total * 0.06 // giả sử giảm 6% hoặc compute if you have
                val revenue = total - discount
                SummaryRow(fmt.format(total), fmt.format(discount), fmt.format(revenue))

                // Chart
                Sparkline(data)
            }
        }
    }
}

@Composable
fun PeriodDropdown(
    selectedPeriod: Period,
    onPeriodSelected: (Period) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val labels = mapOf(
        Period.DAY to "Ngày",
        Period.WEEK to "Tuần",
        Period.MONTH to "Tháng",
        Period.QUARTER to "Quý",
        Period.YEAR to "Năm"
    )
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(labels[selectedPeriod] ?: selectedPeriod.name)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            labels.forEach { (period, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onPeriodSelected(period)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SummaryRow(total: String, discount: String, revenue: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoRow("Tổng", total)
        InfoRow("Giảm giá", discount)
        InfoRow("Doanh thu", revenue)
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value + "đ", style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun Sparkline(data: Map<String, Double>) {
    val entries = data.values.toList().ifEmpty { listOf(0.0, 0.0) }
    val minVal = entries.minOrNull() ?: 0.0
    val maxVal = entries.maxOrNull() ?: 0.0
    val range = (maxVal - minVal).takeIf { it != 0.0 } ?: 1.0
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(surfaceVariantColor)
            .padding(8.dp)
    ) {
        val w = size.width
        val h = size.height
        val stepX = if (entries.size > 1) w / (entries.size - 1) else w

        val path = Path().apply {
            entries.forEachIndexed { idx, v ->
                val x = idx * stepX
                val y = h - ((v - minVal) / range * h).toFloat()
                if (idx == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(
            path,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        entries.forEachIndexed { idx, v ->
            val x = idx * stepX
            val y = h - ((v - minVal) / range * h).toFloat()
            drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = Offset(x, y))
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        data.keys.forEach { label ->
            Text(label, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
    }
}