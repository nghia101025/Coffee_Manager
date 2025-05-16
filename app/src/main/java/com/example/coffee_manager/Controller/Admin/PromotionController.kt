package com.example.coffee_manager.Controller.Admin

import com.example.coffee_manager.Model.Promotion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class PromotionController {
    private val db = FirebaseFirestore.getInstance()
    private val col = db.collection("promotions")

    suspend fun getAllPromotions(): Result<List<Promotion>> = runCatching {
        val snap = col.get().await()
        snap.documents.mapNotNull { doc ->
            doc.toObject(Promotion::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun addPromotion(code: String, percent: Int, expiresAt: Date): Result<String> = runCatching {
        val promo = Promotion(code = code, discountPercent = percent, expiresAt = expiresAt)
        val ref = col.add(promo).await()
        ref.id
    }

    suspend fun updatePromotion(promo: Promotion): Result<Unit> = runCatching {
        require(promo.id.isNotBlank())
        col.document(promo.id)
            .set(promo.copy(id = ""))
            .await()
    }

    suspend fun deletePromotion(id: String): Result<Unit> = runCatching {
        col.document(id).delete().await()
    }

}
