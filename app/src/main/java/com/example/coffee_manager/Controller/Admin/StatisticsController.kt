package com.example.coffee_manager.Controller

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Các khoảng thời gian hỗ trợ cho thống kê.
 */
enum class Period {
    DAY, WEEK, MONTH, QUARTER, YEAR
}

/**
 * Controller chịu trách nhiệm lấy doanh thu từ Firestore theo từng period.
 * Mỗi entry trong Map trả về có key là chuỗi ngày (định dạng "dd/MM") và
 * value là tổng doanh thu của ngày đó.
 */
class StatisticsController {
    private val db = FirebaseFirestore.getInstance()
    private val bills = db.collection("bills")

    /**
     * Fetch doanh thu cho khoảng thời gian [period].
     * Trả về Map từ label ngày (dd/MM) tới tổng doanh thu (Double).
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchRevenue(period: Period): Map<String, Double> {
        val fmt = DateTimeFormatter.ofPattern("dd/MM")
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val start = when (period) {
            Period.DAY -> today
            Period.WEEK -> today.minusDays(6)
            Period.MONTH -> today.minusMonths(1).plusDays(1)
            Period.QUARTER -> today.minusMonths(3).plusDays(1)
            Period.YEAR -> today.minusYears(1).plusDays(1)
        }

        // Khởi tạo kết quả với 0.0 cho mỗi ngày trong khoảng
        val result = mutableMapOf<String, Double>()
        var date = start
        while (!date.isAfter(today)) {
            result[date.format(fmt)] = 0.0
            date = date.plusDays(1)
        }

        // Chỉ query theo createdAt (khoảng timestamp), tránh lỗi cần index phức hợp
        val startMillis = start.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

        val snapshot = bills
            .whereGreaterThanOrEqualTo("createdAt", startMillis)
            .whereLessThanOrEqualTo("createdAt", endMillis)
            .get()
            .await()

        // Lọc finish == true và cộng dồn tổng giá trị
        for (doc in snapshot.documents) {
            val finished = doc.getBoolean("finish") ?: false
            if (!finished) continue

            val total = doc.getLong("totalPrice")?.toDouble() ?: continue
            val ts = doc.getLong("createdAt") ?: continue

            // Chuyển đổi từ timestamp sang LocalDate
            val orderDate = Instant
                .ofEpochMilli(ts)
                .atZone(zone)
                .toLocalDate()

            val key = orderDate.format(fmt)
            result[key] = (result[key] ?: 0.0) + total
        }

        return result
    }
}