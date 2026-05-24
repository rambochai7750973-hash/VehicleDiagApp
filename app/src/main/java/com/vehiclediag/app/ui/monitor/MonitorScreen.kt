package com.vehiclediag.app.ui.monitor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vehiclediag.app.data.model.CanMessage
import com.vehiclediag.app.ui.theme.*

@Composable
fun MonitorScreen(
    viewModel: MonitorViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
    ) {
        Text(
            text = "CAN 总线监听",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (uiState.isMonitoring) {
                Button(
                    onClick = { viewModel.stopMonitoring() },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, tint = AccentRed)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("停止", color = TextPrimary)
                }
            } else {
                Button(
                    onClick = { viewModel.startMonitoring() },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("开始监听")
                }
            }

            Button(
                onClick = { viewModel.clearMessages() },
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.Clear, contentDescription = null, tint = TextSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("清除", color = TextSecondary)
            }

            if (uiState.isMonitoring) {
                Text(
                    text = "监听中...",
                    color = AccentGreen,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = AccentRed,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text("时间", color = TextDim, modifier = Modifier.width(70.dp), fontSize = 11.sp)
            Text("方向", color = TextDim, modifier = Modifier.width(35.dp), fontSize = 11.sp)
            Text("CAN ID", color = TextDim, modifier = Modifier.width(70.dp), fontSize = 11.sp)
            Text("数据", color = TextDim, modifier = Modifier.weight(1f), fontSize = 11.sp)
        }

        Divider(color = CardBorder)

        if (uiState.messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (uiState.isMonitoring) "等待CAN消息..." else "点击「开始监听」启动",
                    color = TextDim,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
            ) {
                items(uiState.messages) { message ->
                    MonitorMessageRow(message)
                }
            }
        }

        Text(
            text = "共 ${uiState.messages.size} 条消息",
            color = TextDim,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun MonitorMessageRow(message: CanMessage) {
    val direction = if (message.rx) "←RX" else "→TX"
    val directionColor = if (message.rx) AccentGreen else AccentRed
    val canId = "0x${message.id.uppercase().trim()}"
    val dataStr = message.data.trim()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(message.time.toString(), color = TextDim, modifier = Modifier.width(70.dp), fontSize = 11.sp)
        Text(direction, color = directionColor, modifier = Modifier.width(35.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(canId, color = TextSecondary, modifier = Modifier.width(70.dp), fontSize = 11.sp)
        Text(
            text = dataStr,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            ),
        )
    }

    Divider(color = CardBorder.copy(alpha = 0.3f))
}
