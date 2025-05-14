// Controller/Order/BillController.kt
package com.example.coffee_manager.Controller.Order

import com.example.coffee_manager.Model.Bill
import com.example.coffee_manager.Model.CartItem
import com.example.coffee_manager.Model.Food
import com.example.coffee_manager.Model.Order
import com.example.coffee_manager.Model.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class BillController {
    private val db = FirebaseFirestore.getInstance()
    private val carts = db.collection("carts")
    private val orders = db.collection("orders")
    private val billsCol = db.collection("bills")


    /** Lấy giỏ hàng */
    suspend fun getCart(): Result<List<CartItem>> = runCatching {
        val uid = SessionManager.currentUserId.ifBlank { throw Exception("Chưa đăng nhập") }
        carts.whereEqualTo("userId", uid).get().await()
            .documents.mapNotNull { it.toObject(CartItem::class.java)?.copy(id = it.id) }
    }

    /** Thêm / cập nhật quantity */
    suspend fun addToCart(foodId: String, quantity: Int): Result<Unit> = runCatching {
        val uid = SessionManager.currentUserId.ifBlank { throw Exception("Chưa đăng nhập") }
        val snap = carts
            .whereEqualTo("userId", uid)
            .whereEqualTo("foodId", foodId)
            .get().await()

        if (snap.isEmpty) {
            carts.add(CartItem(id = "", userId = uid, foodId = foodId, quantity = quantity)).await()
        } else {
            val doc = snap.documents.first()
            val oldQty = doc.getLong("quantity") ?: 0L
            doc.reference.update("quantity", oldQty + quantity).await()
        }
    }

    /** Xóa 1 item */
    suspend fun removeFromCart(foodId: String): Result<Unit> = runCatching {
        val uid = SessionManager.currentUserId.ifBlank { throw Exception("Chưa đăng nhập") }
        carts.whereEqualTo("userId", uid)
            .whereEqualTo("foodId", foodId)
            .get().await()
            .documents.forEach { it.reference.delete().await() }
    }
    suspend fun clearCart(): Result<Unit> = runCatching {
        val uid = SessionManager.currentUserId
            .takeIf { it.isNotBlank() }
            ?: throw Exception("Chưa đăng nhập")
        val snap = carts.whereEqualTo("userId", uid).get().await()
        snap.documents.forEach { doc ->
            carts.document(doc.id).delete().await()
        }
    }

    /** Cập nhật quantity */
    suspend fun updateQuantity(foodId: String, newQuantity: Int): Result<Unit> = runCatching {
        val uid = SessionManager.currentUserId.ifBlank { throw Exception("Chưa đăng nhập") }
        val snap = carts.whereEqualTo("userId", uid)
            .whereEqualTo("foodId", foodId).get().await()
        if (snap.isEmpty) throw Exception("Món chưa có trong giỏ")
        snap.documents.first().reference.update("quantity", newQuantity).await()
    }

    /**
     * Đặt hàng: nhận thêm subtotal, discount, total do UI tính sẵn.
     * Trả về orderId.
     */
    suspend fun placeOrder(
        items: List<CartItem>,
        tableNumber: String,
        promoCode: String,
        paymentMethod: String,
        subtotal: Long,
        discount: Long,
        total: Long
    ): Result<String> = runCatching {
        if (tableNumber.isBlank()) throw Exception("Vui lòng nhập số bàn")
        val uid = SessionManager.currentUserId.ifBlank { throw Exception("Chưa đăng nhập") }

        // Tạo order
        val ref = orders.document()
        val order = Order(
            id = ref.id,
            userId = uid,
            items = items,
            tableNumber = tableNumber,
            promoCode = promoCode,
            paymentMethod = paymentMethod,
            subtotal = subtotal,
            discount = discount,
            total = total,
            timestamp = System.currentTimeMillis()
        )
        ref.set(order).await()

        // Xóa giỏ hàng
        carts.whereEqualTo("userId", uid).get().await()
            .documents.forEach { it.reference.delete().await() }

        ref.id
    }

    /** Lấy lịch sử order */
    suspend fun getOrderHistory(): Result<List<Order>> = runCatching {
        val uid = SessionManager.currentUserId.ifBlank { throw Exception("Chưa đăng nhập") }
        orders.whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get().await()
            .documents.mapNotNull { it.toObject(Order::class.java) }
    }

    /**
     * Tạo mới một Bill và lưu lên Firestore.
     */
    suspend fun createBill(bill: Bill): Result<String> = runCatching {
        // tạo document mới
        val ref = billsCol.document()
        val withId = bill.copy(idBill = ref.id)
        ref.set(withId).await()
        ref.id
    }


}
