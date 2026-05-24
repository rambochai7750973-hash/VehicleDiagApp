package com.vehiclediag.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vehiclediag.app.data.model.*
import com.vehiclediag.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
    ) {
        Text(
            text = "仪表盘",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        DeviceStatusBar(uiState)

        Spacer(modifier = Modifier.height(12.dp))

        ProtocolSelector(
            protocolList = uiState.protocolList,
            selectedIndex = uiState.selectedProtocolIndex,
            onSelect = { viewModel.selectProtocol(it) },
            error = uiState.protocolError,
        )

        Spacer(modifier = Modifier.height(8.dp))

        PingButton(
            result = uiState.pingResult,
            onClick = { viewModel.pingBus() },
            error = uiState.pingError,
        )

        Spacer(modifier = Modifier.height(8.dp))

        PollingControls(
            isPolling = uiState.isPidPolling,
            onStart = { viewModel.startPidPolling() },
            onStop = { viewModel.stopPidPolling() },
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = AccentRed,
                trackColor = DarkCard,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3D0000)),
            ) {
                Text(
                    text = error,
                    color = AccentRed,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        PidDataGrid(
            pidDataList = uiState.livePidData,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DeviceStatusBar(status: DashboardUiState) {
    val deviceStatus = status.deviceStatus
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatusItem("TX", deviceStatus.tx.toString(), AccentRed)
        StatusItem("RX", deviceStatus.rx.toString(), AccentGreen)
        StatusItem(
            "总线",
            if (deviceStatus.busActive) "活动" else "空闲",
            if (deviceStatus.busActive) AccentGreen else TextDim,
        )
        StatusItem(
            "诊断",
            if (deviceStatus.diagBusy) "忙" else "就绪",
            if (deviceStatus.diagBusy) AccentRed else AccentGreen,
        )
    }
}

@Composable
private fun StatusItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProtocolSelector(
    protocolList: com.vehiclediag.app.data.model.ProtocolList?,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    error: String?,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = protocolList?.names?.getOrElse(selectedIndex) { "未知" } ?: "加载中...",
                onValueChange = {},
                readOnly = true,
                label = { Text("协议", color = TextSecondary) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentRed,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = AccentRed,
                    unfocusedLabelColor = TextSecondary,
                ),
                shape = RoundedCornerShape(8.dp),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                protocolList?.names?.forEachIndexed { index, name ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            onSelect(index)
                            expanded = false
                        },
                    )
                }
            }
        }

        error?.let {
            Text(text = it, color = AccentRed, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
        }
    }
}

@Composable
private fun PingButton(
    result: String,
    onClick: () -> Unit,
    error: String?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("总线检测")
        }

        Spacer(modifier = Modifier.width(12.dp))

        if (result.isNotEmpty()) {
            Text(
                text = result,
                color = if (error != null) AccentRed else AccentGreen,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PollingControls(
    isPolling: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (isPolling) {
            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.Stop, contentDescription = null, tint = AccentRed)
                Spacer(modifier = Modifier.width(4.dp))
                Text("停止轮询", color = TextPrimary)
            }
        } else {
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("开始轮询")
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = if (isPolling) "每1秒刷新..." else "已停止",
            color = if (isPolling) AccentGreen else TextDim,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun PidDataGrid(
    pidDataList: List<PidLiveData>,
    modifier: Modifier = Modifier,
) {
    if (pidDataList.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "点击「开始轮询」查看实时数据",
                color = TextDim,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(pidDataList) { pid ->
            PidCard(pid)
        }
    }
}

@Composable
private fun PidCard(pid: PidLiveData) {
    val hasValue = pid.value != 0f || pid.valid
    val textColor = when {
        pid.valid -> TextPrimary
        hasValue -> TextDim
        else -> TextDim
    }
    val unitColor = when {
        pid.valid -> AccentRed
        hasValue -> TextDim.copy(alpha = 0.5f)
        else -> TextDim
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (pid.valid) CardBorder else CardBorder.copy(alpha = 0.3f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = pid.name,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
            )

            Text(
                text = if (pid.valid) String.format("%.1f", pid.value)
                       else if (hasValue) String.format("%.1f", pid.value)
                       else "---",
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
            )

            Text(
                text = pid.unit,
                color = unitColor,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
