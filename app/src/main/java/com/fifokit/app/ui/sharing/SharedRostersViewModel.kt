package com.fifokit.app.ui.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fifokit.app.data.cloud.SharedRosterManager
import com.fifokit.app.domain.sharing.SharedRoster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SharedRostersUiState(
    val isLoading: Boolean = false,
    val rosters: List<SharedRoster> = emptyList(),
    val errorMessage: String? = null
)

class SharedRostersViewModel(
    private val manager: SharedRosterManager = SharedRosterManager()
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(SharedRostersUiState())

    val uiState: StateFlow<SharedRostersUiState> =
        _uiState.asStateFlow()

    fun loadSharedRosters() {
        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

            runCatching {
                manager.loadSharedRosters()
            }.onSuccess { rosters ->

                _uiState.value =
                    SharedRostersUiState(
                        isLoading = false,
                        rosters = rosters
                    )

            }.onFailure {

                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage =
                            "Shared rosters could not be refreshed"
                    )
            }
        }
    }
}