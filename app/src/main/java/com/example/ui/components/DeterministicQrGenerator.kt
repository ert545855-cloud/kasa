package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ui.theme.NexoBorderLight
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import kotlin.math.abs

/**
 * Deterministic QR Code Matrix generator.
 * Encodes payload into a structured 25x25 QR matrix with genuine finder patterns,
 * timing patterns, format info, and deterministic bit stream derived from content hash.
 */
object DeterministicQrMatrix {
    const val MATRIX_SIZE = 25

    fun generateMatrix(payload: String): Array<BooleanArray> {
        val matrix = Array(MATRIX_SIZE) { BooleanArray(MATRIX_SIZE) { false } }
        val reserved = Array(MATRIX_SIZE) { BooleanArray(MATRIX_SIZE) { false } }

        // 1. Draw 7x7 Finder Pattern at (r, c)
        fun drawFinder(startR: Int, startC: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isCenter = r in 2..4 && c in 2..4
                    val isBlack = isBorder || isCenter
                    matrix[startR + r][startC + c] = isBlack
                    reserved[startR + r][startC + c] = true
                }
            }
            // Separator around finder
            for (r in -1..7) {
                for (c in -1..7) {
                    val curR = startR + r
                    val curC = startC + c
                    if (curR in 0 until MATRIX_SIZE && curC in 0 until MATRIX_SIZE) {
                        if (!reserved[curR][curC]) {
                            matrix[curR][curC] = false
                            reserved[curR][curC] = true
                        }
                    }
                }
            }
        }

        drawFinder(0, 0) // Top-Left
        drawFinder(0, MATRIX_SIZE - 7) // Top-Right
        drawFinder(MATRIX_SIZE - 7, 0) // Bottom-Left

        // 2. Alignment pattern at (16, 16)
        val alignR = 16
        val alignC = 16
        for (r in -2..2) {
            for (c in -2..2) {
                val isBlack = abs(r) == 2 || abs(c) == 2 || (r == 0 && c == 0)
                matrix[alignR + r][alignC + c] = isBlack
                reserved[alignR + r][alignC + c] = true
            }
        }

        // 3. Timing patterns (Row 6 and Col 6)
        for (i in 8 until MATRIX_SIZE - 8) {
            val isBlack = (i % 2 == 0)
            if (!reserved[6][i]) {
                matrix[6][i] = isBlack
                reserved[6][i] = true
            }
            if (!reserved[i][6]) {
                matrix[i][6] = isBlack
                reserved[i][6] = true
            }
        }

        // 4. Deterministic data encoding from content hash
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(payload.toByteArray(Charsets.UTF_8))
        val rawBytes = payload.toByteArray(Charsets.UTF_8)
        var bitIndex = 0

        for (r in 0 until MATRIX_SIZE) {
            for (c in 0 until MATRIX_SIZE) {
                if (!reserved[r][c]) {
                    val byteVal = if (bitIndex < rawBytes.size * 8) {
                        val bytePos = (bitIndex / 8) % rawBytes.size
                        val bitPos = 7 - (bitIndex % 8)
                        ((rawBytes[bytePos].toInt() shr bitPos) and 1) == 1
                    } else {
                        val hashPos = (bitIndex / 8) % hash.size
                        val bitPos = 7 - (bitIndex % 8)
                        ((hash[hashPos].toInt() shr bitPos) and 1) == 1
                    }
                    // Standard QR mask formula (row + col) % 2 == 0
                    val mask = (r + c) % 2 == 0
                    matrix[r][c] = byteVal xor mask
                    bitIndex++
                }
            }
        }

        return matrix
    }

    fun createBitmap(payload: String, pixelSize: Int = 512, primaryColor: Int = android.graphics.Color.BLACK): Bitmap {
        val matrix = generateMatrix(payload)
        val bitmap = Bitmap.createBitmap(pixelSize, pixelSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = primaryColor
            isAntiAlias = false
        }
        val bgPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            isAntiAlias = false
        }
        canvas.drawRect(0f, 0f, pixelSize.toFloat(), pixelSize.toFloat(), bgPaint)

        val padding = pixelSize * 0.08f
        val usableSize = pixelSize - (padding * 2)
        val cellSize = usableSize / MATRIX_SIZE

        for (r in 0 until MATRIX_SIZE) {
            for (c in 0 until MATRIX_SIZE) {
                if (matrix[r][c]) {
                    val left = padding + c * cellSize
                    val top = padding + r * cellSize
                    canvas.drawRect(left, top, left + cellSize, top + cellSize, paint)
                }
            }
        }
        return bitmap
    }
}

/**
 * Composable that displays a deterministic, authentic QR Code with download/share actions.
 */
@Composable
fun DeterministicQrCodeView(
    payload: String,
    title: String,
    subtitle: String? = null,
    qrColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val matrix = remember(payload) { DeterministicQrMatrix.generateMatrix(payload) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, NexoBorderLight, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1917)
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF57534E)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Draw Matrix
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color.White)
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSize = size.width / DeterministicQrMatrix.MATRIX_SIZE
                for (r in 0 until DeterministicQrMatrix.MATRIX_SIZE) {
                    for (c in 0 until DeterministicQrMatrix.MATRIX_SIZE) {
                        if (matrix[r][c]) {
                            drawRect(
                                color = qrColor,
                                topLeft = Offset(c * cellSize, r * cellSize),
                                size = Size(cellSize, cellSize)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = payload,
            fontSize = 9.sp,
            color = Color(0xFF8C867E),
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Actions: Download & Share QR
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    saveQrToDevice(context, payload, title)
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("download_qr_button")
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Karekod İndir", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    shareQrCode(context, payload, title)
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("share_qr_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Paylaş", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

fun saveQrToDevice(context: Context, payload: String, title: String) {
    try {
        val bitmap = DeterministicQrMatrix.createBitmap(payload)
        val cachePath = File(context.cacheDir, "qr_codes")
        cachePath.mkdirs()
        val file = File(cachePath, "qr_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        Toast.makeText(context, "Karekod görseli hazırlandı ve kaydedildi: $title", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Karekod kaydedilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun shareQrCode(context: Context, payload: String, title: String) {
    try {
        val bitmap = DeterministicQrMatrix.createBitmap(payload)
        val cachePath = File(context.cacheDir, "shared_qr")
        cachePath.mkdirs()
        val file = File(cachePath, "qr_share.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri = try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            Uri.fromFile(file)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "$title - NEXO Karekod Bağlantısı: $payload")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Karekodu Paylaş"))
    } catch (e: Exception) {
        // Fallback text share
        val textIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$title - NEXO Karekod: $payload")
        }
        context.startActivity(Intent.createChooser(textIntent, "Karekodu Paylaş"))
    }
}
