package com.example.learningenglishvocab.data.model

data class AnsweredTerm(
    val term: Term,
    val isCorrect: Boolean,
    val round: Int
)