package com.example.ui.components

import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.repository.AuthRepository
import com.example.ui.theme.*
import com.example.util.ProfilePhotoUtils
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
    var photoSuccessMessage by remember { mutableStateOf<String?>(null) }
    var isUpdatingPhoto by remember { mutableStateOf(false) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Camera picture capture launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            coroutineScope.launch {
                isUpdatingPhoto = true
                errorMessage = null
                photoSuccessMessage = null
                try {
                    val persistedUri = ProfilePhotoUtils.persistProfileImage(context, tempCameraUri!!)
                    val result = authRepository.updateProfilePhoto(persistedUri)
                    result.onSuccess {
                        photoSuccessMessage = "Profil fotoğrafı kameradan çekildi ve Firebase profiline kaydedildi!"
                    }.onFailure { error ->
                        errorMessage = "Profil fotoğrafı kaydedilemedi: ${error.localizedMessage}"
                    }
                } catch (e: Exception) {
                    errorMessage = "Fotoğraf işlenirken hata oluştu: ${e.localizedMessage}"
                } finally {
                    isUpdatingPhoto = false
                }
            }
        }
    }

    // Camera permission request launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = ProfilePhotoUtils.createCameraImageUri(context)
            tempCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            showPermissionRationaleDialog = true
        }
    }

    // Photo picker launcher (Gallery option)
    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isUpdatingPhoto = true
                errorMessage = null
                photoSuccessMessage = null
                try {
                    val persistedUri = ProfilePhotoUtils.persistProfileImage(context, uri)
                    val result = authRepository.updateProfilePhoto(persistedUri)
                    result.onSuccess {
                        photoSuccessMessage = "Profil fotoğrafı güncellendi ve Firebase profiline kaydedildi!"
                    }.onFailure { error ->
                        errorMessage = "Profil fotoğrafı kaydedilemedi: ${error.localizedMessage}"
                    }
                } catch (e: Exception) {
                    errorMessage = "Fotoğraf seçilirken hata oluştu: ${e.localizedMessage}"
                } finally {
                    isUpdatingPhoto = false
                }
            }
        }
    }

    fun startCameraCapture() {
        val permissionStatus = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            val uri = ProfilePhotoUtils.createCameraImageUri(context)
            tempCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // User Avatar with Camera Badge
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .clickable { showPhotoOptionsDialog = true }
                                .testTag("profile_avatar_box")
                        ) {
                            val photoUrl = currentUser?.photoUrl
                            if (photoUrl != null) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Profil Fotoğrafı",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .border(2.dp, NexoEmerald, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(NexoEmerald.copy(alpha = 0.2f))
                                        .border(1.5.dp, NexoEmeraldLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (currentUser?.displayName?.take(1) ?: "U").uppercase(),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = NexoEmeraldLight
                                    )
                                }
                            }

                            // Camera Overlay Badge
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                shadowElevation = 3.dp,
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.BottomEnd)
                                    .testTag("avatar_camera_badge")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.PhotoCamera,
                                        contentDescription = "Fotoğrafı Değiştir",
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
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
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Fotoğrafı güncellemek için dokunun",
                                fontSize = 10.sp,
                                color = NexoIndigoLight,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { showPhotoOptionsDialog = true }
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

                // Camera Action Buttons
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { startCameraCapture() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("capture_camera_photo_btn"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isUpdatingPhoto
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kameradan Çek", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            pickVisualMediaLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pick_gallery_photo_btn"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isUpdatingPhoto
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Galeriden Seç", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (isUpdatingPhoto) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Profil fotoğrafı Firebase'e kaydediliyor...", fontSize = 11.sp, color = NexoIndigoLight)
                    }
                }

                if (photoSuccessMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = NexoEmerald.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NexoEmeraldLight, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(photoSuccessMessage ?: "", fontSize = 11.sp, color = NexoEmeraldLight, fontWeight = FontWeight.SemiBold)
                        }
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
                        val activity = context as? Activity
                        if (activity != null) {
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                val result = authRepository.loginWithGoogle(activity)
                                isLoading = false
                                result.onSuccess { user ->
                                    onSignInSuccess(user)
                                }.onFailure { error ->
                                    val msg = error.message.orEmpty()
                                    val isCancelled = error is androidx.credentials.exceptions.GetCredentialCancellationException ||
                                            msg.contains("cancelled", ignoreCase = true) ||
                                            msg.contains("canceled", ignoreCase = true) ||
                                            msg.contains("activity is cancelled", ignoreCase = true) ||
                                            msg.contains("16:", ignoreCase = true)
                                    if (!isCancelled) {
                                        errorMessage = error.localizedMessage ?: "Google girişi tamamlanamadı."
                                    }
                                }
                            }
                        } else {
                            errorMessage = "Activity bulunamadı."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("google_login_button"),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google ile Giriş Yap", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = NexoRose.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = NexoRose,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }

    // Photo Source Choice Dialog
    if (showPhotoOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoOptionsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = NexoIndigoPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Profil Fotoğrafını Güncelle")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Profil fotoğrafınızı kamera ile anında çekebilir veya galeriden seçebilirsiniz. Fotoğraf URI'si doğrudan Firebase kullanıcı profilinize kaydedilir.", fontSize = 12.sp, color = NexoDarkTextSecondary)

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = {
                            showPhotoOptionsDialog = false
                            startCameraCapture()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kamera ile Fotoğraf Çek", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            showPhotoOptionsDialog = false
                            pickVisualMediaLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Galeriden Fotoğraf Seç", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoOptionsDialog = false }) { Text("İptal") }
            }
        )
    }

    // Permission Rationale Dialog
    if (showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionRationaleDialog = false },
            icon = { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = NexoRose, modifier = Modifier.size(32.dp)) },
            title = { Text("Kamera İzni Gerekli") },
            text = {
                Text(
                    "Profil fotoğrafınızı doğrudan kameradan çekebilmek için kamera izni vermeniz gerekmektedir. İzin vererek devam edebilirsiniz.",
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionRationaleDialog = false
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary)
                ) {
                    Text("İzin İste")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationaleDialog = false }) { Text("Vazgeç") }
            }
        )
    }
}
