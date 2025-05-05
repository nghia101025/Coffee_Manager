package com.example.coffee_manager.Controller

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginController {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun loginUser(
        email: String,
        password: String,
        onSuccess: (role: String) -> Unit,
        onFailure: (message: String) -> Unit
    ) {
        if (email.isEmpty() || password.isEmpty()) {
            onFailure("Vui lòng nhập đầy đủ email và mật khẩu.")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { document ->
                            val role = document.getString("role") ?: "Order"
                            onSuccess(role)
                        }
                        .addOnFailureListener {
                            onFailure("Không lấy được thông tin người dùng.")
                        }
                } else {
                    onFailure("Không tìm thấy người dùng.")
                }
            }
            .addOnFailureListener {
                onFailure("Sai email hoặc mật khẩu: ${it.message}")
            }
    }
}
