package com.example.learningenglishvocab.viewmodel

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learningenglishvocab.data.model.StudyLog
import com.example.learningenglishvocab.data.model.Term
import com.example.learningenglishvocab.data.model.VocabSet
import com.example.learningenglishvocab.data.repository.StudyLogRepository
import com.example.learningenglishvocab.data.repository.UserRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LibraryViewModel(
    private val authViewModel: AuthViewModel,
    private val userRepository: UserRepository = UserRepository(),
    private val studyLogRepository: StudyLogRepository = StudyLogRepository(),
) : ViewModel() {
    var vocabSetWithLogs by mutableStateOf<List<Pair<VocabSet, StudyLog>>>(emptyList())
        private set

    private val currentUserId: String?
        get() = authViewModel.getCurrentUserId()

    init {
        fetchVocabSets()
    }

    @SuppressLint("NewApi")
    private fun fetchVocabSets() {
        currentUserId?.let { userId ->
            viewModelScope.launch {
                try {
                    // Lấy danh sách StudyLog của người dùng
                    val studyLogs = studyLogRepository.getStudyLogs(userId)
                    val vocabSetIds = studyLogs.map { it.vocabSetId }.distinct()

                    // Lấy danh sách VocabSet do người dùng tạo
                    val createdByQuery = FirebaseFirestore.getInstance().collection("vocab_sets")
                        .whereEqualTo("created_by", userId)
                    val tasks = mutableListOf<Task<QuerySnapshot>>()
                    tasks.add(createdByQuery.get())

                    // Lấy danh sách VocabSet từ StudyLog
                    if (vocabSetIds.isNotEmpty()) {
                        val historyQuery = FirebaseFirestore.getInstance().collection("vocab_sets")
                            .whereIn(FieldPath.documentId(), vocabSetIds)
                        tasks.add(historyQuery.get())
                    }

                    // Kết hợp các truy vấn
                    Tasks.whenAllSuccess<QuerySnapshot>(tasks)
                        .addOnSuccessListener { results ->
                            viewModelScope.launch {
                                val user = userRepository.getUser(userId)
                                val vocabSets = results.flatMap { result ->
                                    result.documents.mapNotNull { doc ->
                                        val terms = (doc["terms"] as? List<Map<String, String>>)?.map {
                                            Term(it["term"] ?: "", it["definition"] ?: "")
                                        } ?: emptyList()

                                        val vocabSet = VocabSet(
                                            vocabSetId = doc.id,
                                            vocabSetName = doc["vocabSetName"] as? String ?: "",
                                            created_by = doc["created_by"] as? String ?: "",
                                            _public = doc["is_public"] as? Boolean ?: true,
                                            created_at = doc["created_at"] as? Long ?: 0L,
                                            updated_at = doc["updated_at"] as? Long ?: 0L,
                                            terms = terms,
                                            premiumContent = doc["premiumContent"] as? Boolean ?: false
                                        )
                                        if (vocabSet._public || vocabSet.created_by == userId) {
                                            if (vocabSet.premiumContent && user?.premium != true) {
                                                null
                                            } else {
                                                vocabSet
                                            }
                                        } else {
                                            null
                                        }
                                    }
                                }.distinctBy { it.vocabSetId }

                                // Ánh xạ VocabSet với StudyLog mới nhất
                                val vocabSetWithLogsList = vocabSets.mapNotNull { vocabSet ->
                                    // Lấy StudyLog mới nhất cho vocabSet này
                                    val latestLog = studyLogs
                                        .filter { it.vocabSetId == vocabSet.vocabSetId }
                                        .maxByOrNull { it.date }
                                    if (latestLog != null) {
                                        vocabSet to latestLog
                                    } else if (vocabSet.created_by == userId) {
                                        // Nếu là bộ do người dùng tạo nhưng chưa có StudyLog
                                        vocabSet to StudyLog(
                                            userId = userId,
                                            date = Instant.ofEpochMilli(vocabSet.created_at)
                                                .atZone(ZoneId.systemDefault())
                                                .format(DateTimeFormatter.ISO_LOCAL_DATE),
                                            vocabSetId = vocabSet.vocabSetId
                                        )
                                    } else {
                                        null
                                    }
                                }.sortedByDescending { it.second.date } // Sắp xếp theo date từ StudyLog

                                vocabSetWithLogs = vocabSetWithLogsList
                                Log.d("LibraryViewModel", "Fetched vocab sets with logs: $vocabSetWithLogs")
                            }
                        }.addOnFailureListener { exception ->
                            Log.e("LibraryViewModel", "Error fetching vocab sets", exception)
                            vocabSetWithLogs = emptyList()
                        }
                } catch (e: Exception) {
                    Log.e("LibraryViewModel", "Error in fetchVocabSets", e)
                    vocabSetWithLogs = emptyList()
                }
            }
        } ?: run {
            Log.e("LibraryViewModel", "User not logged in")
            vocabSetWithLogs = emptyList()
        }
    }

    fun loadAllVocabSets() {
        fetchVocabSets()
    }

    fun updateVocabSetUpdatedAt(vocabSetId: String, onSuccess: () -> Unit) {
        val updatedData = mapOf(
            "updated_at" to System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance().collection("vocab_sets")
            .document(vocabSetId)
            .update(updatedData)
            .addOnSuccessListener {
                fetchVocabSets()
                onSuccess()
                Log.d("LibraryViewModel", "Updated updated_at for vocabSetId: $vocabSetId")
            }
            .addOnFailureListener { exception ->
                Log.e("LibraryViewModel", "Error updating updated_at for vocabSetId: $vocabSetId", exception)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun groupVocabSetsByDate(vocabSetWithLogs: List<Pair<VocabSet, StudyLog>>): Map<String, List<VocabSet>> {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        return vocabSetWithLogs.groupBy { (_, studyLog) ->
            val logDate = LocalDate.parse(studyLog.date, DateTimeFormatter.ISO_LOCAL_DATE)

            when {
                logDate.isEqual(today) -> "Hôm nay"
                logDate.isEqual(yesterday) -> "Hôm qua (${yesterday.format(formatter)})"
                else -> logDate.format(formatter)
            }
        }.mapValues { entry ->
            entry.value.map { it.first }
        }.toList()
            .sortedByDescending { (dateStr, _) ->
                when {
                    dateStr.startsWith("Hôm nay") -> today
                    dateStr.startsWith("Hôm qua") -> yesterday
                    else -> LocalDate.parse(dateStr.takeLast(10), formatter)
                }
            }.toMap()
    }
}