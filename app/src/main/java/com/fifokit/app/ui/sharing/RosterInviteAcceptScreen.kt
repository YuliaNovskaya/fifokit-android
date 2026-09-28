package com.fifokit.app.ui.sharing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect

@Composable
fun RosterInviteAcceptScreen(
    inviteId: String,
    onAccepted: () -> Unit,
    viewModel: RosterInviteViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(inviteId) {
        viewModel.clearState()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "FIFOKIT roster invite",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            state.isLoading -> {
                CircularProgressIndicator()
            }

            state.accepted -> {
                Text(
                    text = "Roster added",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onAccepted
                ) {
                    Text("View roster")
                }
            }

            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {
                Text(
                    text = "A FIFOKIT user has shared their roster with you."
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.acceptInvite(inviteId)
                    }
                ) {
                    Text("Accept invite")
                }
            }
        }
    }
}