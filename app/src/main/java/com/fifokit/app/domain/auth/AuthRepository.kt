package com.fifokit.app.domain.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val currentUser: Flow<AuthUser?>

    suspend fun signInWithGoogle(idToken: String): AuthUser

    suspend fun signOut()
}