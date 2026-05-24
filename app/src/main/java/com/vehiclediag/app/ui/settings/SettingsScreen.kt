package com.vehiclediag.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vehiclediag.app.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        ConnectionSection(uiState, viewModel)
        Spacer(modifier = Modifier.height(16.dp))
        LogSection(uiState, viewModel)
    }
}

@Composable
private fun ConnectionSection(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "设备连接",
                style = MaterialTheme.typography.titleLarge,
                color = AccentRed,
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.deviceSsid,
                onValueChange = { viewModel.updateSsid(it) },
                label = { Text("WiFi SSID", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors(),
                shape = RoundedCornerShape(8.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.devicePassword,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text("WiFi 密码", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors(),
                shape = RoundedCornerShape(8.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.deviceIp,
                onValueChange = { viewModel.updateDeviceIp(it) },
                label = { Text("设备 IP", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors(),
                shape = RoundedCornerShape(8.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (uiState.isConnected) {
                    Button(
                        onClick = { viewModel.disconnect() },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("断开", color = TextPrimary)
                    }
                } else {
                    Button(
                        onClick = { viewModel.connect() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("连接")
                    }
                }

                if (uiState.connectionStatus.isNotEmpty()) {
                    Text(
                        text = uiState.connectionStatus,
                        color = if (uiState.isConnected) AccentGreen else TextDim,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun LogSection(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "诊断日志",
                    style = MaterialTheme.typography.titleLarge,
                    color = AccentRed,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { viewModel.loadLog() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = TextSecondary,
                        )
                    }
                    Button(
                        onClick = { viewModel.clearLog() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D0000)),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("清除", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoadingLog) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentRed,
                    trackColor = DarkCard,
                )
            }

            uiState.logError?.let { error ->
                Text(text = error, color = AccentRed, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (uiState.logText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkBackground),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = uiState.logText,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState()),
                    )
                }
            } else {
                Text(
                    text = "日志为空",
                    color = TextDim,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentRed,
    unfocusedBorderColor = CardBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = AccentRed,
    focusedLabelColor = AccentRed,
    unfocusedLabelColor = TextSecondary,
)
