package com.example.coffee_manager.Controller.Admin

import com.example.coffee_manager.Model.Category
import com.example.coffee_manager.utils.MaterialIconMap
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CategoryController {
    private val db = FirebaseFirestore.getInstance()
    private val categories = db.collection("categories")

    /**
     * Lấy danh sách tất cả danh mục, bao gồm cả iconName.
     */
    suspend fun getAllCategories(): Result<List<Category>> {
        return try {
            val snapshot = categories.get().await()
            val result = snapshot.documents.mapNotNull { doc ->
                val id       = doc.id
                val name     = doc.getString("name")
                if (name != null) Category(idCat = id, name = name)
                else null
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Thêm danh mục mới kèm iconName.
     */
    suspend fun addCategory(name: String, iconName: String): Result<String> {
        return try {
            // Kiểm tra trùng tên
            val check = categories.whereEqualTo("name", name).get().await()
            if (!check.isEmpty) {
                return Result.failure(Exception("Danh mục đã tồn tại"))
            }

            // Tạo document mới với cả iconName
            val newDoc = categories.document()
            val data = hashMapOf(
                "id"       to newDoc.id,
                "name"     to name,
            )
            newDoc.set(data).await()
            Result.success(newDoc.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Xóa danh mục theo id.
     */
    suspend fun deleteCategory(id: String): Result<Unit> {
        return try {
            categories.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
