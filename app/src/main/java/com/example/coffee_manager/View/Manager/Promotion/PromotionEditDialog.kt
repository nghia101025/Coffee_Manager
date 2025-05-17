// PromotionEditDialog.kt
package com.example.coffee_manager.View.Admin

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.coffee_manager.Controller.HistoryController
import com.example.coffee_manager.Model.Promotion
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PromotionEditDialog(
    initial: Promotion?,
    onDismiss: () -> Unit,
    onSave: (code: String, percent: Int, expires: Date) -> Unit
) {
    val ctx = LocalContext.current
    var code by remember { mutableStateOf(initial?.code ?: "") }
    var percentText by remember { mutableStateOf(initial?.discountPercent?.toString() ?: "") }
    var expiresAt by remember { mutableStateOf(initial?.expiresAt ?: Date()) }
    val calendar = Calendar.getInstance().also { it.time = expiresAt }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Thêm khuyến mãi" else "Sửa khuyến mãi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Mã khuyến mãi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = percentText,
                    onValueChange = { percentText = it.filterDigits() },
                    label = { Text("% giảm giá") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(onClick = {
                    DatePickerDialog(
                        ctx,
                        { _: DatePicker, y, m, d ->
                            calendar.set(y, m, d)
                            expiresAt = calendar.time
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Text(
                        "Hết hạn: ${
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .format(expiresAt)
                        }"
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val pct = percentText.toIntOrNull() ?: 0
                onSave(code.trim(), pct, expiresAt)
            }) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

private fun String.filterDigits(): String = filter { it.isDigit() }
