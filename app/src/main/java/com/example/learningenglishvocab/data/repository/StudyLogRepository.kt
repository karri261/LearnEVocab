package com.example.learningenglishvocab.data.repository

import android.annotation.SuppressLint
import com.example.learningenglishvocab.data.model.StudyLog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StudyLogRepository {
    private val db = FirebaseFirestore.getInstance()
    private val studyLogsCollection = db.collection("study_logs")

    // Ghi log khi người dùng học một vocabset
    @SuppressLint("NewApi")
    suspend fun logStudySession(userId: String, vocabSetId: String): Boolean {
        return try {
            val date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val log = StudyLog(
                userId = userId,
                date = date,
                vocabSetId = vocabSetId
            )
            studyLogsCollection.document().set(log).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Lấy tất cả log của người dùng
    suspend fun getStudyLogs(userId: String): List<StudyLog> {
        return try {
            val snapshot = studyLogsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.toObjects(StudyLog::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Lấy danh sách ngày đã học
    suspend fun getStudiedDays(userId: String): List<String> {
        val logs = getStudyLogs(userId)
        return logs.map { it.date }.distinct()
    }

    // Lấy danh sách ngày đã học trong một năm, chia theo tháng
    suspend fun getStudiedDaysByYear(userId: String, year: Int): Map<Int, List<String>> {
        val logs = getStudyLogs(userId)
        // Lọc các log trong năm được chọn
        val logsInYear = logs.filter { log ->
            log.date.startsWith(year.toString())
        }
        // Nhóm các ngày theo tháng
        return logsInYear
            .groupBy { log -> log.date.substring(5, 7).toInt() } // Lấy tháng (MM) từ yyyy-MM-dd
            .mapValues { entry ->
                entry.value.map { it.date }.distinct()
            }
    }

    // Tính chuỗi lửa
    @SuppressLint("NewApi")
    suspend fun calculateStreak(userId: String): Int {
        val logs = getStudyLogs(userId)
        if (logs.isEmpty()) return 0

        // Sắp xếp theo ngày giảm dần
        val sortedDates = logs.map { it.date }
            .distinct()
            .sortedDescending()

        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        var streak = 0

        // Lấy ngày gần nhất
        val latestDate = sortedDates.firstOrNull()?.let { LocalDate.parse(it, formatter) } ?: return 0

        // Kiểm tra nếu ngày gần nhất là hôm nay hoặc hôm qua
        if (latestDate == today || latestDate == today.minusDays(1)) {
            streak = 1 // Bắt đầu chuỗi
            var currentDate = latestDate

            // Tính chuỗi liên tiếp
            for (i in 1 until sortedDates.size) {
                val date = LocalDate.parse(sortedDates[i], formatter)
                if (currentDate.minusDays(1) == date) {
                    streak++
                    currentDate = date
                } else {
                    break // Chuỗi bị gián đoạn
                }
            }
        }

        return streak
    }
}