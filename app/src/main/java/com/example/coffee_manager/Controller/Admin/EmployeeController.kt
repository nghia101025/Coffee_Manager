package com.example.coffee_manager.Controller.Admin

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.coffee_manager.Model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId

class EmployeeController {
    companion object {
        private const val TAG = "EmployeeController"
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    /** Trả về FirebaseUser hiện tại (hoặc null nếu chưa đăng nhập) */
    fun getCurrentAuthUser(): FirebaseUser? = auth.currentUser

    /**
     * Lấy danh sách tất cả nhân viên.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAllEmployees(): Result<List<User>> = runCatching {
        val snap = usersCollection.get().await()
        snap.documents.mapNotNull { doc ->
            try {
                val uid   = doc.id
                val data  = doc.data ?: return@mapNotNull null
                val email = data["email"]    as? String ?: return@mapNotNull null
                val name  = data["name"]     as? String ?: ""
                val role  = data["role"]     as? String ?: ""
                val phone = data["phone"]    as? String ?: ""
                val image = data["imageUrl"] as? String ?: ""

                // Xử lý dateOfBirth
                val rawDob = data["dateOfBirth"]
                val dob: LocalDate? = parseDob(rawDob)

                User(
                    idUser      = uid,
                    email       = email,
                    password    = "",
                    name        = name,
                    dateOfBirth = dob,
                    phone       = phone,
                    role        = role,
                    imageUrl    = image
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing employee document ${doc.id}", e)
                null
            }
        }.let { Result.success(it) }
    }.getOrElse { Result.failure(it) }

    /**
     * Lấy một nhân viên theo idUser.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getEmployeeById(idUser: String): Result<User> = runCatching {
        Log.d(TAG, "Fetching user by idUser: $idUser")
        val snapshot = usersCollection.document(idUser).get().await()
        if (!snapshot.exists()) throw Exception("Không tìm thấy nhân viên với id $idUser")

        val email = snapshot.getString("email") ?: ""
        val name  = snapshot.getString("name") ?: ""
        val role  = snapshot.getString("role") ?: ""
        val phone = snapshot.getString("phone") ?: ""
        val image = snapshot.getString("imageUrl") ?: ""

        // Xử lý dateOfBirth
        val rawDob = snapshot.get("dateOfBirth")
        val dob: LocalDate? = parseDob(rawDob)

        User(
            idUser      = idUser,
            email       = email,
            name        = name,
            dateOfBirth = dob,
            phone       = phone,
            role        = role,
            imageUrl    = image
        )
    }.onFailure {
        Log.e(TAG, "Failed to load user $idUser", it)
    }

    suspend fun isPhoneNumberExists(phone: String): Boolean = try {
        val querySnapshot = usersCollection.whereEqualTo("phone", phone).get().await()
        !querySnapshot.isEmpty
    } catch (e: Exception) {
        false // Nếu lỗi thì coi như chưa tồn tại (hoặc xử lý khác tùy bạn)
    }

    /**
     * Tạo tài khoản mới trong Auth và thêm document trong Firestore.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addEmployee(user: User, rawPassword: String): Result<Unit> = runCatching {
        auth.createUserWithEmailAndPassword(user.email, rawPassword).await()
        val uid = auth.currentUser?.uid ?: throw Exception("Không lấy được UID")
        val toSave = user.copy(idUser = uid)
        usersCollection.document(uid).set(toSave).await()
    }.fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { e ->
            if (e is FirebaseAuthException && e.errorCode == "ERROR_EMAIL_ALREADY_IN_USE" ) {
                Result.failure(Exception("Email đã được sử dụng."))
            }else{
                Result.failure(Exception(e.localizedMessage ?: "Thêm nhân viên thất bại"))
            }
        }
    )

    /**
     * Cập nhật thông tin user (không thay password) dựa trên document ID.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateEmployee(user: User): Result<Unit> = runCatching {
        val docRef = usersCollection.document(user.idUser)
        val snapshot = docRef.get().await()

        if (!snapshot.exists()) throw Exception("Không tìm thấy nhân viên để cập nhật")

        val oldPassword = snapshot.getString("password") ?: ""

        val userToUpdate = user.copy(password = oldPassword)

        docRef.set(userToUpdate).await()
    }.fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { e -> Result.failure(Exception(e.localizedMessage ?: "Cập nhật thất bại")) }
    )

    /**
     * Xóa nhân viên theo idUser (document ID).
     */
    suspend fun deleteEmployee(idUser: String): Result<Unit> = runCatching {
        usersCollection.document(idUser).delete().await()
    }.fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { e -> Result.failure(Exception(e.localizedMessage ?: "Xóa thất bại")) }
    )

    /**
     * Hàm helper parse day-of-birth từ nhiều kiểu input
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseDob(rawDob: Any?): LocalDate? {
        return when (rawDob) {
            is Timestamp -> rawDob.toDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            is String -> runCatching { LocalDate.parse(rawDob) }.getOrNull()

            is Map<*, *> -> {
                // Map có thể chứa keys: "year","month","monthValue","dayOfYear",...
                val year      = (rawDob["year"] as? Number)?.toInt()
                val dayOfYear = (rawDob["dayOfYear"] as? Number)?.toInt()
                if (year != null && dayOfYear != null) {
                    LocalDate.ofYearDay(year, dayOfYear)
                } else {
                    Log.e(TAG, "Invalid dob map fields: $rawDob")
                    null
                }
            }

            else -> {
                if (rawDob != null) Log.e(TAG, "Unexpected dob type: ${rawDob::class.java}")
                null
            }
        }
    }

}