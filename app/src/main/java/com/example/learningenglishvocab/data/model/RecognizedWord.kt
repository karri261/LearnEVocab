package com.example.learningenglishvocab.data.model

data class RecognizedWord(
    val text: String,
    val boundingBox: android.graphics.Rect
)