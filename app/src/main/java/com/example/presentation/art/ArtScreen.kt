package com.example.presentation.art

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@Composable
fun ArtScreen(
    viewModel: FluxViewModel,
    modifier: Modifier = Modifier
) {
    val shizukuState by viewModel.shizukuState.collectAsState()
    val games by viewModel.gamesList.collectAsState()
    val packages by viewModel.installedPackages.collectAsState()
    val artResult by viewModel.artResult.collectAsState()

    var selectedMode by remember { mutableStateOf("speed-profile") }
    var selectedPackage by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            ArtHeaderBanner()
        }

        item {
            // Mode selector card
            ArtModeSelectorCard(
                selectedMode = selectedMode,
                onSelectMode = { selectedMode = it }
            )
        }

        if (artResult != null) {
            item {
                ArtResultCard(result = artResult!!)
            }
        }

        item {
            SectionHeader(
                title = "Select Application to Compile",
                subtitle = "Pre-compile frequently used games or heavy apps to eliminate runtime JIT stutters"
            )
        }

        // Show games first or non-system user apps
        val candidateApps = (games.map { it.packageName to it.appName } +
                packages.filter { !it.isSystem }.map { it.packageName to it.appLabel }).distinctBy { it.first }

        items(candidateApps.take(20)) { (pkg, label) ->
            FluxCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(pkg, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontFamily = FontFamily.Monospace)
                    }

                    if (!shizukuState.isAvailable) {
                        Button(
                            onClick = { viewModel.requestShizukuPermission() },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Shizuku Req", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.resetAppCompilation(pkg) },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, DarkBorder)
                            ) {
                                Text("Reset", color = TextMuted, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { viewModel.compileApp(pkg, selectedMode) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Compile", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
private fun ArtHeaderBanner() {
    FluxCard(borderColor = VioletAccent.copy(alpha = 0.35f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("ANDROID RUNTIME OPTIMIZATION", style = MaterialTheme.typography.labelSmall, color = VioletAccent)
                Spacer(modifier = Modifier.height(2.dp))
                Text("dex2oat AOT Compiler", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Compiles application bytecode directly into native ELF machine binaries. Decreases cold start time and avoids CPU JIT spikes during gameplay.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(imageVector = Icons.Default.DeveloperBoard, contentDescription = null, tint = VioletAccent, modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun ArtModeSelectorCard(
    selectedMode: String,
    onSelectMode: (String) -> Unit
) {
    FluxCard {
        Column {
            Text("COMPILATION FILTER PROFILE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeButton(
                    label = "speed-profile",
                    desc = "Hot methods (Recommended)",
                    isSelected = selectedMode == "speed-profile",
                    onClick = { onSelectMode("speed-profile") },
                    modifier = Modifier.weight(1f)
                )
                ModeButton(
                    label = "speed",
                    desc = "Full AOT Compilation",
                    isSelected = selectedMode == "speed",
                    onClick = { onSelectMode("speed") },
                    modifier = Modifier.weight(1f)
                )
                ModeButton(
                    label = "verify",
                    desc = "Verify bytecode only",
                    isSelected = selectedMode == "verify",
                    onClick = { onSelectMode("verify") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DarkSurfaceHighlight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Note: AOT compilation requires several seconds of CPU time and modestly increases app storage size.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeButton(
    label: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) CyanPrimary.copy(alpha = 0.15f) else DarkSurfaceElevated,
        border = BorderStroke(1.dp, if (isSelected) CyanPrimary else DarkBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = if (isSelected) CyanPrimary else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun ArtResultCard(result: com.example.data.art.ArtCompilationResult) {
    val borderColor = if (result.success) EmeraldSuccess else RedCritical
    FluxCard(borderColor = borderColor.copy(alpha = 0.5f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (result.success) "COMPILATION FINISHED" else "COMPILATION STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = borderColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(result.packageName, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(result.outputMessage, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Icon(
                imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = borderColor,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
