package com.example.presentation.display

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.executor.OptimizationAction
import com.example.domain.OptimizationRegistry
import com.example.presentation.FluxViewModel
import com.example.presentation.common.FluxCard
import com.example.presentation.common.FluxPill
import com.example.presentation.common.SectionHeader
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@Composable
fun DisplayScreen(
    viewModel: FluxViewModel,
    modifier: Modifier = Modifier
) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val shizukuState by viewModel.shizukuState.collectAsState()

    val actions = if (deviceInfo != null) {
        OptimizationRegistry.getAllActions(deviceInfo!!, shizukuState.isAvailable)
    } else emptyList()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            DisplaySummaryBanner(deviceInfo = deviceInfo)
        }

        item {
            SectionHeader(
                title = "Panel Refresh Rate",
                subtitle = "Force or lock display refresh frequency across applications"
            )
            RefreshRateControlCard(
                deviceInfo = deviceInfo,
                isShizukuAvailable = shizukuState.isAvailable,
                actions = actions,
                onExecute = { viewModel.executeAction(it) },
                onRequestShizuku = { viewModel.requestShizukuPermission() }
            )
        }

        item {
            SectionHeader(
                title = "System Animation Scales",
                subtitle = "Adjust window transition delays for immediate UI response"
            )
            AnimationScaleControlCard(
                actions = actions,
                isShizukuAvailable = shizukuState.isAvailable,
                onExecute = { viewModel.executeAction(it) },
                onRequestShizuku = { viewModel.requestShizukuPermission() }
            )
        }

        if (deviceInfo?.isHyperOs == true) {
            item {
                SectionHeader(
                    title = "HyperOS Display & Touch Pacing",
                    subtitle = "Xiaomi touch digitizer sampling acceleration"
                )
                val hyperAction = actions.firstOrNull { it.id == "hyperos_touch_game" }
                if (hyperAction != null) {
                    FluxCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(hyperAction.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(hyperAction.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Button(
                                onClick = { viewModel.executeAction(hyperAction) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Apply", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DisplaySummaryBanner(deviceInfo: com.example.data.device.DeviceInfo?) {
    FluxCard(borderColor = CyanPrimary.copy(alpha = 0.35f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("DISPLAY HARDWARE", style = MaterialTheme.typography.labelSmall, color = CyanPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${deviceInfo?.displayResolution ?: "1080x2400"} • ${deviceInfo?.displayDpi ?: 440} DPI",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Current Active Refresh Rate: ${deviceInfo?.currentRefreshRate?.toInt() ?: 60} Hz",
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldSuccess,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(imageVector = Icons.Default.Tv, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun RefreshRateControlCard(
    deviceInfo: com.example.data.device.DeviceInfo?,
    isShizukuAvailable: Boolean,
    actions: List<OptimizationAction>,
    onExecute: (OptimizationAction) -> Unit,
    onRequestShizuku: () -> Unit
) {
    val supported = deviceInfo?.supportedRefreshRates ?: listOf(60f)

    FluxCard {
        Column {
            Text("HARDWARE PANEL MODES", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Detected panel refresh modes: ${supported.joinToString { "${it.toInt()}Hz" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                supported.forEach { hz ->
                    val isCurrent = (deviceInfo?.currentRefreshRate?.toInt() == hz.toInt())
                    val action = when (hz.toInt()) {
                        120 -> actions.firstOrNull { it.id == "force_120hz" }
                        90 -> actions.firstOrNull { it.id == "force_90hz" }
                        60 -> actions.firstOrNull { it.id == "revert_60hz" }
                        else -> null
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCurrent) EmeraldSuccess.copy(alpha = 0.2f) else DarkSurfaceElevated,
                        border = BorderStroke(1.dp, if (isCurrent) EmeraldSuccess else DarkBorder),
                        modifier = Modifier.weight(1f).clickable {
                            if (action != null) {
                                if (isShizukuAvailable) onExecute(action) else onRequestShizuku()
                            }
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${hz.toInt()} Hz",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isCurrent) EmeraldSuccess else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isCurrent) "ACTIVE" else if (action != null) "Lock" else "Default",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCurrent) EmeraldSuccess else TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DarkSurfaceHighlight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hardware limitation: Refresh rates cannot exceed the physical capabilities of your panel.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimationScaleControlCard(
    actions: List<OptimizationAction>,
    isShizukuAvailable: Boolean,
    onExecute: (OptimizationAction) -> Unit,
    onRequestShizuku: () -> Unit
) {
    val fastAction = actions.firstOrNull { it.id == "anim_scale_fast" }
    val offAction = actions.firstOrNull { it.id == "anim_scale_off" }
    val defaultAction = actions.firstOrNull { it.id == "anim_scale_default" }

    FluxCard {
        Column {
            Text("WINDOW & TRANSITION SCALES", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Controls duration multiplier of system window animations and activity open/close transitions.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimationPresetTile(
                    title = "0.5x Scale",
                    subtitle = "Recommended",
                    accent = CyanPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (fastAction != null) {
                            if (isShizukuAvailable) onExecute(fastAction) else onRequestShizuku()
                        }
                    }
                )

                AnimationPresetTile(
                    title = "0x (Instant)",
                    subtitle = "No Animations",
                    accent = AmberWarning,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (offAction != null) {
                            if (isShizukuAvailable) onExecute(offAction) else onRequestShizuku()
                        }
                    }
                )

                AnimationPresetTile(
                    title = "1.0x (Stock)",
                    subtitle = "Default Android",
                    accent = TextMuted,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (defaultAction != null) {
                            if (isShizukuAvailable) onExecute(defaultAction) else onRequestShizuku()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AnimationPresetTile(
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = DarkSurfaceElevated,
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = accent, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}
