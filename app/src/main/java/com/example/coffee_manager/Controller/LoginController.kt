
package com.example.coffee_manager.Controller

import android.util.Log
import com.example.coffee_manager.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "LoginController"

class LoginController {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    /**
     * Đăng nhập bằng cách query Firestore qua email/password
     */
    fun loginUser(
        email: String,
        password: String,
        onSuccess: (role: String) -> Unit,
        onFailure: (message: String) -> Unit
    ) {
        Log.d(TAG, "loginUser called: email=$email")
        if (email.isBlank() || password.isBlank()) {
            Log.w(TAG, "Empty email or password")
            onFailure("Vui lòng nhập đầy đủ email và mật khẩu.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.w(TAG, "Invalid email format: $email")
            onFailure("Email không hợp lệ.")
            return
        }

        Log.d(TAG, "Querying Firestore for user document")
        db.collection("users")
            .whereEqualTo("email", email.trim())
            .whereEqualTo("password", password)
            .limit(1)
            .get()
            .addOnSuccessListener { query ->
                Log.d(TAG, "Firestore query success, size=${query.size()}")
                if (query.isEmpty) {
                    onFailure("Sai email hoặc mật khẩu.")
                } else {
                    val doc = query.documents[0]
                    Log.d(TAG, "User doc id=${doc.id}")
                    val data = doc.data
                    if (data == null) {
                        onFailure("Dữ liệu không hợp lệ")
                    } else {
                        val role = data["role"] as? String ?: ""
                        onSuccess(role)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore query failure", e)
                onFailure("Lỗi đăng nhập: ${e.message}")
            }
    }
}
