package com.example.learningenglishvocab.data.model

data class Transaction(
    val userId: String = "",
    val paymentIntentId: String = "",
    val amount: Long = 0,
    val currency: String = "vnd",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "completed"
)