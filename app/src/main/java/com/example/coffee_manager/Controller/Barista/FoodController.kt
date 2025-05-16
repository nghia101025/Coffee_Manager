package com.example.coffee_manager.Controller.Barista

import com.example.coffee_manager.Model.Food
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FoodController {
    private val db = FirebaseFirestore.getInstance()
    private val foods = db.collection("foods")

    /** Fetch all foods */
    suspend fun getAllFoods(): Result<List<Food>> = runCatching {
        val snap = foods.get().await()
        snap.documents.mapNotNull { it.toObject(Food::class.java) }
    }

    /** Update availability for one food */
    suspend fun updateAvailability(foodId: String, isAvailable: Boolean): Result<Unit> = runCatching {
        foods.document(foodId)
            .update("available", isAvailable)
            .await()
    }
}
