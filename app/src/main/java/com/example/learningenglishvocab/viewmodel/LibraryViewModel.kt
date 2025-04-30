package com.example.learningenglishvocab.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learningenglishvocab.data.model.Term
import com.example.learningenglishvocab.data.model.VocabSet
import com.example.learningenglishvocab.data.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter

class LibraryViewModel(
    private val authViewModel: AuthViewModel,
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {
    var vocabSets by mutableStateOf<List<VocabSet>>(emptyList())
        private set

    private val currentUserId: String?
        get() = authViewModel.getCurrentUserId()

    init {
        fetchVocabSets()
    }

    private fun fetchVocabSets() {
        currentUserId?.let { userId ->
            FirebaseFirestore.getInstance().collection("vocab_sets")
                .get()
                .addOnSuccessListener { result ->
                    viewModelScope.launch {
                        val user = userRepository.getUser(userId)
                        val vocabSetsList = result.mapNotNull { doc ->
                            val terms = (doc["terms"] as? List<Map<String, String>>)?.map {
                                Term(it["term"] ?: "", it["definition"] ?: "")
                            } ?: emptyList()

                            val vocabSet = VocabSet(
                                vocabSetId = doc.id,
                                vocabSetName = doc["vocabSetName"] as? String ?: "",
                                created_by = doc["created_by"] as? String ?: "",
                                is_public = doc["is_public"] as? Boolean ?: true,
                                created_at = doc["created_at"] as? Long ?: 0L,
                                updated_at = doc["updated_at"] as? Long ?: 0L,
                                terms = terms,
                                isPremiumContent = doc["isPremiumContent"] as? Boolean
                                    ?: false
                            )// Lọc dựa trên quyền truy cập
                            if (vocabSet.is_public || vocabSet.created_by == userId) {
                                if (vocabSet.isPremiumContent && user?.premium != true) {
                                    null
                                } else {
                                    vocabSet
                                }
                            } else {
                                null
                            }
                        }
                        vocabSets = vocabSetsList
                        Log.d("LibraryViewModel", "Fetched vocab sets: $vocabSets")
                    }
                }.addOnFailureListener { exception ->
                    Log.e("LibraryViewModel", "Error fetching vocab sets", exception)
                }
        } ?: run {
            Log.e("LibraryViewModel", "User not logged in")
            vocabSets = emptyList()
        }
    }

    fun loadAllVocabSets() {
        fetchVocabSets()
    }

    fun updateVocabSetUpdatedAt(vocabSetId: String, onSuccess: () -> Unit) {
        val updatedData = mapOf(
            "updated_at" to System.currentTimeMillis()
        )
        Firebase.firestore.collection("vocab_sets")
            .document(vocabSetId)
            .update(updatedData)
            .addOnSuccessListener {
                fetchVocabSets()
                onSuccess()
                Log.d("LibraryViewModel", "Updated updated_at for vocabSetId: $vocabSetId")
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "LibraryViewModel",
                    "Error updating updated_at for vocabSetId: $vocabSetId",
                    exception
                )
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun groupVocabSetsByDate(vocabSets: List<VocabSet>): Map<String, List<VocabSet>> {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        return vocabSets.groupBy { vocabSet ->
            val updatedDate = Instant.ofEpochMilli(vocabSet.updated_at)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            when {
                updatedDate.isEqual(today) -> "Hôm nay"
                updatedDate.isEqual(yesterday) -> "Hôm qua (${yesterday.format(formatter)})"
                else -> updatedDate.format(formatter)
            }
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