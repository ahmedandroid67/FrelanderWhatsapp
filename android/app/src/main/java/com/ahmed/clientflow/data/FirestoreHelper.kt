package com.ahmed.clientflow.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

object FirestoreHelper {
    private val db = FirebaseFirestore.getInstance()
    private const val ACTIVATIONS_COLLECTION = "activations"

    suspend fun activateCode(code: String, deviceId: String): ActivationResult {
        return try {
            val snapshot = db.collection(ACTIVATIONS_COLLECTION)
                .whereEqualTo("code", code.uppercase())
                .whereEqualTo("used", false)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return ActivationResult.CodeNotFound
            }

            val doc = snapshot.documents.first()
            val docRef = db.collection(ACTIVATIONS_COLLECTION).document(doc.id)

            docRef.update(
                mapOf(
                    "used" to true,
                    "usedAt" to com.google.firebase.Timestamp.now(),
                    "activatedDeviceId" to deviceId
                )
            ).await()

            ActivationResult.Success
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                ActivationResult.CodeNotFound
            } else {
                ActivationResult.Error(e.message ?: "Unknown error")
            }
        } catch (e: Exception) {
            ActivationResult.Error(e.message ?: "Unknown error")
        }
    }
}

sealed class ActivationResult {
    data object Success : ActivationResult()
    data object CodeNotFound : ActivationResult()
    data class Error(val message: String) : ActivationResult()
}