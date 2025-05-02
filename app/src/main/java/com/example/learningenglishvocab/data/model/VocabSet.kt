package com.example.learningenglishvocab.data.model

data class VocabSet(
    var vocabSetId: String = "",
    val vocabSetName: String = "",
    val created_by: String = "",
    val _public: Boolean = false,
    val created_at: Long = System.currentTimeMillis(),
    val updated_at: Long = System.currentTimeMillis(),
    val terms: List<Term> = emptyList(),
    val premiumContent: Boolean = false
)