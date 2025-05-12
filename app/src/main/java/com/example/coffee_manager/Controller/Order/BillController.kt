// Controller/Order/BillController.kt
package com.example.coffee_manager.Controller.Order

import com.example.coffee_manager.Model.CartItem
import com.example.coffee_manager.Model.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BillController {
    private val db = FirebaseFirestore.getInstance()
    private val carts = db.collection("carts")

    /**
     * Lấy toàn bộ CartItem của user hiện tại.
     */
    suspend fun getCart(): Result<List<CartItem>> = runCatching {
        val uid = SessionManager.currentUserId
            .takeIf { it.isNotBlank() } ?: throw Exception("Chưa đăng nhập")
        val snap = carts.whereEqualTo("userId", uid).get().await()
        snap.documents.mapNotNull { doc ->
            doc.toObject(CartItem::class.java)
                ?.copy(id = doc.id)
        }
    }

    /**
     * Thêm hoặc cập nhật số lượng trong giỏ cho user hiện tại.
     */
    suspend fun addToCart(foodId: String, quantity: Int): Result<Unit> = runCatching {
        val uid = SessionManager.currentUserId
            .takeIf { it.isNotBlank() } ?: throw Exception("Chưa đăng nhập")

        // Tìm xem đã có item cùng userId & foodId chưa
        val existing = carts
            .whereEqualTo("userId", uid)
            .whereEqualTo("foodId", foodId)
            .get()
            .await()

        if (existing.isEmpty) {
            // thêm mới
            val item = CartItem(
                id = "",          // Firestore tự sinh ID
                userId = uid,
                foodId = foodId,
                quantity = quantity
            )
            carts.add(item).await()
        } else {
            // cập nhật quantity
            val docId = existing.documents.first().id
            // cộng dồn số lượng
            val oldQty = existing.documents.first().getLong("quantity") ?: 0L
            val newQty = oldQty + quantity
            carts.document(docId)
                .update("quantity", newQty)
                .await()
        }
    }

    /**
     * Xóa một món khỏi giỏ theo foodId của user hiện tại.
     */
    suspend fun removeFromCart(foodId: String): Result<Unit> = runCatching {
        val uid = SessionManager.currentUserId
            .takeIf { it.isNotBlank() } ?: throw Exception("Chưa đăng nhập")
        val snap = carts
            .whereEqualTo("userId", uid)
            .whereEqualTo("foodId", foodId)
            .get()
            .await()
        snap.documents.forEach { doc ->
            carts.document(doc.id).delete().await()
        }
    }
    /**
     * Cập nhật số lượng (quantity) cho món đã có trong giỏ.
     */
    suspend fun updateQuantity(foodId: String, newQuantity: Int): Result<Unit> = runCatching {
        val uid = SessionManager.currentUserId
            .takeIf { it.isNotBlank() } ?: throw Exception("Chưa đăng nhập")
        // tìm document phù hợp
        val snap = carts
            .whereEqualTo("userId", uid)
            .whereEqualTo("foodId", foodId)
            .get()
            .await()
        if (snap.isEmpty) throw Exception("Món chưa tồn tại trong giỏ")
        // thường chỉ có 1 doc
        val docId = snap.documents.first().id
        carts.document(docId)
            .update("quantity", newQuantity)
            .await()
    }
}
