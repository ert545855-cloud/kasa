package com.example.ui.components

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AuthRepository
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@Composable
fun GoogleSignInCard(
    authRepository: AuthRepository,
    onSignInSuccess: (FirebaseUser) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by authRepository.currentUser.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (currentUser != null) {
                // Logged in State
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NexoEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (currentUser?.displayName?.take(1) ?: "U").uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = NexoEmeraldLight
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser?.displayName ?: "Kullanıcı",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NexoEmerald, modifier = Modifier.size(14.dp))
                            }
                            Text(
                                text = currentUser?.email ?: "",
                                fontSize = 11.sp,
                                color = NexoDarkTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                authRepository.logout()
                            }
                        },
                        modifier = Modifier.testTag("auth_sign_out_button")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Çıkış Yap", tint = NexoRose, modifier = Modifier.size(18.dp))
                    }
                }
            } else {
                // Not Logged in State
                Text(
                    text = "Firebase & Google ile Kimlik Doğrulama",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "İşletmenizi güvenceye alın, bulut senkronizasyonunu başlatın.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NexoDarkTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        val activity = context as? Activity
                        if (activity != null) {
                            coroutineScope.launch {
                                val result = authRepository.loginWithGoogle(activity)
                                isLoading = false
                                result.onSuccess { user ->
                                    onSignInSuccess(user)
                                }.onFailure { error ->
                                    errorMessage = error.localizedMessage ?: "Giriş yapılamadı"
                                }
                            }
                        } else {
                            isLoading = false
                            errorMessage = "Activity bulunamadı."
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("google_sign_in_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Giriş Yapılıyor...")
                    } else {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google ile Giriş Yap", fontWeight = FontWeight.Bold)
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 11.sp,
                        color = NexoRose
                    )
                }
            }
        }
    }
}
