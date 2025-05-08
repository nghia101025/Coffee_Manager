package com.example.coffee_manager.Controller.Admin

import com.example.coffee_manager.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class EmployeeController {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    /**
     * Lấy danh sách tất cả nhân viên.
     */
    suspend fun getAllEmployee(): Result<List<User>> {
        return try {
            val snap = usersCollection.get().await()
            val docRef = usersCollection.document()
            val list = snap.documents.mapNotNull { doc ->
                runCatching {
                    // Bắt đúng tên field và kiểu
                    val id = doc.getString("idUser") ?: ""
                    val email  = doc.getString("email") ?: ""
                    val name   = doc.getString("name") ?: ""
                    val role   = doc.getString("role") ?: ""
                    val age    = doc.getLong("age")?.toInt() ?: 0
                    val phone  = doc.getString("phone") ?: ""
                    User(
                        idUser = id,
                        email  = email,
                        password = "",   // không lấy password
                        name   = name,
                        age    = age,
                        phone  = phone,
                        role   = role
                    )
                }.getOrNull()
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEmployeeById(idUser: String): Result<User> {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("idUser", idUser)
                .get()
                .await()
            val doc = snapshot.documents.firstOrNull()
                ?: return Result.failure(Exception("Không tìm thấy nhân viên với id $idUser"))
            val user = doc.toObject(User::class.java)
                ?: return Result.failure(Exception("Lỗi chuyển đổi dữ liệu"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


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
    suspend fun addEmployee(user: User): Result<String> {
        return try {
            val docRef = usersCollection.document()
            val foodWithId = user.copy(idUser = docRef.id)
            docRef.set(foodWithId).await()
            Result.success("Thêm nhân viên thành công")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Xóa nhân viên có idUser = [idUser].
     */
    suspend fun deleteEmployee(idUser: String): Result<String> {
        return try {
            // Query tìm document có field idUser == idUser
            val querySnapshot = usersCollection
                .whereEqualTo("idUser", idUser)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                Result.failure(Exception("Không tìm thấy nhân viên với idUser = $idUser"))
            } else {
                // Xóa tất cả các document (thường chỉ có 1 bản ghi)
                querySnapshot.documents.forEach { doc ->
                    usersCollection.document(doc.id).delete().await()
                }
                Result.success("Xóa nhân viên thành công")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cập nhật thông tin nhân viên.
     * Toàn bộ fields trong [user] sẽ overwrite document hiện tại.
     */
    suspend fun updateEmployee(user: User): Result<String> {
        return try {
            // Tìm document theo idUser
            val querySnapshot = usersCollection
                .whereEqualTo("idUser", user.idUser)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                Result.failure(Exception("Không tìm thấy nhân viên với idUser = ${user.idUser}"))
            } else {
                // Lấy ID của document Firestore
                val docId = querySnapshot.documents.first().id
                // Ghi đè toàn bộ User mới lên document
                usersCollection
                    .document(docId)
                    .set(user)
                    .await()

                Result.success("Cập nhật nhân viên thành công")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}