package com.example.coffee_manager.Controller

import com.example.coffee_manager.Model.Food
import com.example.coffee_manager.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Controller for admin operations on User accounts (employees).
 */
class AdminController {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val foodsCollection = db.collection("foods")

    /**
     * Kiểm tra xem email đã được đăng ký trong Firebase Auth chưa.
     */
    suspend fun isEmailRegistered(email: String): Boolean {
        return try {
            val methods = auth.fetchSignInMethodsForEmail(email).await().signInMethods
            !methods.isNullOrEmpty()
        } catch (e: FirebaseAuthException) {
            false
        }
    }

    /**
     * Thêm nhân viên mới: tạo tài khoản trong Firebase Auth và lưu chi tiết vào Firestore.
     */
    suspend fun addEmployee(
        email: String,
        password: String,
        name: String,
        age: Int,
        phone: String,
        role: String
    ): Result<String> {
        return try {
            // Tạo user trong Firebase Authentication
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Failed to create user.")

            // Cập nhật displayName
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // Tạo model User và thêm vào Firestore
            val userModel = User(
                idUser = 0, // có thể tự generate hoặc dùng firebaseUser.uid
                email = email,
                password = password,
                name = name,
                age = age,
                phone = phone,
                role = role
            )
            usersCollection.document(firebaseUser.uid)
                .set(userModel)
                .await()

            Result.success("Thêm nhân viên thành công")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lấy danh sách tất cả nhân viên.
     */
    suspend fun getAllEmployees(): List<User> {
        val snapshot = usersCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(User::class.java) }
    }

    /**
     * Cập nhật thông tin nhân viên theo uid.
     */
    suspend fun updateEmployee(
        uid: String,
        updated: Map<String, Any>
    ): Result<String> {
        return try {
            usersCollection.document(uid)
                .update(updated)
                .await()
            Result.success("Cập nhật nhân viên thành công")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Xóa nhân viên: xóa document Firestore và xóa user Auth.
     */
    suspend fun deleteEmployee(uid: String): Result<String> {
        return try {
            // Xóa trong Firestore
            usersCollection.document(uid).delete().await()

            // Xóa trong Authentication
            auth.currentUser?.let {
                if (it.uid == uid) {
                    it.delete().await()
                } else {
                    // Trường hợp xóa user khác không thể dùng currentUser
                    // Cần sử dụng Cloud Functions hoặc Admin SDK
                }
            }
            Result.success("Xóa nhân viên thành công")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    /**
     * Thêm món ăn mới vào Firestore.
     */
    suspend fun addFood(food: Food): Result<String> {
        return try {
            val docRef = foodsCollection.document()
            val foodWithId = food.copy(idFood = docRef.id)
            docRef.set(foodWithId).await()
            Result.success("Thêm món ăn thành công")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * Lấy danh sách tất cả món ăn.
     */
    suspend fun getAllFoods(): Result<List<Food>> {
        return try {
            val snap = foodsCollection.get().await()
            val list = snap.documents.mapNotNull { doc ->
                runCatching {
                    // Ví dụ dùng imageUrl; nếu bạn dùng imageBase64 thì thay getString("imageUrl") thành getString("imageBase64")
                    val id     = doc.id
                    val name   = doc.getString("name") ?: ""
                    val recipe = doc.getString("recipe") ?: ""
                    val price  = doc.getString("price") ?: "0.0"
                    val avail  = doc.getBoolean("isAvailable") ?: true
                    val url    = doc.getString("imageUrl") ?: ""
                    Food(
                        idFood      = id,
                        name        = name,
                        recipe      = recipe,
                        price       = price,
                        isAvailable = avail,
                        imageUrl    = url
                    )
                }.getOrNull()
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    /**
     * Cập nhật thông tin món ăn theo idFood.
     */
    suspend fun updateFood(
        idFood: String,
        updated: Map<String, Any>
    ): Result<String> {
        return try {
            foodsCollection.document(idFood).update(updated).await()
            Result.success("Cập nhật món ăn thành công")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Xóa món ăn theo idFood.
     */
    suspend fun deleteFood(idFood: String): Result<String> {
        return try {
            foodsCollection.document(idFood).delete().await()
            Result.success("Xóa món ăn thành công")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
