package com.example.learningenglishvocab.data.model

data class PracticeRecord(
    val vocabSetId: String = "",
    val practiceCount: Int = 1,
    val lastCompletedAt: Long = System.currentTimeMillis()
)