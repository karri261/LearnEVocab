package com.example.learningenglishvocab.data.repository

import android.util.Log
import com.example.learningenglishvocab.data.model.VocabSet
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class VocabSetRepository {

    private val firestore = FirebaseFirestore.getInstance()
    val collection = firestore.collection("vocab_sets")

    fun addVocabSet(
        vocabSet: VocabSet,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val doc = collection.document(vocabSet.vocabSetId.ifEmpty { collection.document().id })
        val vocabSetWithId = vocabSet.copy(vocabSetId = doc.id)

        doc.set(vocabSetWithId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun updateVocabSet(
        vocabSet: VocabSet,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val id = vocabSet.vocabSetId
        collection.document(id)
            .set(vocabSet)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteVocabSet(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        collection.document(id)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun getVocabSetById(
        id: String,
        onResult: (VocabSet) -> Unit
    ) {
        collection.document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val vocabSet = document.toObject(VocabSet::class.java)
                    vocabSet?.vocabSetId = document.id
                    if (vocabSet != null) {
                        onResult(vocabSet)
                    }
                }
            }
    }

    suspend fun getPublicVocabSetsByCreator(creatorId: String, userId: String, limit: Long): List<VocabSet> {
        return try {
            val snapshot = collection
                .whereEqualTo("created_by", creatorId)
                .whereEqualTo("_public", true)
                .whereEqualTo("premiumContent", false)
                .limit(limit)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(VocabSet::class.java)?.apply { vocabSetId = doc.id }
                } catch (e: Exception) {
                    Log.e("VocabSetSuggest", "Error mapping VocabSet ${doc.id}: $e")
                    null
                }
            }.also {
                Log.d("VocabSetSuggest", "Fetched ${it.size} VocabSets for creator $creatorId: $it")
            }
        } catch (e: Exception) {
            Log.e("VocabSetSuggest", "Error fetching creator vocab sets: $e")
            emptyList()
        }
    }

    suspend fun getRandomPublicVocabSets(userId: String, limit: Long): List<VocabSet> {
        return try {
            val snapshot = collection
                .whereNotEqualTo("created_by", userId)
                .whereEqualTo("_public", true)
                .whereEqualTo("premiumContent", false)
                .limit(limit)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(VocabSet::class.java)?.apply { vocabSetId = doc.id }
                } catch (e: Exception) {
                    Log.e("VocabSetSuggest", "Error mapping VocabSet ${doc.id}: $e")
                    null
                }
            }.also {
                Log.d("VocabSetSuggest", "Fetched ${it.size} random VocabSets: $it")
            }
        } catch (e: Exception) {
            Log.e("VocabSetSuggest", "Error fetching random vocab sets: $e")
            emptyList()
        }
    }
}