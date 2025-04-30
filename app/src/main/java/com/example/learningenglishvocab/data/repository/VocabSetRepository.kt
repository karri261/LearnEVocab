package com.example.learningenglishvocab.data.repository

import com.example.learningenglishvocab.data.model.VocabSet
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class VocabSetRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("vocab_sets")

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
}