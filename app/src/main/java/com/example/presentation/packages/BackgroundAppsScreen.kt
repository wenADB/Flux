package com.example.presentation.packages

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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import com.example.core.safety.PackageCategory
import com.example.data.packages.PackageItem
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
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@Composable
fun BackgroundAppsScreen(
    viewModel: FluxViewModel,
    modifier: Modifier = Modifier
) {
    val packages by viewModel.installedPackages.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<PackageCategory?>(null) }

    val filteredPackages = remember(packages, searchQuery, selectedCategoryFilter) {
        packages.filter { item ->
            val matchesQuery = searchQuery.isEmpty() ||
                    item.appLabel.contains(searchQuery, ignoreCase = true) ||
                    item.packageName.contains(searchQuery, ignoreCase = true)
            val matchesCat = selectedCategoryFilter == null || item.category == selectedCategoryFilter
            matchesQuery && matchesCat
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            PackageHeaderBanner(totalApps = packages.size)
        }

        item {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search installed apps or packages...", color = TextMuted) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = CyanPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedIndicatorColor = CyanPrimary,
                    unfocusedIndicatorColor = DarkBorder
                )
            )
        }

        item {
            // Category Filter Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CategoryFilterPill(label = "All", isSelected = selectedCategoryFilter == null, onClick = { selectedCategoryFilter = null })
                CategoryFilterPill(label = "User Apps", isSelected = selectedCategoryFilter == PackageCategory.USER_APP, onClick = { selectedCategoryFilter = PackageCategory.USER_APP })
                CategoryFilterPill(label = "Games", isSelected = selectedCategoryFilter == PackageCategory.GAME, onClick = { selectedCategoryFilter = PackageCategory.GAME })
                CategoryFilterPill(label = "OEM", isSelected = selectedCategoryFilter == PackageCategory.OEM_OPTIONAL, onClick = { selectedCategoryFilter = PackageCategory.OEM_OPTIONAL })
            }
        }

        item {
            SectionHeader(
                title = "Installed Packages (${filteredPackages.size})",
                subtitle = "Protected packages are guarded against termination to prevent bootloops"
            )
        }

        items(filteredPackages.take(50)) { item ->
            PackageCardItem(
                item = item,
                onForceStop = { viewModel.forceStopApp(item.packageName) },
                onOpenInfo = {
                    val app = viewModel.getApplication<com.example.FluxApplication>()
                    app.container.packageAnalyzer.openAppInfo(item.packageName)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PackageHeaderBanner(totalApps: Int) {
    FluxCard(borderColor = EmeraldSuccess.copy(alpha = 0.35f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("PROCESS & PACKAGE ANALYZER", style = MaterialTheme.typography.labelSmall, color = EmeraldSuccess)
                Spacer(modifier = Modifier.height(2.dp))
                Text("$totalApps Applications Scanned", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Zero-Risk Architecture: Critical system processes (SystemUI, Telephony, Keyguard) are strictly protected.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun CategoryFilterPill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) CyanPrimary else DarkSurfaceElevated,
        border = BorderStroke(1.dp, if (isSelected) CyanPrimary else DarkBorder)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun PackageCardItem(
    item: PackageItem,
    onForceStop: () -> Unit,
    onOpenInfo: () -> Unit
) {
    FluxCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.appLabel, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(item.packageName, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val catColor = when (item.category) {
                        PackageCategory.GAME -> VioletAccent
                        PackageCategory.USER_APP -> CyanPrimary
                        PackageCategory.OEM_OPTIONAL -> AmberWarning
                        else -> TextMuted
                    }
                    FluxPill(text = item.category.name.replace("_", " "), color = catColor)

                    if (item.isProtected) {
                        FluxPill(text = "Protected", color = EmeraldSuccess, icon = Icons.Default.Lock)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onOpenInfo,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }

                if (!item.isProtected) {
                    Button(
                        onClick = onForceStop,
                        colors = ButtonDefaults.buttonColors(containerColor = RedCritical.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stop", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
