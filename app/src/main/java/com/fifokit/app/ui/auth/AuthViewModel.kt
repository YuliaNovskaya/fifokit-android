package com.fifokit.app.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fifokit.app.data.auth.FirebaseAuthRepository
import com.fifokit.app.domain.auth.AuthRepository
import com.fifokit.app.domain.auth.AuthUser
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.fifokit.app.data.cloud.CloudRepository
import com.fifokit.app.data.cloud.model.CloudUser

import com.fifokit.app.data.RosterPreferences
import com.fifokit.app.data.RosterRepository
import com.fifokit.app.data.cloud.CloudBackupManager
import com.fifokit.app.data.cloud.CloudSyncPreferences
import com.fifokit.app.data.local.RosterDatabase

class AuthViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val cloudRepository = CloudRepository()

    private val rosterDatabase =
        RosterDatabase.getInstance(application)

    private val rosterRepository =
        RosterRepository(rosterDatabase.rosterDao())

    private val rosterPreferences =
        RosterPreferences(application)

    private val cloudBackupManager =
        CloudBackupManager(
            context = application,
            rosterRepository = rosterRepository,
            rosterPreferences = rosterPreferences
        )

    private val cloudSyncPreferences =
        CloudSyncPreferences(application)

    private val _isBackingUp =
        MutableStateFlow(false)

    val isBackingUp: StateFlow<Boolean> =
        _isBackingUp.asStateFlow()

    private val _lastBackupAt =
        MutableStateFlow(
            cloudSyncPreferences.getLastBackupAt()
        )

    val lastBackupAt: StateFlow<Long?> =
        _lastBackupAt.asStateFlow()

    private val authRepository: AuthRepository =
        FirebaseAuthRepository()

    private val analytics =
        FirebaseAnalytics.getInstance(application)

    val currentUser: StateFlow<AuthUser?> =
        authRepository.currentUser.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {

            analytics.logEvent("account_sign_in_started") {}

            _isLoading.value = true
            _errorMessage.value = null

            try {
                val user = authRepository.signInWithGoogle(idToken)

                analytics.logEvent("account_sign_in_success") {
                    param("provider", "google")
                }

                try {
                    cloudRepository.saveUser(
                        CloudUser(
                            uid = user.uid,
                            email = user.email.orEmpty(),
                            displayName = user.displayName.orEmpty()
                        )
                    )
                } catch (e: Exception) {
                    analytics.logEvent("cloud_user_save_failed") {
                        param("error_type", e.javaClass.simpleName)
                    }

                    _errorMessage.value =
                        "Signed in, but cloud profile setup failed"
                }

            } catch (e: Exception) {

                analytics.logEvent("account_sign_in_failed") {
                    param("provider", "google")
                    param("error_type", e.javaClass.simpleName)
                }

                _errorMessage.value =
                    e.message ?: "Google sign-in failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun backupNow() {

        val uid = currentUser.value?.uid
            ?: return

        viewModelScope.launch {

            _isBackingUp.value = true
            _errorMessage.value = null

            try {

                val result =
                    cloudBackupManager.backup(uid)

                cloudSyncPreferences.setLastBackupAt(
                    result.backedUpAt
                )

                _lastBackupAt.value =
                    result.backedUpAt

                analytics.logEvent(
                    "cloud_backup_complete"
                ) {
                    param(
                        "roster_count",
                        result.rostersBackedUp.toLong()
                    )
                }

            } catch (e: Exception) {

                analytics.logEvent(
                    "cloud_backup_failed"
                ) {
                    param(
                        "error_type",
                        e.javaClass.simpleName
                    )
                }

                _errorMessage.value =
                    "Cloud backup failed"

            } finally {
                _isBackingUp.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {

            authRepository.signOut()

            analytics.logEvent("account_sign_out") {}
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}