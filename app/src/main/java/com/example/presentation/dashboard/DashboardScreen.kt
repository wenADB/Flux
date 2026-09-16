package com.example.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.shizuku.ShizukuStatus
import com.example.presentation.FluxViewModel
import com.example.presentation.common.FluxCard
import com.example.presentation.common.FluxMetricBox
import com.example.presentation.common.FluxPill
import com.example.presentation.common.SectionHeader
import com.example.presentation.navigation.Screen
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RedCritical
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@Composable
fun DashboardScreen(
    viewModel: FluxViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val shizukuState by viewModel.shizukuState.collectAsState()
    val perf by viewModel.performanceTelemetry.collectAsState()
    val thermal by viewModel.thermalTelemetry.collectAsState()
    val healthScore by viewModel.healthScore.collectAsState()
    val recs by viewModel.recommendations.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Device Telemetry Banner
            DeviceBanner(deviceInfo = deviceInfo, onRescan = { viewModel.scanDevice() })
        }

        item {
            // Status Pills Bar
            StatusPillsRow(
                shizukuState = shizukuState,
                thermalStatus = thermal.thermalStatus,
                batteryTemp = thermal.temperatureCelsius,
                batteryPct = thermal.batteryPct,
                onRequestShizuku = { viewModel.requestShizukuPermission() }
            )
        }

        item {
            // System Health & Optimization Score
            HealthScoreCard(
                healthScore = healthScore,
                onOptimizeClick = { navController.navigate(Screen.Optimize.route) }
            )
        }

        item {
            // Live Real-Time Telemetry Quad Box
            SectionHeader(
                title = "Hardware Telemetry",
                subtitle = "Real-time metrics sampled via Android Choreographer & ActivityManager"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FluxMetricBox(
                    label = "CPU Load",
                    value = "${perf.cpuUsagePct}%",
                    subtitle = "${deviceInfo?.cpuCores ?: 8} Cores Active",
                    accentColor = CyanPrimary,
                    modifier = Modifier.weight(1f)
                )
                FluxMetricBox(
                    label = "RAM Usage",
                    value = "${perf.ramUsagePct}%",
                    unit = "used",
                    subtitle = "${(perf.totalRamBytes - perf.availRamBytes) / (1024 * 1024 * 1024)}GB / ${perf.totalRamBytes / (1024 * 1024 * 1024)}GB",
                    accentColor = VioletAccent,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FluxMetricBox(
                    label = "Display Rate",
                    value = "${deviceInfo?.currentRefreshRate?.toInt() ?: 60}Hz",
                    subtitle = "Panel modes: ${deviceInfo?.supportedRefreshRates?.joinToString("/") { "${it.toInt()}Hz" } ?: "60Hz"}",
                    accentColor = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
                FluxMetricBox(
                    label = "Frame Pacing",
                    value = perf.frameMetrics.fpsDisplay,
                    subtitle = "p90: ${String.format("%.1f", perf.frameMetrics.p90FrameTimeMs)}ms • ${perf.frameMetrics.jankCount} janks",
                    accentColor = if (perf.frameMetrics.jankCount > 5) AmberWarning else CyanPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            // Smart Recommendations summary
            SmartRecommendationsSummaryCard(
                recommendationsCount = recs.count { it.isExecutable },
                totalRecommendations = recs.size,
                hasThermalConstraint = thermal.isThermallyConstrained,
                onViewRecommendations = { navController.navigate(Screen.Optimize.route) }
            )
        }

        item {
            // Quick Navigation Hub
            SectionHeader(title = "Engine Modules", subtitle = "Zero-root system utilities")
            QuickAccessRow(
                onDisplayClick = { navController.navigate(Screen.Display.route) },
                onAppsClick = { navController.navigate(Screen.Apps.route) },
                onArtClick = { navController.navigate(Screen.Art.route) },
                onGameSpaceClick = { navController.navigate(Screen.GameSpace.route) }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DeviceBanner(
    deviceInfo: com.example.data.device.DeviceInfo?,
    onRescan: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = DarkSurfaceElevated,
        border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FLUX ENGINE",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanPrimary,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${deviceInfo?.brand ?: "Android"} ${deviceInfo?.model ?: "Device"}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${deviceInfo?.romFamily?.displayName ?: "Android"} • ${deviceInfo?.romVersion ?: "Stock"} (API ${deviceInfo?.apiLevel ?: 34})",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onRescan) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Rescan",
                        tint = CyanPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(DarkBorder)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SoC: ${deviceInfo?.socModel ?: "Qualcomm"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = "Kernel: ${deviceInfo?.kernelVersion ?: "Linux"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun StatusPillsRow(
    shizukuState: com.example.data.shizuku.ShizukuState,
    thermalStatus: String,
    batteryTemp: Float,
    batteryPct: Int,
    onRequestShizuku: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shizuku Pill
        val shizukuColor = when (shizukuState.status) {
            ShizukuStatus.PERMISSION_GRANTED -> EmeraldSuccess
            ShizukuStatus.PERMISSION_MISSING -> AmberWarning
            else -> RedCritical
        }
        val shizukuLabel = when (shizukuState.status) {
            ShizukuStatus.PERMISSION_GRANTED -> "Shizuku Active"
            ShizukuStatus.PERMISSION_MISSING -> "Shizuku Needs Auth"
            ShizukuStatus.NOT_RUNNING -> "Shizuku Offline"
            else -> "No Shizuku"
        }
        FluxPill(
            text = shizukuLabel,
            color = shizukuColor,
            icon = Icons.Default.Shield,
            onClick = onRequestShizuku
        )

        // Thermal Pill
        val thermalColor = when (thermalStatus) {
            "NORMAL" -> EmeraldSuccess
            "LIGHT" -> CyanPrimary
            "MODERATE" -> AmberWarning
            else -> RedCritical
        }
        FluxPill(
            text = "${String.format("%.1f", batteryTemp)}°C",
            color = thermalColor,
            icon = Icons.Default.Thermostat
        )

        // Battery Pill
        FluxPill(
            text = "$batteryPct%",
            color = if (batteryPct > 20) TextSecondary else RedCritical
        )
    }
}

@Composable
private fun HealthScoreCard(
    healthScore: com.example.domain.engine.SystemHealthScore?,
    onOptimizeClick: () -> Unit
) {
    val score = healthScore?.overallScore ?: 78
    val scoreColor = when {
        score >= 85 -> EmeraldSuccess
        score >= 65 -> CyanPrimary
        score >= 50 -> AmberWarning
        else -> RedCritical
    }

    FluxCard(borderColor = scoreColor.copy(alpha = 0.35f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SYSTEM HEALTH & OPTIMIZATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.headlineLarge,
                        color = scoreColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "/100",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = healthScore?.formulaExplanation ?: "Calculated from real thermal, RAM, and panel telemetry",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onOptimizeClick,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tune", color = Color.Black, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SmartRecommendationsSummaryCard(
    recommendationsCount: Int,
    totalRecommendations: Int,
    hasThermalConstraint: Boolean,
    onViewRecommendations: () -> Unit
) {
    FluxCard(
        borderColor = if (hasThermalConstraint) AmberWarning.copy(alpha = 0.5f) else DarkBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onViewRecommendations() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (hasThermalConstraint) Icons.Default.Warning else Icons.Default.Speed,
                        contentDescription = null,
                        tint = if (hasThermalConstraint) AmberWarning else CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasThermalConstraint) "Thermal Safety Warning" else "Smart Optimization Recommendations",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (hasThermalConstraint) "Performance optimizations paused while device cools down."
                    else "$recommendationsCount safe, reversible optimizations discovered for your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View",
                tint = CyanPrimary
            )
        }
    }
}

@Composable
private fun QuickAccessRow(
    onDisplayClick: () -> Unit,
    onAppsClick: () -> Unit,
    onArtClick: () -> Unit,
    onGameSpaceClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickAccessTile(title = "Display", subtitle = "120Hz & UI", onClick = onDisplayClick, modifier = Modifier.weight(1f))
        QuickAccessTile(title = "Apps", subtitle = "Memory & Freeze", onClick = onAppsClick, modifier = Modifier.weight(1f))
        QuickAccessTile(title = "ART", subtitle = "Dex2oat Opt", onClick = onArtClick, modifier = Modifier.weight(1f))
        QuickAccessTile(title = "Game Space", subtitle = "Gaming Profiles", onClick = onGameSpaceClick, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun QuickAccessTile(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = DarkSurfaceElevated,
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = CyanPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted, maxLines = 1)
        }
    }
}
