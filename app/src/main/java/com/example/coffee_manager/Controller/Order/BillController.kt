// Controller/BillController.kt
package com.example.coffee_manager.Controller.Order

import com.example.coffee_manager.Model.Bill
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BillController {
    private val col = FirebaseFirestore.getInstance().collection("bills")

    suspend fun createBill(bill: Bill): Result<String> {
        return try {
            // Tạo document mới với ID tự động
            val docRef = col.document()
            val newBill = bill.copy(idBill = docRef.id)
            docRef.set(newBill).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
