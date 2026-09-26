package com.fifokit.app.data.cloud

import com.fifokit.app.data.cloud.model.CloudFinancialGoal
import com.fifokit.app.data.cloud.model.CloudRoster
import com.fifokit.app.data.cloud.model.CloudSavedCalculation
import com.fifokit.app.data.cloud.model.CloudSettings
import com.fifokit.app.data.cloud.model.CloudUser
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.firebase.firestore.SetOptions
class CloudRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun saveUser(user: CloudUser) {

        val document = firestore
            .collection(FirestorePaths.USERS)
            .document(user.uid)

        val existing = document
            .get()
            .awaitResult()

        val now = System.currentTimeMillis()

        val cloudUser = user.copy(
            createdAt = existing.getLong("createdAt")
                ?: if (user.createdAt != 0L) user.createdAt else now,
            updatedAt = now
        )

        document
            .set(cloudUser, SetOptions.merge())
            .awaitResult()
    }

    suspend fun saveRoster(
        uid: String,
        roster: CloudRoster
    ) {
        firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.ROSTERS)
            .document(roster.id)
            .set(roster)
            .awaitResult()
    }

    suspend fun getRosters(
        uid: String
    ): List<CloudRoster> {
        return firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.ROSTERS)
            .get()
            .awaitResult()
            .toObjects(CloudRoster::class.java)
    }

    suspend fun deleteRoster(
        uid: String,
        rosterId: String
    ) {
        firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.ROSTERS)
            .document(rosterId)
            .delete()
            .awaitResult()
    }

    suspend fun saveSettings(
        uid: String,
        settings: CloudSettings
    ) {
        firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.SETTINGS)
            .document(FirestorePaths.APP_SETTINGS_DOCUMENT)
            .set(settings)
            .awaitResult()
    }

    suspend fun getSettings(
        uid: String
    ): CloudSettings? {
        return firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.SETTINGS)
            .document(FirestorePaths.APP_SETTINGS_DOCUMENT)
            .get()
            .awaitResult()
            .toObject(CloudSettings::class.java)
    }

    suspend fun saveFinancialGoal(
        uid: String,
        goal: CloudFinancialGoal
    ) {
        firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.FINANCIAL_GOALS)
            .document(goal.id)
            .set(goal)
            .awaitResult()
    }

    suspend fun getFinancialGoals(
        uid: String
    ): List<CloudFinancialGoal> {
        return firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.FINANCIAL_GOALS)
            .get()
            .awaitResult()
            .toObjects(CloudFinancialGoal::class.java)
    }

    suspend fun deleteFinancialGoal(
        uid: String,
        goalId: String
    ) {
        firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.FINANCIAL_GOALS)
            .document(goalId)
            .delete()
            .awaitResult()
    }

    suspend fun saveCalculation(
        uid: String,
        calculation: CloudSavedCalculation
    ) {
        firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.SAVED_CALCULATIONS)
            .document(calculation.id)
            .set(calculation)
            .awaitResult()
    }

    suspend fun getCalculations(
        uid: String
    ): List<CloudSavedCalculation> {
        return firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.SAVED_CALCULATIONS)
            .get()
            .awaitResult()
            .toObjects(CloudSavedCalculation::class.java)
    }

    suspend fun deleteCalculation(
        uid: String,
        calculationId: String
    ) {
        firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.SAVED_CALCULATIONS)
            .document(calculationId)
            .delete()
            .awaitResult()
    }

    private suspend fun <T> Task<T>.awaitResult(): T =
        suspendCancellableCoroutine { continuation ->

            addOnSuccessListener { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }

            addOnFailureListener { exception ->
                if (continuation.isActive) {
                    continuation.resumeWithException(exception)
                }
            }
        }
}