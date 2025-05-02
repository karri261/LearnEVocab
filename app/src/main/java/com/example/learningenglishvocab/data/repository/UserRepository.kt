package com.example.learningenglishvocab.data.repository

import android.util.Log
import com.example.learningenglishvocab.data.model.PracticeRecord
import com.example.learningenglishvocab.data.model.Transaction
import com.example.learningenglishvocab.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun createUser(user: User): Boolean {
        return try {
            usersCollection.document(user.userId).set(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUser(userId: String): User? {
        return try {
            Log.d("UserRepository", "Attempting to fetch user with ID: $userId")
            val document = usersCollection.document(userId).get().await()
            if (document.exists()) {
                val user = document.toObject(User::class.java)
                Log.d("UserRepository", "User found: $user")
                user
            } else {
                Log.e("UserRepository", "No user found for ID: $userId")
                null
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user for ID $userId: ${e.message}", e)
            null
        }
    }

    suspend fun updateUser(user: User): Boolean {
        return try {
            usersCollection.document(user.userId).set(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Lưu streak của user vào Firestore
    suspend fun updateUserStreak(userId: String, streak: Int): Boolean {
        return try {
            usersCollection.document(userId).update("streak", streak).await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating streak: ${e.message}")
            false
        }
    }

    // Lấy streak của user từ Firestore
    suspend fun getUserStreak(userId: String): Int {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            snapshot.getLong("streak")?.toInt() ?: 0
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting streak: ${e.message}")
            0
        }
    }

    suspend fun isUsernameTaken(username: String, currentUserId: String): Boolean {
        return try {
            val snapshot = usersCollection.whereEqualTo("username", username).get().await()
            snapshot.documents.any { document ->
                val userId = document.getString("userId")
                userId != null && userId != currentUserId
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error checking username: ${e.message}")
            false
        }
    }

    suspend fun saveTransaction(transaction: Transaction): Boolean {
        return try {
            Firebase.firestore.collection("transactions")
                .document()
                .set(transaction)
                .await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error saving transaction: ${e.message}", e)
            false
        }
    }

    suspend fun addPracticeRecord(userId: String, record: PracticeRecord): Boolean {
        return try {
            usersCollection.document(userId)
                .collection("practiceHistory")
                .document(record.vocabSetId)
                .set(record)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getPracticeRecord(userId: String, vocabSetId: String): PracticeRecord? {
        return try {
            val snapshot = usersCollection.document(userId)
                .collection("practiceHistory")
                .document(vocabSetId)
                .get()
                .await()
            snapshot.toObject(PracticeRecord::class.java)
        } catch (e: Exception) {
            null
        }
    }
}