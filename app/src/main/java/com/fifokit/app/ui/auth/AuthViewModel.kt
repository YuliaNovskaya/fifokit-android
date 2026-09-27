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
import com.fifokit.app.data.cloud.RosterCloudSyncManager
import com.fifokit.app.data.cloud.SettingsCloudSyncManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.fifokit.app.data.FinancePreferences
import com.fifokit.app.data.cloud.FinancialGoalCloudSyncManager
import com.fifokit.app.data.cloud.PayInputCloudSyncManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collectLatest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network

class AuthViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _isDeletingAccount =
        MutableStateFlow(false)

    val isDeletingAccount: StateFlow<Boolean> =
        _isDeletingAccount.asStateFlow()

    private val financePreferences =
        FinancePreferences(application)

    private val financialGoalCloudSyncManager =
        FinancialGoalCloudSyncManager(
            context = application,
            financePreferences = financePreferences
        )

    private val _syncCompleted =
        MutableSharedFlow<Unit>()

    val syncCompleted: SharedFlow<Unit> =
        _syncCompleted.asSharedFlow()

    private val payInputCloudSyncManager =
        PayInputCloudSyncManager(
            context = application,
            financePreferences = financePreferences
        )

    private val cloudRepository = CloudRepository()

    private val rosterDatabase =
        RosterDatabase.getInstance(application)

    private val rosterRepository =
        RosterRepository(rosterDatabase.rosterDao())

    private val rosterPreferences =
        RosterPreferences(application)
    private val cloudSyncPreferences =
        CloudSyncPreferences(application)

    private val cloudBackupManager =
        CloudBackupManager(
            context = application,
            rosterRepository = rosterRepository,
            rosterPreferences = rosterPreferences
        )
    private val rosterCloudSyncManager =
        RosterCloudSyncManager(
            context = application,
            rosterRepository = rosterRepository,
            rosterPreferences = rosterPreferences
        )

    private val _isSyncing =
        MutableStateFlow(false)

    val isSyncing: StateFlow<Boolean> =
        _isSyncing.asStateFlow()

    private val _lastSyncAt =
        MutableStateFlow(
            cloudSyncPreferences.getLastSyncAt()
        )

    val lastSyncAt: StateFlow<Long?> =
        _lastSyncAt.asStateFlow()

    private val settingsCloudSyncManager =
        SettingsCloudSyncManager(
            context = application,
            rosterRepository = rosterRepository,
            rosterPreferences = rosterPreferences
        )


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

    private val connectivityManager =
        application.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

    private val networkCallback =
        object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {

                val uid =
                    currentUser.value?.uid
                        ?: return

                viewModelScope.launch {
                    performSync(
                        uid = uid,
                        reportError = false
                    )
                }
            }
        }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {

            currentUser
                .map { it?.uid }
                .distinctUntilChanged()
                .collectLatest { uid ->

                    if (uid != null) {
                        performSync(
                            uid = uid,
                            reportError = false
                        )
                    }
                }
        }
        connectivityManager.registerDefaultNetworkCallback(
            networkCallback
        )
    }

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

    fun syncNow() {

        val uid = currentUser.value?.uid
            ?: return

        viewModelScope.launch {
            performSync(
                uid = uid,
                reportError = true
            )
        }
    }

    private suspend fun performSync(
        uid: String,
        reportError: Boolean
    ) {

        if (_isSyncing.value) {
            return
        }

        _isSyncing.value = true

        if (reportError) {
            _errorMessage.value = null
        }

        try {

            val rosterResult =
                rosterCloudSyncManager.sync(uid)

            val settingsResult =
                settingsCloudSyncManager.sync(uid)

            val financialGoalResult =
                financialGoalCloudSyncManager.sync(uid)

            val payInputResult =
                payInputCloudSyncManager.sync(uid)

            val now =
                System.currentTimeMillis()

            cloudSyncPreferences.setLastSyncAt(now)
            _lastSyncAt.value = now

            _syncCompleted.emit(Unit)

            analytics.logEvent(
                "cloud_sync_complete"
            ) {
                param(
                    "rosters_uploaded",
                    rosterResult.uploaded.toLong()
                )
                param(
                    "rosters_downloaded",
                    rosterResult.downloaded.toLong()
                )
                param(
                    "rosters_unchanged",
                    rosterResult.unchanged.toLong()
                )
                param(
                    "settings_uploaded",
                    if (settingsResult.uploaded) 1L else 0L
                )
                param(
                    "settings_downloaded",
                    if (settingsResult.downloaded) 1L else 0L
                )
                param(
                    "financial_goal_uploaded",
                    if (financialGoalResult.uploaded) 1L else 0L
                )
                param(
                    "financial_goal_downloaded",
                    if (financialGoalResult.downloaded) 1L else 0L
                )
                param(
                    "pay_input_uploaded",
                    if (payInputResult.uploaded) 1L else 0L
                )
                param(
                    "pay_input_downloaded",
                    if (payInputResult.downloaded) 1L else 0L
                )
            }

        } catch (e: Exception) {

            analytics.logEvent(
                "cloud_sync_failed"
            ) {
                param(
                    "error_type",
                    e.javaClass.simpleName
                )
            }

            if (reportError) {
                _errorMessage.value =
                    "Cloud sync failed"
            }

        } finally {
            _isSyncing.value = false
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

    fun deleteAccount(
        googleIdToken: String
    ) {

        val uid = currentUser.value?.uid
            ?: return

        viewModelScope.launch {

            _isDeletingAccount.value = true
            _errorMessage.value = null

            try {

                authRepository.reauthenticateWithGoogle(
                    googleIdToken
                )

                cloudRepository.deleteAllUserData(
                    uid
                )

                authRepository.deleteAccount()

                analytics.logEvent(
                    "account_deleted"
                ) {}

            } catch (e: Exception) {

                analytics.logEvent(
                    "account_delete_failed"
                ) {
                    param(
                        "error_type",
                        e.javaClass.simpleName
                    )
                }

                _errorMessage.value =
                    "Account deletion failed"

            } finally {
                _isDeletingAccount.value = false
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

    override fun onCleared() {

        runCatching {
            connectivityManager.unregisterNetworkCallback(
                networkCallback
            )
        }

        super.onCleared()
    }

}