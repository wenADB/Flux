package com.example.presentation.performance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.FluxViewModel
import com.example.presentation.common.FluxCard
import com.example.presentation.common.FluxMetricBox
import com.example.presentation.common.FluxPill
import com.example.presentation.common.SectionHeader
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RedCritical
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@Composable
fun PerformanceScreen(
    viewModel: FluxViewModel,
    modifier: Modifier = Modifier
) {
    val perf by viewModel.performanceTelemetry.collectAsState()
    val thermal by viewModel.thermalTelemetry.collectAsState()
    val network by viewModel.networkTelemetry.collectAsState()
    val deviceInfo by viewModel.deviceInfo.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Thermal Health Card
            ThermalMonitorCard(thermal = thermal)
        }

        item {
            // Live Frame Pacing & Choreographer Analysis
            SectionHeader(
                title = "Frame Pacing & VSync Telemetry",
                subtitle = "Active Choreographer frame intervals (no simulated FPS)"
            )
            FrameTimingCard(frameMetrics = perf.frameMetrics)
        }

        item {
            // CPU & SoC Architecture
            SectionHeader(
                title = "Processor & Memory Telemetry",
                subtitle = "Qualcomm Snapdragon & Android memory subsystems"
            )
            CpuAndMemoryCard(
                perf = perf,
                deviceInfo = deviceInfo
            )
        }

        item {
            // Network & Latency Diagnostics
            SectionHeader(
                title = "Network Quality Diagnostics",
                subtitle = "Live TCP socket latency to Google DNS (8.8.8.8)"
            )
            NetworkDiagnosticsCard(
                network = network,
                onRefresh = { viewModel.refreshNetwork() }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ThermalMonitorCard(
    thermal: com.example.data.thermal.ThermalTelemetry
) {
    val statusColor = when (thermal.thermalStatus) {
        "NORMAL" -> EmeraldSuccess
        "LIGHT" -> CyanPrimary
        "MODERATE" -> AmberWarning
        else -> RedCritical
    }

    FluxCard(borderColor = statusColor.copy(alpha = 0.4f)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Thermostat, contentDescription = null, tint = statusColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("THERMAL ENGINE TELEMETRY", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = "Status: ${thermal.thermalStatus}",
                            style = MaterialTheme.typography.titleMedium,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "${String.format("%.1f", thermal.temperatureCelsius)}°C",
                    style = MaterialTheme.typography.headlineMedium,
                    color = statusColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            // Temperature status bar
            val progress = (thermal.temperatureCelsius / 50.0f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = statusColor,
                trackColor = DarkSurfaceHighlight
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = thermal.statusDescription,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Battery: ${thermal.batteryPct}% (${if (thermal.isCharging) "Charging" else "Discharging"})", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text("Voltage: ${thermal.voltageMv} mV", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
}

@Composable
private fun FrameTimingCard(
    frameMetrics: com.example.data.performance.FrameMetrics
) {
    FluxCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ACTIVE RENDER PACE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        text = frameMetrics.fpsDisplay,
                        style = MaterialTheme.typography.titleLarge,
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                FluxPill(
                    text = "${frameMetrics.jankCount} Janks",
                    color = if (frameMetrics.jankCount > 5) AmberWarning else EmeraldSuccess
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PercentileBox(label = "Avg Frame", value = "${String.format("%.1f", frameMetrics.averageFrameTimeMs)} ms", modifier = Modifier.weight(1f))
                PercentileBox(label = "p50 (Median)", value = "${String.format("%.1f", frameMetrics.p50FrameTimeMs)} ms", modifier = Modifier.weight(1f))
                PercentileBox(label = "p90", value = "${String.format("%.1f", frameMetrics.p90FrameTimeMs)} ms", modifier = Modifier.weight(1f))
                PercentileBox(label = "p99 (Spike)", value = "${String.format("%.1f", frameMetrics.p99FrameTimeMs)} ms", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Measured via Android Choreographer VSync pipeline. A 16.6ms frame represents 60Hz pacing; 8.3ms represents 120Hz pacing.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun PercentileBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = DarkSurfaceElevated,
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun CpuAndMemoryCard(
    perf: com.example.data.performance.PerformanceTelemetry,
    deviceInfo: com.example.data.device.DeviceInfo?
) {
    FluxCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("PLATFORM & PROCESSOR", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        text = deviceInfo?.socModel ?: "Qualcomm Snapdragon",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                FluxPill(text = "${deviceInfo?.cpuCores ?: 8} Cores", color = CyanPrimary)
            }

            Spacer(modifier = Modifier.height(12.dp))
            // RAM progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("RAM Usage (${perf.ramUsagePct}%)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(
                    text = "${(perf.usedRamBytes) / (1024 * 1024 * 1024)}GB used / ${(perf.totalRamBytes) / (1024 * 1024 * 1024)}GB total",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextCyan,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (perf.ramUsagePct / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = if (perf.ramUsagePct > 80) AmberWarning else VioletAccent,
                trackColor = DarkSurfaceHighlight
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "GPU: ${deviceInfo?.gpuRenderer ?: "Adreno"} • Low RAM Device: ${deviceInfo?.isLowRamDevice ?: false}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun NetworkDiagnosticsCard(
    network: com.example.data.network.NetworkTelemetry?,
    onRefresh: () -> Unit
) {
    FluxCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("NETWORK QUALITY", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text(
                    text = network?.connectionType ?: "Checking...",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = network?.signalDescription ?: "Diagnosing link latency...",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (network != null && network.dnsLatencyMs > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "TCP Latency to 8.8.8.8:53: ${network.dnsLatencyMs} ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldSuccess,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            IconButton(onClick = onRefresh) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = CyanPrimary)
            }
        }
    }
}
