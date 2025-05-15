package com.example.coffee_manager.Controller.Admin

import com.example.coffee_manager.Model.Bill
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BillController {
    private val db = FirebaseFirestore.getInstance()
    private val billsCol = db.collection("bills")

    /**
     * Fetch all bills, ordered by createdAt descending.
     */
    suspend fun getAllBills(): Result<List<Bill>> = runCatching {
        val snapshot = billsCol
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        snapshot.documents.mapNotNull { doc ->
            doc.toObject(Bill::class.java)
                ?.copy(idBill = doc.id)
        }
    }

    /**
     * Fetch one bill by ID.
     */
    suspend fun getBillById(id: String): Result<Bill> = runCatching {
        val doc = billsCol.document(id).get().await()
        doc.toObject(Bill::class.java)
            ?.copy(idBill = doc.id)
            ?: throw Exception("Bill not found")
    }
}
