package com.example.learningenglishvocab.data.model

enum class UserRole { USER, ADMIN }

data class User(
    val userId: String = "",
    val email: String = "",
    val username: String = "",
    val avatar: String = "",
    val createdAt: Long = 0L,
    val premium: Boolean = false,
    val role: UserRole = UserRole.USER,
    val streak: Int = 0
)