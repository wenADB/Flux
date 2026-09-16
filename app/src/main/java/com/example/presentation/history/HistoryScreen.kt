package com.example.presentation.history

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.database.BackupEntity
import com.example.data.database.OptimizationHistoryEntity
import com.example.presentation.FluxViewModel
import com.example.presentation.common.FluxCard
import com.example.presentation.common.FluxPill
import com.example.presentation.common.SectionHeader
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RedCritical
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: FluxViewModel,
    modifier: Modifier = Modifier
) {
    val history by viewModel.historyEntries.collectAsState()
    val backups by viewModel.activeBackups.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            HistoryBanner(activeBackupsCount = backups.size)
        }

        if (backups.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Active State Backups",
                    subtitle = "One-click restore of system settings to original pre-optimization values"
                )
            }
            items(backups) { backup ->
                BackupCard(
                    backup = backup,
                    onRestore = { viewModel.restoreBackup(backup.id) }
                )
            }
        }

        item {
            SectionHeader(
                title = "Execution Audit Log",
                subtitle = "Complete verifiable ledger of all applied tweaks and system checks"
            )
        }

        if (history.isEmpty()) {
            item {
                FluxCard {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Optimization History Yet", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("When you execute actions from the Optimize or Display tab, every verification step will be logged here.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        } else {
            items(history) { entry ->
                HistoryEntryCard(entry = entry)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HistoryBanner(activeBackupsCount: Int) {
    FluxCard(borderColor = CyanPrimary.copy(alpha = 0.35f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("INTEGRITY & ROLLBACK GUARANTEE", style = MaterialTheme.typography.labelSmall, color = CyanPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Zero-Risk Architecture", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "$activeBackupsCount active setting restore points stored in SQLite database.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun BackupCard(
    backup: BackupEntity,
    onRestore: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(backup.timestamp))

    FluxCard(borderColor = EmeraldSuccess.copy(alpha = 0.4f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(backup.actionId, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Original Value: ${backup.originalValue}", style = MaterialTheme.typography.bodySmall, color = EmeraldSuccess, fontFamily = FontFamily.Monospace)
                Text("Backed up at $dateStr", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
            Button(
                onClick = onRestore,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Undo, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Rollback", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun HistoryEntryCard(entry: OptimizationHistoryEntity) {
    val dateStr = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault()).format(Date(entry.timestamp))
    val statusColor = when {
        entry.isVerified -> EmeraldSuccess
        entry.success -> CyanPrimary
        else -> RedCritical
    }

    FluxCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.actionTitle, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Applied: ${entry.appliedValue} (was: ${entry.previousValue ?: "unset"})",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(entry.details, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text(dateStr, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
            }

            FluxPill(
                text = if (entry.isVerified) "Verified" else if (entry.success) "Applied" else "Failed",
                color = statusColor,
                icon = if (entry.isVerified) Icons.Default.CheckCircle else Icons.Default.Error
            )
        }
    }
}
