package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthService(private val context: Context) {

    private val auth: FirebaseAuth = Firebase.auth
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    val isAuthenticated: Boolean
        get() = _currentUser.value != null

    fun getRequiredUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("İşlem için oturum açılmış olmalıdır.")
    }

    suspend fun signInWithGoogle(activity: Activity): Result<FirebaseUser> {
        val clientId = try {
            activity.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            return Result.failure(IllegalStateException("Google Sign-In yapılandırması eksik: default_web_client_id bulunamadı."))
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        return try {
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    _currentUser.value = user
                    Result.success(user)
                } else {
                    Result.failure(Exception("Kullanıcı kimliği alınamadı."))
                }
            } else {
                Result.failure(Exception("Beklenmeyen kimlik türü."))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("FirebaseAuthService", "Google Sign-In flow cancelled: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    suspend fun attemptSilentSignIn(): Result<FirebaseUser> {
        if (auth.currentUser != null) {
            _currentUser.value = auth.currentUser
            return Result.success(auth.currentUser!!)
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    _currentUser.value = user
                    Result.success(user)
                } else {
                    Result.failure(Exception("Kullanıcı bulunamadı"))
                }
            } else {
                Result.failure(Exception("Yetkilendirilmiş hesap bulunamadı"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw Exception("Kullanıcı bilgisi alınamadı.")
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Email login failed", e)
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw Exception("Kullanıcı kaydı oluşturulamadı.")
            if (displayName.isNotBlank()) {
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                user.updateProfile(profileUpdate).await()
            }
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Email sign up failed", e)
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Password reset failed", e)
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            _currentUser.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Sign out error", e)
            Result.failure(e)
        }
    }
}
