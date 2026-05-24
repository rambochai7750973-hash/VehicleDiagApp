package com.vehiclediag.app.ui.diagnostic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vehiclediag.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticScreen(
    viewModel: DiagnosticViewModel = viewModel(),
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
            text = "诊断",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        DtcSection(uiState, viewModel)
        Spacer(modifier = Modifier.height(16.dp))
        UdsSection(uiState, viewModel)
        Spacer(modifier = Modifier.height(16.dp))
        CustomDiagSection(uiState, viewModel)
    }
}

@Composable
private fun DtcSection(uiState: DiagnosticUiState, viewModel: DiagnosticViewModel) {
    SectionCard(title = "故障码管理 (DTC)") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { viewModel.readStoredDtc() },
                enabled = !uiState.isReadingStoredDtc,
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                if (uiState.isReadingStoredDtc) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("存储故障码", fontSize = 13.sp)
                }
            }

            Button(
                onClick = { viewModel.readPendingDtc() },
                enabled = !uiState.isReadingPendingDtc,
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                if (uiState.isReadingPendingDtc) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("待定故障码", fontSize = 13.sp)
                }
            }

            Button(
                onClick = { viewModel.clearDtc() },
                enabled = !uiState.isClearingDtc,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D0000)),
                shape = RoundedCornerShape(8.dp),
            ) {
                if (uiState.isClearingDtc) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("清除", fontSize = 13.sp)
                }
            }
        }

        if (uiState.dtcClearSuccess) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF003D00)),
            ) {
                Text(
                    text = "故障码清除成功",
                    color = AccentGreen,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        uiState.dtcError?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, color = AccentRed, fontSize = 12.sp)
        }

        val allDtc = uiState.storedDtc + uiState.pendingDtc
        if (allDtc.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = CardBorder)

            allDtc.forEach { dtc ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = dtc.code,
                        color = AccentRed,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(80.dp),
                    )
                    Text(
                        text = dtc.desc,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun UdsSection(uiState: DiagnosticUiState, viewModel: DiagnosticViewModel) {
    SectionCard(title = "UDS 诊断服务") {
        // Session control
        Text("会话控制", color = AccentRed, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { viewModel.udsSessionControl(1) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = ButtonDefaults.outlinedButtonBorder,
            ) {
                Text("默认会话 (0x01)")
            }
            OutlinedButton(
                onClick = { viewModel.udsSessionControl(3) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = ButtonDefaults.outlinedButtonBorder,
            ) {
                Text("扩展会话 (0x03)")
            }
        }

        if (uiState.udsSessionResult.isNotEmpty()) {
            Text(uiState.udsSessionResult, color = AccentGreen, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ECU Reset
        Text("ECU 复位", color = AccentRed, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { viewModel.udsReset(1) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = ButtonDefaults.outlinedButtonBorder,
            ) {
                Text("硬复位 (0x01)")
            }
            OutlinedButton(
                onClick = { viewModel.udsReset(2) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = ButtonDefaults.outlinedButtonBorder,
            ) {
                Text("软复位 (0x02)")
            }
        }

        if (uiState.udsResetResult.isNotEmpty()) {
            Text(uiState.udsResetResult, color = AccentGreen, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Read VIN
        Text("读取 VIN", color = AccentRed, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { viewModel.readVin() },
                enabled = !uiState.isReadingVin,
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                shape = RoundedCornerShape(8.dp),
            ) {
                if (uiState.isReadingVin) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("读取 VIN")
                }
            }

            if (uiState.udsVinResult.isNotEmpty()) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(uiState.udsVinResult, color = AccentGreen, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Read DID
        Text("读取 DID (0xF190)", color = AccentRed, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { viewModel.readDid(0xF190) },
                enabled = !uiState.isReadingDid,
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                shape = RoundedCornerShape(8.dp),
            ) {
                if (uiState.isReadingDid) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("读取 DID")
                }
            }

            if (uiState.udsDidResult.isNotEmpty()) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(uiState.udsDidResult, color = AccentGreen, fontSize = 12.sp)
            }
        }

        uiState.udsError?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, color = AccentRed, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CustomDiagSection(uiState: DiagnosticUiState, viewModel: DiagnosticViewModel) {
    SectionCard(title = "自定义诊断请求") {
        var hexInput by remember { mutableStateOf("") }

        OutlinedTextField(
            value = hexInput,
            onValueChange = { hexInput = it },
            label = { Text("十六进制数据 (如 02 3E 80)", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentRed,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = AccentRed,
            ),
            shape = RoundedCornerShape(8.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.sendCustomDiag(hexInput) },
            enabled = !uiState.isSendingCustom && hexInput.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
            shape = RoundedCornerShape(8.dp),
        ) {
            if (uiState.isSendingCustom) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = TextPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("发送")
            }
        }

        uiState.customDiagError?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, color = AccentRed, fontSize = 12.sp)
        }

        uiState.customDiagResponse?.let { response ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("响应:", color = AccentGreen, fontWeight = FontWeight.Bold)
                    response.id?.let {
                        Text("CAN ID: 0x${it.toString(16).uppercase()}", color = TextSecondary)
                    }
                    response.data?.let { data ->
                        val hexStr = data.joinToString(" ") { byte ->
                            byte.toString(16).uppercase().padStart(2, '0')
                        }
                        Text("数据: $hexStr", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = AccentRed,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
