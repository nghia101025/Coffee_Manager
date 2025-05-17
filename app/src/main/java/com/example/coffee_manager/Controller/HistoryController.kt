package com.example.coffee_manager.Controller

import com.example.coffee_manager.Model.History
import com.example.coffee_manager.Model.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HistoryController {
    private val db = FirebaseFirestore.getInstance()
    private val col = db.collection("history")

    /** Thêm 1 mục history mới cho người dùng hiện tại */
    suspend fun addHistory(action: String): Result<String> = runCatching {
        val userId = SessionManager.currentUserId
        val docRef = col.document()
        val h = History(
            id = docRef.id,
            userId = userId,
            action = action,
            timestamp = System.currentTimeMillis()
        )
        docRef.set(h).await()
        docRef.id
    }

    suspend fun getHistoryForCurrentUser(): Result<List<History>> = runCatching {
        val currentUserId = SessionManager.currentUserId
        // 1) Lấy tất cả bản ghi của user này (không orderBy)
        val snapshot = col
            .whereEqualTo("userId", currentUserId)
            .get()
            .await()

        // 2) Chuyển về model và tự sort client‑side theo timestamp desc
        val list = snapshot.documents.mapNotNull { doc ->
            doc.toObject(History::class.java)
        }.sortedByDescending { it.timestamp }

        list
    }
}
