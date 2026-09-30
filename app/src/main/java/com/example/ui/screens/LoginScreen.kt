package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import com.example.data.repository.NexoRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: () -> Unit,
    onOpenLiveDemo: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by authRepository.currentUser.collectAsState()

    var isRegisterMode by remember { mutableStateOf(false) }

    // Login Form State
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Register Form State
    var restaurantNameInput by remember { mutableStateOf("") }
    var ownerNameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("+90 ") }
    var selectedCurrency by remember { mutableStateOf("₺") }

    // Role state
    var selectedRole by remember { mutableStateOf(UserRole.OWNER) }

    // UI feedback
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmailInput by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        if (currentUser != null && errorMessage == null) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NexoWarmBeige),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 500.dp)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row with Skip / Later option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp)) // balance placeholder
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(NexoBurgundy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = "NEXO Restaurant OS",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    TextButton(
                        onClick = onOpenLiveDemo,
                        modifier = Modifier.testTag("skip_login_top_button")
                    ) {
                        Text("Atla / Geç", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NexoBurgundy)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "NEXO RESTAURANT OS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = NexoTextPrimary,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "Restoranınızın tüm operasyonu tek platformda.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NexoBurgundy,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Menüden siparişe, masadan mutfağa.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NexoTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Role selector
                Text(
                    text = "Giriş Rolü:",
                    style = MaterialTheme.typography.labelMedium,
                    color = NexoTextSecondary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        UserRole.OWNER to "Yönetici",
                        UserRole.EMPLOYEE to "Garson",
                        UserRole.KITCHEN to "Mutfak",
                        UserRole.CASHIER to "Kasa"
                    ).forEach { (role, label) ->
                        val isSelected = selectedRole == role
                        Surface(
                            onClick = {
                                selectedRole = role
                                NexoRepository.setStaffRole(role)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) NexoBurgundy else NexoLightGray,
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .testTag("role_btn_${role.name.lowercase()}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else NexoTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Switcher (Giriş Yap vs Restoran Aç)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NexoLightGray)
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isRegisterMode) Color.White else Color.Transparent)
                            .clickable {
                                isRegisterMode = false
                                errorMessage = null
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Giriş Yap",
                            fontWeight = if (!isRegisterMode) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (!isRegisterMode) NexoTextPrimary else NexoTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isRegisterMode) Color.White else Color.Transparent)
                            .clickable {
                                isRegisterMode = true
                                errorMessage = null
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Restoran Aç (Kayıt)",
                            fontWeight = if (isRegisterMode) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (isRegisterMode) NexoTextPrimary else NexoTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error / Success Banners
                if (errorMessage != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        color = NexoRoseRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = NexoRoseRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = NexoRoseRed,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                if (successMessage != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        color = NexoSoftGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NexoSoftGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = successMessage ?: "",
                                color = NexoSoftGreen,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Registration Fields
                if (isRegisterMode) {
                    OutlinedTextField(
                        value = restaurantNameInput,
                        onValueChange = { restaurantNameInput = it },
                        label = { Text("Restoran Adı *") },
                        placeholder = { Text("Örn: Bella Vista Trattoria") },
                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_restaurant_name_input"),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = ownerNameInput,
                        onValueChange = { ownerNameInput = it },
                        label = { Text("Yetkili Adı Soyadı") },
                        placeholder = { Text("Örn: Mehmet Yılmaz") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_owner_name_input"),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Telefon") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_phone_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Email & Password Fields
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("E-posta Adresi *") },
                    placeholder = { Text("restoran@ornek.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Şifre *") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Şifreyi Gizle" else "Şifreyi Göster"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                if (!isRegisterMode) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), contentAlignment = Alignment.CenterEnd) {
                        TextButton(
                            onClick = {
                                resetEmailInput = emailInput
                                showForgotPasswordDialog = true
                            }
                        ) {
                            Text("Şifremi Unuttum?", fontSize = 12.sp, color = NexoBurgundy)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Primary Email Auth Action Button
                Button(
                    onClick = {
                        errorMessage = null
                        successMessage = null
                        if (emailInput.isBlank() || passwordInput.isBlank()) {
                            errorMessage = "Lütfen e-posta ve şifre alanlarını doldurun."
                            return@Button
                        }
                        if (isRegisterMode && restaurantNameInput.isBlank()) {
                            errorMessage = "Lütfen restoran adını girin."
                            return@Button
                        }

                        isLoading = true
                        coroutineScope.launch {
                            if (isRegisterMode) {
                                val result = authRepository.registerWithEmail(
                                    email = emailInput,
                                    password = passwordInput,
                                    displayName = ownerNameInput,
                                    restaurantName = restaurantNameInput,
                                    phone = phoneInput,
                                    currency = selectedCurrency
                                )
                                isLoading = false
                                result.onSuccess {
                                    successMessage = "Restoran başarıyla oluşturuldu! Giriş yapılıyor..."
                                    onLoginSuccess()
                                }.onFailure { error ->
                                    errorMessage = error.localizedMessage ?: "Restoran kaydı oluşturulamadı."
                                }
                            } else {
                                val result = authRepository.loginWithEmail(emailInput, passwordInput)
                                isLoading = false
                                result.onSuccess {
                                    onLoginSuccess()
                                }.onFailure { error ->
                                    errorMessage = error.localizedMessage ?: "E-posta veya şifre hatalı."
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = NexoBurgundy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("İşleniyor...", fontWeight = FontWeight.Bold)
                    } else {
                        Text(
                            text = if (isRegisterMode) "Restoranı Oluştur ve Başla" else "Restoran Paneline Giriş Yap",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = NexoBorderLight)
                    Text(
                        text = "VEYA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NexoTextMuted,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = NexoBorderLight)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Google Sign-In with Firebase Auth & Credential Manager
                OutlinedButton(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        val activity = context as? Activity
                        if (activity != null) {
                            coroutineScope.launch {
                                val result = authRepository.loginWithGoogle(activity)
                                isLoading = false
                                result.onSuccess {
                                    if (isRegisterMode && restaurantNameInput.isNotBlank()) {
                                        authRepository.registerBusiness(
                                            businessName = restaurantNameInput,
                                            businessType = "RESTAURANT",
                                            phone = phoneInput,
                                            address = "İstanbul, Türkiye",
                                            currency = selectedCurrency
                                        )
                                    }
                                    onLoginSuccess()
                                }.onFailure { error ->
                                    errorMessage = error.localizedMessage ?: "Google girişi tamamlanamadı."
                                }
                            }
                        } else {
                            isLoading = false
                            errorMessage = "Activity oturumu bulunamadı."
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("login_screen_google_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NexoTextPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(NexoBorderLight)
                    )
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = NexoBurgundy
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRegisterMode) "Google ile Restoranı Kaydet" else "Google ile Güvenli Giriş Yap",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fast One-Click Demo Mode button
                TextButton(
                    onClick = {
                        NexoRepository.resetToDemoData()
                        onOpenLiveDemo()
                    },
                    modifier = Modifier.testTag("login_screen_demo_btn")
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = NexoTerracotta, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Daha Sonra Giriş Yap / İçeriklere Geç (Canlı Demo)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = NexoTerracotta
                    )
                }
            }
        }
    }

    // Password Reset Dialog
    if (showForgotPasswordDialog) {
        var resetSent by remember { mutableStateOf(false) }
        var resetError by remember { mutableStateOf<String?>(null) }
        var isResetting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Şifre Sıfırlama") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (resetSent) {
                        Text(
                            text = "Şifre sıfırlama bağlantısı e-posta adresinize gönderildi. Lütfen gelen kutunuzu kontrol edin.",
                            color = NexoSoftGreen,
                            fontSize = 13.sp
                        )
                    } else {
                        Text("Hesabınıza ait e-posta adresini girin, size sıfırlama bağlantısı gönderelim:")
                        OutlinedTextField(
                            value = resetEmailInput,
                            onValueChange = { resetEmailInput = it },
                            label = { Text("E-posta") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (resetError != null) {
                            Text(resetError ?: "", color = NexoRoseRed, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                if (!resetSent) {
                    Button(
                        onClick = {
                            if (resetEmailInput.isBlank()) {
                                resetError = "Lütfen e-posta girin."
                                return@Button
                            }
                            isResetting = true
                            resetError = null
                            coroutineScope.launch {
                                val res = authRepository.sendPasswordReset(resetEmailInput)
                                isResetting = false
                                res.onSuccess {
                                    resetSent = true
                                }.onFailure { err ->
                                    resetError = err.localizedMessage ?: "Sıfırlama e-postası gönderilemedi."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NexoBurgundy)
                    ) {
                        Text(if (isResetting) "Gönderiliyor..." else "Bağlantı Gönder")
                    }
                } else {
                    Button(
                        onClick = { showForgotPasswordDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = NexoBurgundy)
                    ) {
                        Text("Tamam")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}
