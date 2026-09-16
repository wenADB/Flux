package com.example.presentation.settings

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.logging.LogLevel
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
import com.example.ui.theme.RedCritical
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: FluxViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.logs.collectAsState()
    var requireConfirm by remember { mutableStateOf(true) }
    var autoRescan by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            SectionHeader(title = "Safety & Guardrail Preferences", subtitle = "Fail-safe execution settings")
            SafetyPreferencesCard(
                requireConfirm = requireConfirm,
                onToggleConfirm = { requireConfirm = it },
                autoRescan = autoRescan,
                onToggleAutoRescan = { autoRescan = it }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = "In-Memory Diagnostic Logs", subtitle = "Sanitized application runtime logs")
                IconButton(onClick = { viewModel.clearDebugLogs() }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Logs", tint = RedCritical)
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                FluxCard {
                    Text("No diagnostic entries recorded in current session.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }
        } else {
            items(logs.reversed().take(40)) { entry ->
                LogEntryRow(entry = entry)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SafetyPreferencesCard(
    requireConfirm: Boolean,
    onToggleConfirm: (Boolean) -> Unit,
    autoRescan: Boolean,
    onToggleAutoRescan: (Boolean) -> Unit
) {
    FluxCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Execution Safety Gate", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("Block high-risk actions when thermal limit > 42°C is detected", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Switch(
                    checked = requireConfirm,
                    onCheckedChange = onToggleConfirm,
                    colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary, checkedTrackColor = CyanPrimary.copy(alpha = 0.3f))
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-Rescan on Resume", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("Automatically rescan thermal and refresh telemetry when returning to app", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Switch(
                    checked = autoRescan,
                    onCheckedChange = onToggleAutoRescan,
                    colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary, checkedTrackColor = CyanPrimary.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
private fun LogEntryRow(entry: com.example.core.logging.LogEntry) {
    val timeStr = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(entry.timestamp))
    val levelColor = when (entry.level) {
        LogLevel.DEBUG -> TextMuted
        LogLevel.INFO -> CyanPrimary
        LogLevel.WARN -> AmberWarning
        LogLevel.ERROR -> RedCritical
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = DarkSurfaceHighlight,
        border = BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "[${entry.level.name}]",
                        style = MaterialTheme.typography.labelSmall,
                        color = levelColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = entry.tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(timeStr, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = entry.message,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }
    }
}
