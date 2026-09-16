package com.example.presentation.about

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.FluxViewModel
import com.example.presentation.common.FluxCard
import com.example.presentation.common.SectionHeader
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@Composable
fun AboutScreen(
    viewModel: FluxViewModel,
    modifier: Modifier = Modifier
) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            FluxHeroBanner()
        }

        item {
            SectionHeader(title = "Credits & Collaboration", subtitle = "Open engineering principles")
            CreditsCard()
        }

        item {
            SectionHeader(title = "Strict Non-Affiliation Disclaimer", subtitle = "Legal & Trademark Disclosures")
            DisclaimerCard()
        }

        item {
            SectionHeader(title = "Hardware Telemetry Summary", subtitle = "Target profile parameters")
            HardwareSummaryCard(deviceInfo = deviceInfo)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FluxHeroBanner() {
    FluxCard(borderColor = CyanPrimary.copy(alpha = 0.4f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(54.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("FLUX ENGINE", style = MaterialTheme.typography.headlineMedium, color = CyanPrimary, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("Version 2.0-PRO • Build 2026.1", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Rootless Android Performance & Optimization Engine with real-time telemetry, Shizuku privileged execution, thermal guardrails, and game profiles.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
private fun CreditsCard() {
    FluxCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = VioletAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Author & Architectural Credits", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("• Created by wenADB (GitHub: https://github.com/wenADB)", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("• Architecture designed in collaborative engineering between Gemini & ChatGPT", style = MaterialTheme.typography.bodyMedium, color = CyanPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("• Privileged Shizuku IPC protocol via Rikka Apps (moe.shizuku.privilege.api)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

@Composable
private fun DisclaimerCard() {
    FluxCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Legal Disclaimer", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "FLUX is an independent utility and is NOT affiliated with, endorsed by, or sponsored by Xiaomi, POCO, Qualcomm, Google, or any original equipment manufacturer (OEM).",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "All brand names, product names, trademarks, and registered trademarks (including Snapdragon, HyperOS, MIUI, and Android) are the property of their respective owners. FLUX operates within standard user and privileged APIs with zero-risk architectural guardrails.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun HardwareSummaryCard(deviceInfo: com.example.data.device.DeviceInfo?) {
    FluxCard {
        Column {
            Text("DEVICE SPECIFICATION", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Model:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("${deviceInfo?.brand ?: "Android"} ${deviceInfo?.model ?: "Device"} (${deviceInfo?.codename ?: "unknown"})", style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Platform:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("${deviceInfo?.socModel ?: "Qualcomm"} (${deviceInfo?.cpuCores ?: 8} cores)", style = MaterialTheme.typography.bodySmall, color = TextCyan)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ROM:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("${deviceInfo?.romFamily?.displayName ?: "AOSP"} ${deviceInfo?.romVersion ?: ""} (API ${deviceInfo?.apiLevel ?: 34})", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Display:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("${deviceInfo?.displayResolution ?: "1080x2400"} @ ${deviceInfo?.currentRefreshRate?.toInt() ?: 60}Hz", style = MaterialTheme.typography.bodySmall, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
            }
        }
    }
}
