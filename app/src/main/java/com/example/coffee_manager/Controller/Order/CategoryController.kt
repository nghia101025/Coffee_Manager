package com.example.coffee_manager.Controller.Order

import com.example.coffee_manager.Model.Category
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CategoryController{
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

}