package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat

@Composable
fun CameraQrScannerDialog(
    onDismiss: () -> Unit,
    onQrCodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Scanning laser animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("scanner_close_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                }

                Text(
                    text = "Karekod / Barkod Tara",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                IconButton(
                    onClick = {
                        // Quick demo scan for testing
                        onQrCodeDetected("nexo-table-03")
                    },
                    modifier = Modifier.testTag("scanner_quick_scan_button")
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = "Hızlı Test", tint = Color(0xFFF59E0B))
                }
            }

            // Center Scanner Viewfinder
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (hasCameraPermission) {
                    Text(
                        text = "Masa karekodunu veya ürün barkodunu çerçevenin içine hizalayın",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Target Box with Corner Brackets
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                            .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(20.dp))
                    ) {
                        // Animated Scanning Laser Line
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val lineY = size.height * laserY
                            drawLine(
                                color = Color(0xFF38BDF8),
                                start = Offset(10f, lineY),
                                end = Offset(size.width - 10f, lineY),
                                strokeWidth = 4f
                            )
                        }

                        // Corner Marks
                        Icon(
                            Icons.Default.CropFree,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8).copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxSize().padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Manual Table Select Chips
                    Text("Veya masayı doğrudan seçin:", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 2, 3, 5, 8).forEach { num ->
                            Button(
                                onClick = { onQrCodeDetected("nexo-table-$num") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp).testTag("select_table_${num}_btn")
                            ) {
                                Text("Masa $num", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                } else {
                    // Permission Request UI
                    Card(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Kamera İzni Gerekli",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Masa karekodlarını ve ürün barkodlarını okutabilmek için kamera erişimine izin verilmelidir.",
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                modifier = Modifier.testTag("grant_camera_permission_btn")
                            ) {
                                Text("Kamera İznini Aç")
                            }
                        }
                    }
                }
            }
        }
    }
}
