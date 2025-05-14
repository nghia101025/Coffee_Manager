package com.example.coffee_manager.Controller.Cashier

import com.example.coffee_manager.Model.Bill
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BillController {
    private val db = FirebaseFirestore.getInstance()
    private val bills = db.collection("bills")

    suspend fun createBill(bill: Bill): Result<String> = runCatching {
        // paid=false, processed=false ban dau
        val doc = bills.document()
        val toSave = bill.copy(
            idBill = doc.id,
            isPaid = false,
            isProcessed = false
        )
        doc.set(toSave).await()
        doc.id
    }

    suspend fun checkoutBill(billId: String): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        bills.document(billId)
            .update(
                mapOf(
                    "isPaid" to true,
                    "isProcessed" to true,
                    "finish" to true,
                    "dateFinish" to now
                )
            )
            .await()
    }

    // Lấy bill theo id
    suspend fun getBill(billId: String): Result<Bill> = runCatching {
        val snap = bills.document(billId).get().await()
        snap.toObject(Bill::class.java)
            ?: throw Exception("Bill không tồn tại")
    }
}
