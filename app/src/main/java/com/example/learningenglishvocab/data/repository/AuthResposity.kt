package com.example.learningenglishvocab.data.repository

import com.example.learningenglishvocab.data.model.User
import com.example.learningenglishvocab.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthRepository(private val auth: FirebaseAuth) {
    suspend fun loginUser(email: String, password: String): FirebaseUser? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user
        } catch (e: Exception) {
            null
        }
    }

    suspend fun registerUser(email: String, password: String): FirebaseUser? {
        return try {
            val result = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                val db = FirebaseFirestore.getInstance()
                val userId = firebaseUser.uid

                val username = "user_${UUID.randomUUID().toString().substring(0, 8)}"

                val defaultAvatar = "iVBORw0KGgoAAAANSUhEUgAAAEYAAABFCAIAAAB42Ad9AAAACXBIWXMAABJ0AAASdAHeZh94AAAFFmlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iQWRvYmUgWE1QIENvcmUgNi4wLWMwMDIgNzkuMTY0NDYwLCAyMDIwLzA1LzEyLTE2OjA0OjE3ICAgICAgICAiPiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPiA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyIgeG1sbnM6cGhvdG9zaG9wPSJodHRwOi8vbnMuYWRvYmUuY29tL3Bob3Rvc2hvcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgMjEuMiAoV2luZG93cykiIHhtcDpDcmVhdGVEYXRlPSIyMDI1LTA0LTI3VDE1OjI0OjAxKzA3OjAwIiB4bXA6TW9kaWZ5RGF0ZT0iMjAyNS0wNC0yN1QyMDozMjoxMCswNzowMCIgeG1wOk1ldGFkYXRhRGF0ZT0iMjAyNS0wNC0yN1QyMDozMjoxMCswNzowMCIgZGM6Zm9ybWF0PSJpbWFnZS9wbmciIHBob3Rvc2hvcDpDb2xvck1vZGU9IjMiIHBob3Rvc2hvcDpJQ0NQcm9maWxlPSJzUkdCIElFQzYxOTY2LTIuMSIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDozNzFhM2MzYy0wYzgxLTIy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"

                // Tạo đối tượng User
                val user = User(
                    userId = userId,
                    email = email,
                    username = username,
                    avatar = defaultAvatar,
                    createdAt = System.currentTimeMillis(),
                    premium = false,
                    role = UserRole.USER
                )

                db.collection("users").document(userId).set(user).await()
            }

            firebaseUser
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun sendEmailVerification(user: FirebaseUser): Boolean {
        return try {
            user.sendEmailVerification().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun isEmailVerified(): Boolean {
        return auth.currentUser?.reload()?.await().let {
            auth.currentUser?.isEmailVerified ?: false
        }
    }
}