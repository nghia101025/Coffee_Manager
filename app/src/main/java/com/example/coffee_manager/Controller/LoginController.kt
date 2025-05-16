
package com.example.coffee_manager.Controller

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.coffee_manager.Model.SessionManager
import com.example.coffee_manager.Model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId

private const val TAG = "LoginController"

class LoginController {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    /**
     * Đăng nhập bằng cách query Firestore qua email/password
     */
    fun loginUser(
        email: String,
        password: String,
        onSuccess: (role: String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onFailure("Vui lòng nhập đầy đủ email và mật khẩu.")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    usersCollection.document(uid)
                        .get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                // Lưu idUser để dùng sau
                                SessionManager.currentUserId = doc.id
                                val role = doc.getString("role") ?: "Order"
                                SessionManager.role = role
                                onSuccess(role)
                            } else {
                                onFailure("Không tìm thấy thông tin người dùng.")
                            }
                        }
                        .addOnFailureListener {
                            onFailure("Lỗi khi lấy thông tin người dùng: ${it.message}")
                        }
                } else {
                    onFailure("UID không hợp lệ.")
                }
            }
            .addOnFailureListener {
                onFailure("Sai email hoặc mật khẩu: ${it.message}")
            }
    }
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Lấy thông tin User (bao gồm parse dateOfBirth từ đa dạng kiểu lưu)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchCurrentUser(): Result<User> = runCatching {
        val uid = getCurrentUserId() ?: throw Exception("Chưa đăng nhập")
        Log.d(TAG, "Fetching profile for uid=$uid")
        val snapshot = usersCollection.document(uid).get().await()
        if (!snapshot.exists()) throw Exception("User not found")

        // Các field cơ bản
        val email = snapshot.getString("email")   ?: ""
        val name  = snapshot.getString("name")    ?: ""
        val phone = snapshot.getString("phone")   ?: ""
        val role  = snapshot.getString("role")    ?: ""
        val image = snapshot.getString("imageUrl")?: ""

        // Parse dateOfBirth linh hoạt: Timestamp, String, hoặc Map có dayOfYear/year
        val rawDob = snapshot.get("dateOfBirth")
        val dob = when (rawDob) {
            is Timestamp -> rawDob.toDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate()

            is String -> runCatching { LocalDate.parse(rawDob) }.getOrNull()

            is Map<*, *> -> {
                val year      = (rawDob["year"]      as? Number)?.toInt()
                val dayOfYear = (rawDob["dayOfYear"] as? Number)?.toInt()
                if (year != null && dayOfYear != null) {
                    LocalDate.ofYearDay(year, dayOfYear)
                } else {
                    Log.e(TAG, "Invalid dob map: $rawDob")
                    null
                }
            }

            else -> {
                if (rawDob != null) Log.e(TAG, "Unexpected dob type: ${rawDob::class.java}")
                null
            }
        }

        User(
            idUser      = uid,
            email       = email,
            password    = "",       // không trả về password
            name        = name,
            dateOfBirth = dob,
            phone       = phone,
            role        = role,
            imageUrl    = image
        )
    }.onFailure {
        Log.e(TAG, "Error fetching user profile", it)
    }
}
