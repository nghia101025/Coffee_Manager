package com.example.coffee_manager.Controller.Admin

import com.example.coffee_manager.Model.Table
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TableController {
    private val tablesCollection = FirebaseFirestore.getInstance().collection("tables")

    /**
     * Lấy danh sách tất cả bàn
     */
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

    /**
     * Thêm bàn mới (id tự sinh)
     */
    suspend fun addTable(table: Table): Result<String> {
        return try {
            // Để Firestore sinh id document
            val docRef = tablesCollection.document()
            val withId = table.copy(idTable = docRef.id)
            docRef.set(withId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cập nhật thông tin bàn
     */
    suspend fun updateTable(table: Table): Result<String> {
        return try {
            val data = table.copy(idTable = "")
            tablesCollection.document(table.idTable)
                .set(table)
                .await()
            Result.success(table.idTable)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTableStatusByNumber(
        number: Int,
        newStatus: Table.Status
    ): Result<String> {
        return try {
            // Tìm document có field "number" = số bàn
            val snapshot = tablesCollection
                .whereEqualTo("number", number)
                .get()
                .await()

            val doc = snapshot.documents.firstOrNull()
                ?: return Result.failure(Exception("Không tìm thấy bàn số $number"))

            // Cập nhật trường status
            tablesCollection
                .document(doc.id)
                .update("status", newStatus.name)
                .await()

            Result.success(doc.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Xóa bàn
     */
    suspend fun deleteTable(idTable: String): Result<String> {
        return try {
            tablesCollection.document(idTable).delete().await()
            Result.success(idTable)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
