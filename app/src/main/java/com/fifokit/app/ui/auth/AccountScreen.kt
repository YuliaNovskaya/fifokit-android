package com.fifokit.app.ui.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

@Composable
fun AccountScreen(
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    val context = LocalContext.current
    val activity = context.findActivity()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
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

                    scope.launch {
                        try {
                            val idToken =
                                GoogleSignInManager(activity)
                                    .getGoogleIdToken()

                            authViewModel.signInWithGoogle(idToken)

                        } catch (e: Exception) {
                            // Credential Manager may also throw if the user
                            // closes the Google sign-in dialog.
                        }
                    }
                }
            ) {
                Text("Continue with Google")
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
                onClick = authViewModel::signOut
            ) {
                Text("Sign out")
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

private fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}