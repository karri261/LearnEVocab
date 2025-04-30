package com.example.learningenglishvocab.data.model

import java.util.UUID

enum class TermStatus { NONE, LEARNING, KNOWN }

data class Term(
    val id: String = UUID.randomUUID().toString(),
    val term: String = "",
    val definition: String = "",
    var status: TermStatus = TermStatus.NONE
)