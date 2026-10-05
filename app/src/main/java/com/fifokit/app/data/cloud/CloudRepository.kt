package com.fifokit.app.data.cloud

import com.fifokit.app.data.cloud.model.CloudFinancialGoal
import com.fifokit.app.data.cloud.model.CloudRoster
import com.fifokit.app.data.cloud.model.CloudSavedCalculation
import com.fifokit.app.data.cloud.model.CloudSettings
import com.fifokit.app.data.cloud.model.CloudUser
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.fifokit.app.data.cloud.model.CloudPayInput
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Source
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
        val data = mapOf(
            "id" to roster.id,
            "name" to roster.name,
            "pattern" to roster.pattern,
            "isCustomRoster" to roster.isCustomRoster,
            "customWorkDays" to roster.customWorkDays,
            "customOffDays" to roster.customOffDays,
            "startDate" to roster.startDate,
            "isShutdownRoster" to roster.isShutdownRoster,
            "endDate" to roster.endDate,
            "shutdownsJson" to roster.shutdownsJson,
            "selectedStates" to roster.selectedStates,
            "isActive" to roster.isActive,
            "isDeleted" to roster.isDeleted,
            "deletedAt" to roster.deletedAt,
            "createdAt" to roster.createdAt,
            "updatedAt" to roster.updatedAt,
            "deviceId" to roster.deviceId,
            "schemaVersion" to roster.schemaVersion
        )

        firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.ROSTERS)
            .document(roster.id)
            .set(data)
            .awaitResult()
    }

    suspend fun getRosters(
        uid: String
    ): List<CloudRoster> {

        val snapshot =
            firestore
                .collection(FirestorePaths.USERS)
                .document(uid)
                .collection(FirestorePaths.ROSTERS)
                .get()
                .awaitResult()

        return snapshot.documents.map { document ->

            CloudRoster(
                id =
                    document.getString("id")
                        ?: document.id,

                name =
                    document.getString("name").orEmpty(),

                pattern =
                    document.getString("pattern").orEmpty(),

                isCustomRoster =
                    document.getBoolean("isCustomRoster")
                        ?: document.getBoolean("customRoster")
                        ?: false,

                customWorkDays =
                    (document.getLong("customWorkDays")
                        ?: 0L).toInt(),

                customOffDays =
                    (document.getLong("customOffDays")
                        ?: 0L).toInt(),

                startDate =
                    document.getString("startDate").orEmpty(),

                isShutdownRoster =
                    document.getBoolean("isShutdownRoster")
                        ?: false,

                endDate =
                    document.getString("endDate"),

                shutdownsJson =
                    document.getString("shutdownsJson")
                        ?: "[]",

                selectedStates =
                    (
                        document.get("selectedStates")
                            as? List<*>
                    )
                        ?.mapNotNull {
                            it as? String
                        }
                        ?.ifEmpty {
                            listOf("WA")
                        }
                        ?: listOf("WA"),

                isActive =
                    document.getBoolean("isActive")
                        ?: document.getBoolean("active")
                        ?: false,

                isDeleted =
                    document.getBoolean("isDeleted")
                        ?: document.getBoolean("deleted")
                        ?: false,

                deletedAt =
                    document.getLong("deletedAt")
                        ?: 0L,

                createdAt =
                    document.getLong("createdAt")
                        ?: 0L,

                updatedAt =
                    document.getLong("updatedAt")
                        ?: 0L,

                deviceId =
                    document.getString("deviceId").orEmpty(),

                schemaVersion =
                    (document.getLong("schemaVersion")
                        ?: 1L).toInt()
            )
        }
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
            .get(Source.SERVER)
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
        await()

    suspend fun savePayInput(
        uid: String,
        payInput: CloudPayInput
    ) {
        firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.FINANCE)
            .document(FirestorePaths.PAY_INPUT_DOCUMENT)
            .set(payInput)
            .awaitResult()
    }

    suspend fun getPayInput(
        uid: String
    ): CloudPayInput? {
        return firestore
            .collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.FINANCE)
            .document(FirestorePaths.PAY_INPUT_DOCUMENT)
            .get()
            .awaitResult()
            .toObject(CloudPayInput::class.java)
    }

    suspend fun deleteAllUserData(
        uid: String
    ) {
        val userDocument =
            firestore
                .collection(FirestorePaths.USERS)
                .document(uid)

        val subcollections = listOf(
            FirestorePaths.ROSTERS,
            FirestorePaths.FINANCIAL_GOALS,
            FirestorePaths.SAVED_CALCULATIONS,
            FirestorePaths.SETTINGS,
            FirestorePaths.FINANCE
        )

        subcollections.forEach { collectionName ->

            val snapshot =
                userDocument
                    .collection(collectionName)
                    .get()
                    .awaitResult()

            snapshot.documents.forEach { document ->
                document.reference
                    .delete()
                    .awaitResult()
            }
        }

        userDocument
            .delete()
            .awaitResult()
    }

}