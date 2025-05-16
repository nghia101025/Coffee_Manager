package com.example.coffee_manager.Controller.Brewing

import com.example.coffee_manager.Model.Table
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TableController {
    private val tablesCollection = FirebaseFirestore.getInstance().collection("tables")

    /**
     * Lấy thông tin bàn theo ID
     */
    suspend fun getTableById(idTable: String): Result<Table> {
        return try {
            val doc = tablesCollection.document(idTable).get().await()
            val table = doc.toObject(Table::class.java)
                ?.copy(idTable = doc.id)
                ?: return Result.failure(Exception("Không tìm thấy bàn với id $idTable"))
            Result.success(table)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}