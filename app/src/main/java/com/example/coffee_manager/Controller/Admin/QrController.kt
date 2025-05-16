package com.example.coffee_manager.Controller.Admin

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class QrController {
    private val db = FirebaseFirestore.getInstance()
    private val qrDoc = db.collection("config").document("qr")

    suspend fun fetchQrBase64(): Result<String?> = runCatching {
        val snap = qrDoc.get().await()
        if (!snap.exists()) return@runCatching null
        snap.getString("imageUrl")
    }

    suspend fun uploadQr(base64: String): Result<Unit> = runCatching {
        qrDoc.set(mapOf("imageUrl" to base64)).await()
    }
}
