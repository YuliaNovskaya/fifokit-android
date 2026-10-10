package com.fifokit.app.ui.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fifokit.app.data.auth.GoogleSignInManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import com.fifokit.app.ui.components.FifokitBackButton
import com.fifokit.app.ui.components.FifokitTopBar
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    authViewModel: AuthViewModel = viewModel(),
    onBack: (() -> Unit)? = null,
    onCreateRoster: (() -> Unit)? = null,
    onSignedIn: () -> Unit = {}
){
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val isBackingUp by authViewModel.isBackingUp.collectAsState()
    val lastBackupAt by authViewModel.lastBackupAt.collectAsState()
    val isSyncing by authViewModel.isSyncing.collectAsState()
    val lastSyncAt by authViewModel.lastSyncAt.collectAsState()

    val isDeletingAccount by authViewModel.isDeletingAccount.collectAsState()

    var showDeleteConfirmation by remember {
        mutableStateOf(false)
    }

    var signInRequested by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(
        currentUser,
        signInRequested
    ) {
        if (
            signInRequested &&
            currentUser != null
        ) {
            signInRequested = false
            onSignedIn()
        }
    }

    val context = LocalContext.current
    val activity = context.findActivity()
    val scope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (onBack != null) {
                FifokitTopBar(
                    title = "Account & cloud sync",
                    onBack = onBack
                )
            }
        }
    ) { innerPadding ->

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (currentUser == null) {

            Text("Sign in to back up and sync your FIFOKIT data")

            Button(
                modifier = Modifier.padding(top = 24.dp),
                enabled = !isLoading && activity != null,
                onClick = {
                    activity ?: return@Button
                    signInRequested = true

                    scope.launch {
                        try {
                            val idToken =
                                GoogleSignInManager(activity)
                                    .getGoogleIdToken()

                            authViewModel.signInWithGoogle(idToken)

                        } catch (e: Exception) {
                            signInRequested = false
                            android.util.Log.e(
                                "FIFOKIT_AUTH",
                                "Google credential sign-in failed",
                                e
                            )
                        }
                    }
                }
            ) {
                Text("Continue with Google")
            }

            if (onCreateRoster != null) {
                OutlinedButton(
                    modifier = Modifier.padding(top = 12.dp),
                    onClick = onCreateRoster
                ) {
                    Text("Create my roster")
                }
            }

        } else {

            Text("Signed in")

            currentUser?.displayName?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            currentUser?.email?.let {
                Text(text = it)
            }

            Button(
                modifier = Modifier.padding(top = 24.dp),
                enabled = !isSyncing && !isBackingUp,
                onClick = authViewModel::syncNow
            ) {
                Text(
                    if (isSyncing) {
                        "Syncing..."
                    } else {
                        "Sync now"
                    }
                )
            }

            lastSyncAt?.let { timestamp ->
                Text(
                    text = "Last sync: ${formatBackupTime(timestamp)}",
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Button(
                modifier = Modifier.padding(top = 24.dp),
                enabled = !isBackingUp && !isSyncing,
                onClick = authViewModel::backupNow
            ) {
                Text(
                    if (isBackingUp) {
                        "Backing up..."
                    } else {
                        "Back up now"
                    }
                )
            }

            lastBackupAt?.let { timestamp ->

                Text(
                    text = "Last backup: ${formatBackupTime(timestamp)}",
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Button(
                modifier = Modifier.padding(top = 24.dp),
                onClick = authViewModel::signOut
            ) {
                Text("Sign out")
            }

            OutlinedButton(
                modifier = Modifier.padding(top = 16.dp),
                enabled =
                    !isDeletingAccount &&
                            !isSyncing &&
                            !isBackingUp,
                onClick = {
                    showDeleteConfirmation = true
                }
            ) {
                Text(
                    if (isDeletingAccount) {
                        "Deleting account..."
                    } else {
                        "Delete account"
                    }
                )
            }

            if (onCreateRoster != null) {
                OutlinedButton(
                    modifier = Modifier.padding(top = 12.dp),
                    onClick = onCreateRoster
                ) {
                    Text("Create my roster")
                }
            }

        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 24.dp)
            )
        }

        errorMessage?.let {
            Text(
                text = it,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
    }
    if (showDeleteConfirmation) {

        AlertDialog(
            onDismissRequest = {
                if (!isDeletingAccount) {
                    showDeleteConfirmation = false
                }
            },
            title = {
                Text("Delete account?")
            },
            text = {
                Text(
                    "This permanently deletes your FIFOKIT cloud data and account."
                )
            },
            confirmButton = {

                TextButton(
                    enabled = !isDeletingAccount,
                    onClick = {

                        activity ?: return@TextButton

                        showDeleteConfirmation = false

                        scope.launch {
                            try {

                                val idToken =
                                    GoogleSignInManager(activity)
                                        .getGoogleIdToken()

                                authViewModel.deleteAccount(
                                    idToken
                                )

                            } catch (e: Exception) {
                                android.util.Log.e(
                                    "FIFOKIT_AUTH",
                                    "Account deletion reauthentication failed",
                                    e
                                )
                            }
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {

                TextButton(
                    enabled = !isDeletingAccount,
                    onClick = {
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

}

private fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

private fun formatBackupTime(
    timestamp: Long
): String {

    return SimpleDateFormat(
        "dd MMM yyyy, HH:mm",
        Locale.getDefault()
    ).format(
        Date(timestamp)
    )
}