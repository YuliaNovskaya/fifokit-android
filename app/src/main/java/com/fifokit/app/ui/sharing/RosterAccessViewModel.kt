package com.fifokit.app.ui.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fifokit.app.data.cloud.RosterSharingManager
import com.fifokit.app.data.cloud.model.CloudRosterAccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RosterAccessUiState(
    val isLoading: Boolean = false,
    val shares: List<CloudRosterAccess> = emptyList(),
    val errorMessage: String? = null
)

class RosterAccessViewModel(
    private val manager: RosterSharingManager = RosterSharingManager()
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(RosterAccessUiState())

    val uiState: StateFlow<RosterAccessUiState> =
        _uiState.asStateFlow()

    fun loadShares(
        rosterId: String
    ) {
        viewModelScope.launch {
            _uiState.value =
                RosterAccessUiState(
                    isLoading = true
                )

            runCatching {
                manager.getRosterShares(rosterId)
            }.onSuccess { shares ->
                _uiState.value =
                    RosterAccessUiState(
                        shares = shares
                    )
            }.onFailure { error ->
                _uiState.value =
                    RosterAccessUiState(
                        errorMessage =
                            error.message
                                ?: "Unable to load sharing access"
                    )
            }
        }
    }

    fun revokeAccess(
        rosterId: String,
        userId: String
    ) {
        viewModelScope.launch {
            runCatching {
                manager.revokeViewerAccess(
                    rosterId = rosterId,
                    viewerUserId = userId
                )
            }.onSuccess {
                loadShares(rosterId)
            }.onFailure { error ->
                _uiState.value =
                    _uiState.value.copy(
                        errorMessage =
                            error.message
                                ?: "Unable to remove access"
                    )
            }
        }
    }
}