package com.example.coffee_manager.Controller.Cashier

import com.example.coffee_manager.Model.Table
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TableController {
    private val db = FirebaseFirestore.getInstance()
    private val tables = db.collection("tables")

    // Gán bill lên bàn + set status = OCCUPIED
    suspend fun assignBillToTable(tableNumber: Int, billId: String): Result<Unit> = runCatching {
        val docId = tableNumber.toString()
        tables.document(docId)
            .update(
                mapOf(
                    "currentBillId" to billId,
                    "status" to "OCCUPIED"
                )
            ).await()
    }

    // Khi checkout xong, clear bàn về EMPTY
    suspend fun clearTable(tableId: String): Result<Unit> = runCatching {
        tables.document(tableId)
            .update(
                mapOf(
                    "status" to Table.Status.EMPTY.name,
                    "currentBillId" to null
                )
            )
            .await()
    }

    suspend fun getAllTables(): Result<List<Table>> = runCatching {
        tables.get().await().documents.mapNotNull { it.toObject(Table::class.java) }
    }

    suspend fun getTable(tableNumber: Int): Result<Table> = runCatching {
        val doc = tables.document(tableNumber.toString()).get().await()
        doc.toObject(Table::class.java)
            ?: throw Exception("Bàn không tồn tại")
    }
}
