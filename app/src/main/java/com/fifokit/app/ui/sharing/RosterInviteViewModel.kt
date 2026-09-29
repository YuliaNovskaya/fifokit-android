package com.fifokit.app.ui.sharing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fifokit.app.data.RosterPreferences
import com.fifokit.app.data.RosterRepository
import com.fifokit.app.data.cloud.RosterCloudSyncManager
import com.fifokit.app.data.cloud.RosterInviteManager
import com.fifokit.app.data.local.RosterDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RosterInviteUiState(
    val isLoading: Boolean = false,
    val inviteId: String? = null,
    val accepted: Boolean = false,
    val errorMessage: String? = null
)

class RosterInviteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val inviteManager =
        RosterInviteManager()

    private val auth =
        FirebaseAuth.getInstance()

    private val rosterDatabase =
        RosterDatabase.getInstance(application)

    private val rosterRepository =
        RosterRepository(
            rosterDatabase.rosterDao()
        )

    private val rosterPreferences =
        RosterPreferences(application)

    private val rosterCloudSyncManager =
        RosterCloudSyncManager(
            context = application,
            rosterRepository = rosterRepository,
            rosterPreferences = rosterPreferences
        )

    private val _uiState =
        MutableStateFlow(
            RosterInviteUiState()
        )

    val uiState: StateFlow<RosterInviteUiState> =
        _uiState.asStateFlow()

    fun createInvite(
        rosterId: String,
        rosterName: String
    ) {
        viewModelScope.launch {

            _uiState.value =
                RosterInviteUiState(
                    isLoading = true
                )

            runCatching {

                val uid =
                    auth.currentUser?.uid
                        ?: error(
                            "Sign in before sharing a roster"
                        )

                // Ensure newly created/edited rosters
                // exist in Firestore before the invite
                // is created.
                rosterCloudSyncManager.sync(uid)

                inviteManager.createInvite(
                    rosterId = rosterId,
                    rosterName = rosterName
                )

            }.onSuccess { inviteId ->

                _uiState.value =
                    RosterInviteUiState(
                        inviteId = inviteId
                    )

            }.onFailure { error ->

                _uiState.value =
                    RosterInviteUiState(
                        errorMessage =
                            error.message
                                ?: "Unable to create invite"
                    )
            }
        }
    }

    fun acceptInvite(
        inviteId: String
    ) {
        viewModelScope.launch {

            _uiState.value =
                RosterInviteUiState(
                    isLoading = true
                )

            runCatching {

                inviteManager.acceptInvite(
                    inviteId
                )

            }.onSuccess {

                _uiState.value =
                    RosterInviteUiState(
                        accepted = true
                    )

            }.onFailure { error ->

                _uiState.value =
                    RosterInviteUiState(
                        errorMessage =
                            error.message
                                ?: "Unable to accept invite"
                    )
            }
        }
    }

    fun clearState() {
        _uiState.value =
            RosterInviteUiState()
    }
}