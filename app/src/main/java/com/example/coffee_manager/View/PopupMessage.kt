package com.example.coffee_manager.View

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Hiển thị một popup thông báo chung.
 *
 * @param show     Khi true thì hiện dialog, false thì ẩn.
 * @param message  Nội dung thông báo.
 * @param onDismiss Khi người dùng bấm "OK" hoặc bấm ra ngoài sẽ được gọi.
 */
@Composable
fun PopupMessage(
    show: Boolean,
    message: String,
    onDismiss: () -> Unit
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Thông báo") },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
