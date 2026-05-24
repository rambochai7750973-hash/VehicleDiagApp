package com.vehiclediag.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.vehiclediag.app.ui.dashboard.DashboardScreen
import com.vehiclediag.app.ui.diagnostic.DiagnosticScreen
import com.vehiclediag.app.ui.monitor.MonitorScreen
import com.vehiclediag.app.ui.settings.SettingsScreen
import com.vehiclediag.app.ui.theme.*

private data class NavTab(
    val title: String,
    val icon: ImageVector,
    val route: String,
)

private val navTabs = listOf(
    NavTab("仪表盘", Icons.Default.Dashboard, "dashboard"),
    NavTab("诊断", Icons.Default.MiscellaneousServices, "diagnostic"),
    NavTab("监听", Icons.Default.MonitorHeart, "monitor"),
    NavTab("设置", Icons.Default.Settings, "settings"),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VehicleDiagTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextPrimary,
            ) {
                navTabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentRed,
                            selectedTextColor = AccentRed,
                            unselectedIconColor = TextDim,
                            unselectedTextColor = TextDim,
                            indicatorColor = DarkCard,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = DarkBackground,
        ) {
            when (selectedTab) {
                0 -> DashboardScreen()
                1 -> DiagnosticScreen()
                2 -> MonitorScreen()
                3 -> SettingsScreen()
            }
        }
    }
}
