package com.example.coffee_manager.Controller.Order

import com.example.coffee_manager.Model.Promotion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PromotionController {
    private val db = FirebaseFirestore.getInstance()
    private val promos = db.collection("promotions")

    /**
     * Lấy Promotion theo code; ném Exception nếu không tìm thấy.
     */
    suspend fun getByCode(code: String): Result<Promotion> = runCatching {
        val snap = promos.whereEqualTo("code", code).get().await()
        if (snap.isEmpty) throw Exception("Mã không tồn tại")
        snap.documents.first().toObject(Promotion::class.java)
            ?: throw Exception("Không thể parse Promotion")
    }
}
