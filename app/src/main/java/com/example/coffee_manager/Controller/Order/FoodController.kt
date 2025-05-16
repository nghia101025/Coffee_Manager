package com.example.coffee_manager.Controller.Order

import com.example.coffee_manager.Model.Food
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FoodController {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val foodsCollection = db.collection("foods")
    private val categoriesCollection = db.collection("categories")

    /**
     * Lấy danh sách tất cả món ăn.
     */
    suspend fun getAllFoods(): Result<List<Food>> {
        return try {
            val snap = foodsCollection.get().await()
            val list = snap.documents.mapNotNull { doc ->
                runCatching {
                    // Ví dụ dùng imageUrl; nếu bạn dùng imageBase64 thì thay getString("imageUrl") thành getString("imageBase64")
                    val id = doc.id
                    val name = doc.getString("name") ?: ""
                    val recipe = doc.getString("recipe") ?: ""
                    val price = doc.getLong("price") ?: 0L
                    val avail = doc.getBoolean("available") ?: true
                    val url = doc.getString("imageUrl") ?: ""
                    val category = doc.getString("category") ?: ""

                    Food(
                        idFood = id,
                        name = name,
                        recipe = recipe,
                        price = price,
                        available = avail,
                        imageUrl = url,
                        category = category
                    )
                }.getOrNull()
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun incrementSoldCount(idFood: String, quantity: Int): Result<Unit> = runCatching {
        val docRef = foodsCollection.document(idFood)
        db.runTransaction { tx ->
            val snap = tx.get(docRef)
            val current = (snap.getLong("soldCount") ?: 0L)
            tx.update(docRef, "soldCount", current + quantity)
        }.await()
    }
    suspend fun getFoodById(idFood: String): Result<Food> {
        return try {
            val snapshot = foodsCollection
                .whereEqualTo("idFood", idFood)
                .get()
                .await()
            val doc = snapshot.documents.firstOrNull()
                ?: return Result.failure(Exception("Không tìm thấy nhân viên với id $idFood"))
            val user = doc.toObject(Food::class.java)
                ?: return Result.failure(Exception("Lỗi chuyển đổi dữ liệu"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}