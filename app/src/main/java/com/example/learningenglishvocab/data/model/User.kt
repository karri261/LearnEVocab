package com.example.learningenglishvocab.data.model

enum class UserRole { USER, ADMIN }

data class User(
    val userId: String,
    val email: String,
    val username: String,
    val avatar: String,
    val createdAt: Long,
    val premium: Boolean,
    val role: UserRole
)