package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import com.example.data.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexoTopBar(
    title: String,
    subtitle: String? = null,
    activeRole: UserRole,
    onRoleChange: (UserRole) -> Unit,
    onOpenCustomerView: () -> Unit,
    onOpenAdmin: () -> Unit,
    onResetDemo: () -> Unit,
    onOpenLogin: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showRoleMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.size(38.dp).testTag("burger_menu_button")
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menü", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NexoIndigoPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "N",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = NexoIndigoPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "PRO",
                                    color = NexoIndigoLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = NexoDarkTextSecondary
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Customer QR Menu Simulation Button
                    OutlinedButton(
                        onClick = onOpenCustomerView,
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("customer_menu_preview_button"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NexoEmeraldLight
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(NexoEmeraldLight.copy(alpha = 0.5f))
                        )
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Masa QR Görünümü", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Role Switcher Button
                    Box {
                        Surface(
                            onClick = { showRoleMenu = true },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("role_switcher_dropdown")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(NexoEmerald)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activeRole.titleTr,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }

                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false }
                        ) {
                            Text(
                                text = "Çalışma Modu & Rol Değiştir",
                                style = MaterialTheme.typography.labelSmall,
                                color = NexoDarkTextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            UserRole.entries.forEach { role ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (role == activeRole) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = NexoIndigoPrimary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            Text(role.titleTr, fontWeight = if (role == activeRole) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    },
                                    onClick = {
                                        onRoleChange(role)
                                        showRoleMenu = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Hesap / Giriş & Restoran Kayıt", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    showRoleMenu = false
                                    onOpenLogin()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Superadmin Paneli", color = NexoIndigoLight) },
                                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = NexoIndigoLight) },
                                onClick = {
                                    showRoleMenu = false
                                    onOpenAdmin()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Demo Verileri Sıfırla", color = NexoRose) },
                                leadingIcon = { Icon(Icons.Default.RestartAlt, contentDescription = null, tint = NexoRose) },
                                onClick = {
                                    showRoleMenu = false
                                    onResetDemo()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color = NexoIndigoLight,
    badgeColor: Color = NexoIndigoPrimary.copy(alpha = 0.15f),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = NexoDarkTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = NexoEmeraldLight,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val bgColor = Color(status.colorHex).copy(alpha = 0.15f)
    val textColor = Color(status.colorHex)

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = status.titleTr,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun TableQrCardView(
    tableNumber: Int,
    label: String,
    token: String,
    brandColor: Color,
    onRegenerate: () -> Unit,
    onDelete: () -> Unit,
    onPreviewCustomerMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Sil", tint = NexoRose, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Deterministic Real QR Code Canvas Generator
            val qrPayload = remember(tableNumber, token) { "https://nexo.app/menu/table/$tableNumber?token=$token" }
            val qrMatrix = remember(qrPayload) { DeterministicQrMatrix.generateMatrix(qrPayload) }
            val context = androidx.compose.ui.platform.LocalContext.current

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(2.dp, brandColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellSize = size.width / DeterministicQrMatrix.MATRIX_SIZE
                    for (r in 0 until DeterministicQrMatrix.MATRIX_SIZE) {
                        for (c in 0 until DeterministicQrMatrix.MATRIX_SIZE) {
                            if (qrMatrix[r][c]) {
                                drawRect(
                                    color = Color(0xFF1E293B),
                                    topLeft = Offset(c * cellSize, r * cellSize),
                                    size = Size(cellSize, cellSize)
                                )
                            }
                        }
                    }
                }
                // Center Table Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = brandColor,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$tableNumber",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "nexo.app/menu/table/$tableNumber",
                fontSize = 11.sp,
                color = NexoDarkTextSecondary,
                maxLines = 1
            )
            Text(
                text = "Jeton: $token",
                fontSize = 10.sp,
                color = NexoDarkTextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        saveQrToDevice(context, qrPayload, "Masa $tableNumber Karekod")
                    },
                    modifier = Modifier.weight(1f).height(34.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("İndir", fontSize = 11.sp)
                }
                Button(
                    onClick = onPreviewCustomerMenu,
                    modifier = Modifier.weight(1f).height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Test Et", fontSize = 11.sp)
                }
            }
        }
    }
}
