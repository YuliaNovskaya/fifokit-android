package com.fifokit.app.ui.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fifokit.app.data.cloud.RosterInviteManager
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
    private val inviteManager: RosterInviteManager = RosterInviteManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RosterInviteUiState())
    val uiState: StateFlow<RosterInviteUiState> = _uiState.asStateFlow()

    fun createInvite(
        rosterId: String,
        rosterName: String
    ) {
        viewModelScope.launch {
            _uiState.value = RosterInviteUiState(
                isLoading = true
            )

            runCatching {
                inviteManager.createInvite(
                    rosterId = rosterId,
                    rosterName = rosterName
                )
            }.onSuccess { inviteId ->
                _uiState.value = RosterInviteUiState(
                    inviteId = inviteId
                )
            }.onFailure { error ->
                _uiState.value = RosterInviteUiState(
                    errorMessage = error.message ?: "Unable to create invite"
                )
            }
        }
    }

    fun acceptInvite(
        inviteId: String
    ) {
        viewModelScope.launch {
            _uiState.value = RosterInviteUiState(
                isLoading = true
            )

            runCatching {
                inviteManager.acceptInvite(inviteId)
            }.onSuccess {
                _uiState.value = RosterInviteUiState(
                    accepted = true
                )
            }.onFailure { error ->
                _uiState.value = RosterInviteUiState(
                    errorMessage = error.message ?: "Unable to accept invite"
                )
            }
        }
    }

    fun clearState() {
        _uiState.value = RosterInviteUiState()
    }
}