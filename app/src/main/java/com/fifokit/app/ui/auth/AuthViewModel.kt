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

class AuthViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val cloudRepository = CloudRepository()

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