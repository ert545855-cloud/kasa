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
import com.example.NexoApplication
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthService(private val context: Context) {

    private val auth: FirebaseAuth? by lazy {
        try {
            NexoApplication.initializeFirebaseSafely(context)
            Firebase.auth
        } catch (e: Throwable) {
            Log.e("FirebaseAuthService", "Firebase Auth initialization skipped or failed", e)
            null
        }
    }
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        try {
            auth?.let { firebaseAuth ->
                _currentUser.value = firebaseAuth.currentUser
                firebaseAuth.addAuthStateListener { updatedAuth ->
                    _currentUser.value = updatedAuth.currentUser
                }
            }
        } catch (e: Throwable) {
            Log.w("FirebaseAuthService", "Could not attach auth state listener", e)
        }
    }

    val isAuthenticated: Boolean
        get() = _currentUser.value != null

    fun getRequiredUserId(): String {
        return auth?.currentUser?.uid
            ?: "guest_user_${System.currentTimeMillis() % 10000}"
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
            val currentAuth = auth ?: return Result.failure(Exception("Firebase Auth yapılandırılmamış."))
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = currentAuth.signInWithCredential(authCredential).await()
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
            Log.d("FirebaseAuthService", "Google Sign-In flow cancelled by user: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val msg = e.message.orEmpty()
            if (msg.contains("cancelled", ignoreCase = true) ||
                msg.contains("canceled", ignoreCase = true) ||
                msg.contains("activity is cancelled", ignoreCase = true)
            ) {
                Log.d("FirebaseAuthService", "Google Sign-In cancelled by user: $msg")
                Result.failure(GetCredentialCancellationException(msg))
            } else {
                Log.e("FirebaseAuthService", "Google Sign-In failed", e)
                Result.failure(e)
            }
        }
    }

    suspend fun attemptSilentSignIn(): Result<FirebaseUser> {
        val currentAuth = auth ?: return Result.failure(Exception("Firebase Auth yapılandırılmamış."))
        val currentUserNow = currentAuth.currentUser
        if (currentUserNow != null) {
            _currentUser.value = currentUserNow
            return Result.success(currentUserNow)
        }

        // If no user is cached by Firebase Auth locally, return immediately without invoking CredentialManager
        // which would otherwise prompt or fail with 'activity is cancelled by the user' in the background
        return Result.failure(Exception("Aktif kullanıcı oturumu yok."))
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val currentAuth = auth ?: throw Exception("Firebase Auth servisi yapılandırılmamış.")
            val authResult = currentAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw Exception("Kullanıcı bilgisi alınamadı.")
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("FirebaseAuthService", "Email login failed", e)
            Result.failure(e)
        }
    }

    suspend fun signInAnonymouslyOrDemo(displayName: String): Result<FirebaseUser> {
        return try {
            val currentAuth = auth ?: throw Exception("Firebase Auth servisi yapılandırılmamış.")
            val authResult = currentAuth.signInAnonymously().await()
            val user = authResult.user ?: throw Exception("Demo oturumu oluşturulamadı.")
            if (displayName.isNotBlank()) {
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                user.updateProfile(profileUpdate).await()
            }
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("FirebaseAuthService", "Demo sign in failed", e)
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            val currentAuth = auth ?: throw Exception("Firebase Auth servisi yapılandırılmamış.")
            val authResult = currentAuth.createUserWithEmailAndPassword(email.trim(), password).await()
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
            if (e is CancellationException) throw e
            Log.e("FirebaseAuthService", "Email sign up failed", e)
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            val currentAuth = auth ?: throw Exception("Firebase Auth servisi yapılandırılmamış.")
            currentAuth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("FirebaseAuthService", "Password reset failed", e)
            Result.failure(e)
        }
    }

    suspend fun updateProfilePhoto(photoUri: android.net.Uri): Result<FirebaseUser> {
        return try {
            val currentAuth = auth ?: throw Exception("Firebase Auth servisi yapılandırılmamış.")
            val user = currentAuth.currentUser ?: throw Exception("Aktif oturum bulunamadı.")
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setPhotoUri(photoUri)
                .build()
            user.updateProfile(profileUpdate).await()
            user.reload().await()
            val refreshedUser = currentAuth.currentUser ?: user
            _currentUser.value = refreshedUser
            Result.success(refreshedUser)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("FirebaseAuthService", "Profile photo update failed", e)
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            auth?.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            _currentUser.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("FirebaseAuthService", "Sign out error", e)
            Result.failure(e)
        }
    }
}
