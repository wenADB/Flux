package com.example.presentation.optimize

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.capability.Backend
import com.example.core.capability.RiskLevel
import com.example.core.executor.OptimizationAction
import com.example.domain.OptimizationRegistry
import com.example.domain.engine.SmartRecommendation
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

@Composable
fun OptimizeScreen(
    viewModel: FluxViewModel,
    modifier: Modifier = Modifier
) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val shizukuState by viewModel.shizukuState.collectAsState()
    val recs by viewModel.recommendations.collectAsState()
    val thermal by viewModel.thermalTelemetry.collectAsState()

    val allActions = if (deviceInfo != null) {
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
            OptimizationPipelineVisualizer()
        }

        if (recs.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Smart Recommendations",
                    subtitle = "Facts-based system tuning suggestions for your hardware"
                )
            }
            items(recs) { rec ->
                RecommendationCard(
                    rec = rec,
                    onExecute = { action ->
                        viewModel.executeAction(action)
                    },
                    onRequestShizuku = { viewModel.requestShizukuPermission() }
                )
            }
        }

        item {
            SectionHeader(
                title = "Optimization Action Registry",
                subtitle = "Typed, verified system modifications with automatic backup"
            )
        }

        items(allActions) { action ->
            OptimizationActionCard(
                action = action,
                isShizukuAvailable = shizukuState.isAvailable,
                isThermallyConstrained = thermal.isThermallyConstrained,
                onExecute = { viewModel.executeAction(action) },
                onRequestShizuku = { viewModel.requestShizukuPermission() }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun OptimizationPipelineVisualizer() {
    FluxCard(borderColor = CyanPrimary.copy(alpha = 0.3f)) {
        Text(
            text = "FLUX EXECUTION & SAFETY PIPELINE",
            style = MaterialTheme.typography.labelSmall,
            color = CyanPrimary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Every action is validated, backed up, executed via typed API, and verified against system state with instant rollback support.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PipelineStepItem("1. DISCOVER", true)
            PipelineStepArrow()
            PipelineStepItem("2. SAFETY", true)
            PipelineStepArrow()
            PipelineStepItem("3. BACKUP", true)
            PipelineStepArrow()
            PipelineStepItem("4. EXECUTE", true)
            PipelineStepArrow()
            PipelineStepItem("5. VERIFY", true)
        }
    }
}

@Composable
private fun PipelineStepItem(name: String, active: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (active) CyanPrimary.copy(alpha = 0.15f) else DarkSurfaceHighlight,
        border = BorderStroke(1.dp, if (active) CyanPrimary.copy(alpha = 0.4f) else DarkBorder)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) CyanPrimary else TextMuted,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun PipelineStepArrow() {
    Text(text = "→", color = TextMuted, fontSize = 11.sp)
}

@Composable
fun RecommendationCard(
    rec: SmartRecommendation,
    onExecute: (OptimizationAction) -> Unit,
    onRequestShizuku: () -> Unit
) {
    FluxCard(borderColor = if (rec.isExecutable) CyanPrimary.copy(alpha = 0.4f) else DarkBorder) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rec.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = rec.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                if (rec.action != null && rec.isExecutable) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onExecute(rec.action) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Apply", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else if (rec.id == "rec_shizuku") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onRequestShizuku,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Authorize", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Expected benefit: ${rec.expectedGain}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextCyan
                )
            }
        }
    }
}

@Composable
fun OptimizationActionCard(
    action: OptimizationAction,
    isShizukuAvailable: Boolean,
    isThermallyConstrained: Boolean,
    onExecute: () -> Unit,
    onRequestShizuku: () -> Unit
) {
    val requiresShizuku = action.backend == Backend.SHIZUKU
    val isExecutable = (!requiresShizuku || isShizukuAvailable) && (!isThermallyConstrained || action.risk == RiskLevel.SAFE)

    FluxCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = action.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = action.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = VioletAccent
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Risk badge
                    val riskColor = when (action.risk) {
                        RiskLevel.SAFE -> EmeraldSuccess
                        RiskLevel.LOW -> CyanPrimary
                        RiskLevel.MODERATE -> AmberWarning
                        else -> RedCritical
                    }
                    FluxPill(text = action.risk.label, color = riskColor)

                    // Backend badge
                    val backendColor = when (action.backend) {
                        Backend.NATIVE_API -> EmeraldSuccess
                        Backend.SHIZUKU -> if (isShizukuAvailable) CyanPrimary else AmberWarning
                        else -> TextMuted
                    }
                    FluxPill(text = action.backend.displayName, color = backendColor)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))
            // Technical details box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DarkSurfaceElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "TECHNICAL SPECIFICATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = action.technicalDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Verification: ${action.verificationMethod}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (action.reversible) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reversible", style = MaterialTheme.typography.labelSmall, color = EmeraldSuccess)
                    }
                }

                if (requiresShizuku && !isShizukuAvailable) {
                    Button(
                        onClick = onRequestShizuku,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Needs Shizuku", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else if (isThermallyConstrained && action.risk != RiskLevel.SAFE) {
                    Text("Paused (Thermal Limit)", style = MaterialTheme.typography.labelSmall, color = RedCritical)
                } else {
                    Button(
                        onClick = onExecute,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Execute & Verify", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
