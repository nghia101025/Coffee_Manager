package com.example.coffee_manager.Controller

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import android.util.Base64
import android.util.Log

// Extension function để chuyển Bitmap thành Base64
fun Bitmap.toBase64(quality: Int = 50): String {
    val outputStream = ByteArrayOutputStream()
    // Nén ảnh với JPEG và chất lượng chỉ 50%
    this.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

// Hàm chuyển từ Base64 string thành Bitmap với kiểm tra lỗi và log
fun base64ToBitmap(base64Str: String?): Bitmap? {
    if (base64Str.isNullOrEmpty()) {
        Log.e("Bitmap", "Base64 string is null or empty")
        return null
    }

    return try {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: IllegalArgumentException) {
        Log.e("Bitmap", "Error decoding Base64 string", e)
        null
    }
}
