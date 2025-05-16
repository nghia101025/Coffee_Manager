// View/Payment/QrScreen.kt
package com.example.coffee_manager.View.Manager.Qr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.Controller.Admin.QrController
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import android.provider.MediaStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScreen(navController: NavController) {
    val controller = remember { QrController() }
    var base64 by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Launcher để pick image
    val picker = rememberLauncherForActivityResult(GetContent()) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                    val out = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                    val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                    controller.uploadQr(b64)
                        .onSuccess {
                            base64 = b64
                            message = "Upload thành công"
                        }
                        .onFailure {
                            message = "Lỗi upload: ${it.message}"
                        }
                } catch (e: Exception) {
                    message = "Không thể đọc ảnh"
                }
            }
        }
    }

    // Fetch QR khi vào screen
    LaunchedEffect(Unit) {
        controller.fetchQrBase64()
            .onSuccess { fetched ->
                // nếu chưa có QR thì fetched sẽ null hoặc empty
                if (!fetched.isNullOrBlank()) {
                    base64 = fetched
                }
            }
            .onFailure {
                // chỉ log, không hiện message nếu doc không tồn tại
                message = "Lỗi lấy QR: ${it.message}"
            }
        loading = false
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("QR chuyển khoản") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                }
            },
        )
    }) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator()
            } else if (base64 != null) {
                // Hiển thị QR và nút đổi
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "QR chuyển khoản",
                        modifier = Modifier.size(256.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { picker.launch("image/*") }) {
                        Text("Đổi QR")
                    }
                    message?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                // Chưa có QR: chỉ hiện upload
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Chưa có QR chuyển khoản nào", Modifier.padding(bottom = 16.dp))
                    Button(onClick = { picker.launch("image/*") }) {
                        Text("Upload QR")
                    }
                    message?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
