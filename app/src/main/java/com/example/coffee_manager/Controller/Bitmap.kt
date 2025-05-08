package com.example.coffee_manager.Controller

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import android.util.Base64


fun Bitmap.toBase64(quality: Int = 50): String {
    val outputStream = ByteArrayOutputStream()
    // Nén ảnh với JPEG và chất lượng chỉ 50%
    this.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}
fun base64ToBitmap(base64Str: String): Bitmap {
    val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
}


