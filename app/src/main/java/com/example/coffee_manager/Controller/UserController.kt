package com.example.coffee_manager.Controller

import android.util.Log
import com.example.coffee_manager.Model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

object UserController {
    private val users = mutableListOf<User>()
    private val db = FirebaseFirestore.getInstance()

    fun getAllUsers(
        onSuccess: (List<User>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("Users")
            .get()
            .addOnSuccessListener { result ->
                val userList = result.mapNotNull { doc ->
                    try {
                        User(
                            IdUser = (doc["IdUser"] as? Long)?.toInt() ?: 0,
                            email = doc["email"] as? String ?: "",
                            password = doc["password"] as? String ?: "",
                            name = doc["name"] as? String ?: "",
                            age = (doc["age"] as? Long)?.toInt() ?: 0,
                            address = doc["address"] as? String ?: "",
                            role = doc["role"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        null // Bỏ qua nếu lỗi parse
                    }
                }
                onSuccess(userList)
            }
            .addOnFailureListener {
                onError(it.message ?: "Lỗi không xác định")
            }
    }


    // Đăng ký người dùng
    fun registerUser(
        user: User,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Giả sử việc đăng ký người dùng thành công
            users.add(user)  // Thêm người dùng vào danh sách
            onSuccess()
        } catch (e: Exception) {
            Log.e("UserController", "Error registering user: ${e.message}")
            onError("Đã xảy ra lỗi khi đăng ký người dùng.")
        }
    }

    // Xóa người dùng
    fun deleteUser(
        userId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val user = users.find { it.IdUser == userId }
            if (user != null) {
                users.remove(user) // Xóa người dùng
                onSuccess()
            } else {
                onError("Người dùng không tồn tại.")
            }
        } catch (e: Exception) {
            Log.e("UserController", "Error deleting user: ${e.message}")
            onError("Đã xảy ra lỗi khi xóa người dùng.")
        }
    }
    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Kiểm tra đăng nhập từ Firebase hoặc bất kỳ nơi nào bạn lưu trữ thông tin người dùng
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("email", email)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onError("Email hoặc mật khẩu không đúng.")
                } else {
                    onSuccess() // Nếu tìm thấy user hợp lệ, gọi onSuccess
                }
            }
            .addOnFailureListener { exception ->
                onError("Lỗi khi đăng nhập: ${exception.message}")
            }
    }


}
