package com.example.learningenglishvocab.data.repository

import android.util.Log
import com.example.learningenglishvocab.data.model.PracticeRecord
import com.example.learningenglishvocab.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
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