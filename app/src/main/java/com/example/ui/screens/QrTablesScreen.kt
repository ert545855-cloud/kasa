package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TableQr
import com.example.data.repository.NexoRepository
import com.example.ui.components.TableQrCardView
import com.example.ui.theme.*

@Composable
fun QrTablesScreen(
    onPreviewCustomerMenu: (Int) -> Unit
) {
    val tables by NexoRepository.tables.collectAsState()
    val business = NexoRepository.getActiveBusiness()

    var showAddTableDialog by remember { mutableStateOf(false) }
    var showPrintSheetDialog by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTableDialog = true },
                containerColor = NexoIndigoPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_table_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Masa QR Ekle")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Masa QR Kodları",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${tables.size} aktif masa tanımlı • Deterministik QR",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexoDarkTextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { showScannerDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("open_qr_scanner_btn")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kamera Tara", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showPrintSheetDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Yazdır", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tables) { table ->
                    TableQrCardView(
                        tableNumber = table.tableNumber,
                        label = table.label,
                        token = table.secureToken,
                        brandColor = if (table.qrDesignColor.startsWith("#")) {
                            try {
                                Color(android.graphics.Color.parseColor(table.qrDesignColor))
                            } catch (e: Exception) {
                                NexoIndigoPrimary
                            }
                        } else NexoIndigoPrimary,
                        onRegenerate = { NexoRepository.regenerateQrToken(table.id) },
                        onDelete = { NexoRepository.deleteTable(table.id) },
                        onPreviewCustomerMenu = { onPreviewCustomerMenu(table.tableNumber) }
                    )
                }
            }
        }
    }

    // Add Table Dialog
    if (showAddTableDialog) {
        var tableNum by remember { mutableStateOf("${tables.size + 1}") }
        var tableLabel by remember { mutableStateOf("Masa ${tables.size + 1}") }

        AlertDialog(
            onDismissRequest = { showAddTableDialog = false },
            title = { Text("Yeni Masa QR Ekle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tableNum,
                        onValueChange = {
                            tableNum = it
                            tableLabel = "Masa $it"
                        },
                        label = { Text("Masa Numarası") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tableLabel,
                        onValueChange = { tableLabel = it },
                        label = { Text("Masa Etiketi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = tableNum.toIntOrNull() ?: (tables.size + 1)
                        NexoRepository.addTable(tableLabel, num)
                        showAddTableDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoIndigoPrimary)
                ) {
                    Text("Oluştur")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTableDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Print Sheet Dialog
    if (showPrintSheetDialog) {
        AlertDialog(
            onDismissRequest = { showPrintSheetDialog = false },
            title = { Text("Yazdırılabilir Masa Kartları") },
            text = {
                Column {
                    Text("Tüm masalar için yüksek çözünürlüklü A4 / Stand QR şablonu hazırlandı:")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("• İşletme: ${business.name}")
                    Text("• Masa Sayısı: ${tables.size} Adet")
                    Text("• Standart Ebat: 10 x 15 cm Pleksi Kartı")
                    Text("• Güvenlik: Her masaya özgü kriptografik token")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrintSheetDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoEmeraldDark)
                ) {
                    Text("PDF Olarak Dışa Aktar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrintSheetDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }

    // Camera QR Scanner Dialog
    if (showScannerDialog) {
        com.example.ui.components.CameraQrScannerDialog(
            onDismiss = { showScannerDialog = false },
            onQrCodeDetected = { code ->
                showScannerDialog = false
                val tableNum = code.filter { it.isDigit() }.toIntOrNull() ?: 3
                onPreviewCustomerMenu(tableNum)
            }
        )
    }
}
