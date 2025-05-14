package com.example.coffee_manager.Controller.Admin

import com.example.coffee_manager.Model.Category
import com.example.coffee_manager.Model.Food
import com.example.coffee_manager.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class FoodController {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val foodsCollection = db.collection("foods")
    private val categoriesCollection = db.collection("categories")


    /**
     * Thêm món ăn mới vào Firestore.
     */
    suspend fun addFood(food: Food): Result<String> {
        return try {
            val docRef = foodsCollection.document()
            val foodWithId = food.copy(idFood = docRef.id)
            docRef.set(foodWithId).await()
            Result.success("Thêm món ăn thành công")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


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
                    val avail = doc.getBoolean("isAvailable") ?: true
                    val url = doc.getString("imageUrl") ?: ""
                    val category = doc.getString("category") ?: ""

                    Food(
                        idFood = id,
                        name = name,
                        recipe = recipe,
                        price = price,
                        isAvailable = avail,
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

    /**
     * Cập nhật thông tin món ăn theo idFood.
     */
    suspend fun updateFood(
        idFood: String,
        updated: Map<String, Any>
    ): Result<String> {
        return try {
            foodsCollection.document(idFood).update(updated).await()
            Result.success("Cập nhật món ăn thành công")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Xóa món ăn theo idFood.
     */
    suspend fun deleteFood(idFood: String): Result<String> {
        return try {
            foodsCollection.document(idFood).delete().await()
            Result.success("Xóa món ăn thành công")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cập nhật thông tin nhân viên.
     * Toàn bộ fields trong [food] sẽ overwrite document hiện tại.
     */
    suspend fun updateFood(food: Food): Result<String> {
        return try {
            // Tìm document theo idUser
            val querySnapshot = foodsCollection
                .whereEqualTo("idFood", food.idFood)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                Result.failure(Exception("Không tìm thấy nhân viên với idFood = ${food.idFood}"))
            } else {
                // Lấy ID của document Firestore
                val docId = querySnapshot.documents.first().id
                // Ghi đè toàn bộ User mới lên document
                foodsCollection
                    .document(docId)
                    .set(food)
                    .await()

                Result.success("Cập nhật món ăn thành công")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    /**
     * Lấy danh sách tất cả danh mục Category.
     */
    suspend fun getAllCategories(): Result<List<Category>> {
        return try {
            // Fetch categories from Firestore and map them to Category objects
            val querySnapshot: QuerySnapshot = categoriesCollection.get().await()
            val categories = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Category::class.java)
            }
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }





}