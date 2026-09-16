package com.example.core.executor

import com.example.core.capability.Backend
import com.example.core.capability.RiskLevel

enum class OptimizationCategory(val displayName: String) {
    PERFORMANCE("Performance"),
    GAMING("Gaming Engine"),
    DISPLAY("Display & Refresh Rate"),
    THERMAL("Thermal Management"),
    MEMORY("Memory & RAM"),
    STORAGE("Storage & Cache"),
    ART("ART Compilation"),
    NETWORK("Network & Latency"),
    BATTERY("Battery & Power"),
    SYSTEM_UI("UI & Responsiveness"),
    BACKGROUND_APPS("Background Execution"),
    INPUT("Touch & Input Pacing"),
    DIAGNOSTICS("Hardware Diagnostics")
}

data class OptimizationAction(
    val id: String,
    val title: String,
    val description: String,
    val technicalDescription: String,
    val category: OptimizationCategory,
    val risk: RiskLevel,
    val backend: Backend,
    val requiresRestart: Boolean = false,
    val reversible: Boolean = true,
    val backupRequired: Boolean = true,
    val gamingRelevance: String,
    val batteryImpact: String,
    val thermalImpact: String,
    val verificationMethod: String,
    val settingNamespace: String? = null,
    val settingKey: String? = null,
    val commandTemplate: String? = null,
    val targetValue: String? = null
)
