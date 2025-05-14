package com.example.coffee_manager.Controller.Order

import com.example.coffee_manager.Model.Table
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TableController {
    private val tablesCollection = FirebaseFirestore.getInstance().collection("tables")

    suspend fun getAllTables(): Result<List<Table>> {
        return try {
            val snapshot = tablesCollection.get().await()
            val list = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Table::class.java)?.copy(idTable = doc.id)
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    /** Đọc status của bàn. Nếu không tồn tại, trả "EMPTY" */
    suspend fun getTableStatus(idTable: String): String {
        val doc = tablesCollection.document(idTable.toString()).get().await()
        if (!doc.exists()) return "EMPTY"
        return doc.getString("status")
            ?: throw Exception("Bàn $idTable thiếu field 'status'")
    }


    suspend fun updateTableStatus(idTable: String, newStatus: String,idBill: String): Result<Unit> = runCatching {
        val docRef = tablesCollection.document(idTable.toString())
        // đảm bảo document có trước khi update
        val snap = docRef.get().await()
        docRef.update(
            mapOf(
                "status"        to newStatus,
                "currentBillId" to idBill
            )
        ).await()

    }
}