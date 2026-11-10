package com.example.bluey.ui.companion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.bluey.model.Palette
import com.example.bluey.ui.theme.Fredoka
import com.example.bluey.ui.theme.IBMPlexMono
import com.example.bluey.ui.theme.IBMPlexSans

@Composable
fun PairingDialog(
    connected: Boolean,
    currentMacName: String?,
    onPairSuccess: (macName: String) -> Unit,
    onDisconnect: () -> Unit,
    onDismiss: () -> Unit
) {
    var manualHost by remember { mutableStateOf("MacBook Pro") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            color = Palette.Panel
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pair Mac",
                        fontFamily = Fredoka,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_pairing_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // QR / Scanner animation frame
                Card(
                    modifier = Modifier
                        .size(220.dp)
                        .border(2.dp, Palette.Berry2, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(160.dp)) {
                            // Stylized QR matrix representation
                            val step = size.width / 8f
                            for (r in 0 until 8) {
                                for (c in 0 until 8) {
                                    val isCorner = (r < 3 && c < 3) || (r < 3 && c > 4) || (r > 4 && c < 3)
                                    val draw = isCorner || ((r * 7 + c * 13) % 5 == 0)
                                    if (draw) {
                                        drawRect(
                                            color = if (isCorner) Palette.Berry1 else Color.White.copy(alpha = 0.8f),
                                            topLeft = Offset(c * step + 2f, r * step + 2f),
                                            size = androidx.compose.ui.geometry.Size(step - 4f, step - 4f)
                                        )
                                    }
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan code",
                            tint = Palette.Berry2.copy(alpha = 0.6f),
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (connected) "Connected to ${currentMacName ?: "Mac"}" else "Scan the QR code in Bluey's Mac menu",
                        fontFamily = Fredoka,
                        fontSize = 18.sp,
                        color = if (connected) Palette.TaskGreen else Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Make sure your iPhone and Mac are on the same local network.",
                        fontFamily = IBMPlexSans,
                        fontSize = 13.sp,
                        color = Palette.InkSoft
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!connected) {
                        OutlinedTextField(
                            value = manualHost,
                            onValueChange = { manualHost = it },
                            label = { Text("Companion Device Name", color = Palette.InkSoft) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Palette.Berry2,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("companion_name_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                onPairSuccess(manualHost.ifBlank { "MacBook Pro" })
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Palette.Berry2),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("confirm_pair_button")
                        ) {
                            Icon(Icons.Default.Laptop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pair with $manualHost", fontFamily = IBMPlexSans)
                        }
                    } else {
                        Button(
                            onClick = {
                                onDisconnect()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5547A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("disconnect_companion_button")
                        ) {
                            Text("Disconnect", fontFamily = IBMPlexSans)
                        }
                    }
                }
            }
        }
    }
}
