package com.example.domain

import com.example.core.capability.Backend
import com.example.core.capability.RiskLevel
import com.example.core.executor.OptimizationAction
import com.example.core.executor.OptimizationCategory
import com.example.data.device.DeviceInfo

object OptimizationRegistry {

    fun getAllActions(deviceInfo: DeviceInfo, hasShizuku: Boolean): List<OptimizationAction> {
        val actions = mutableListOf<OptimizationAction>()

        val backendForPrivileged = if (hasShizuku) Backend.SHIZUKU else Backend.UNSUPPORTED

        // --- 1. SYSTEM UI & RESPONSIVENESS (Animation Scales) ---
        actions.add(
            OptimizationAction(
                id = "anim_scale_fast",
                title = "UI Transition Speed (0.5x)",
                description = "Halves system transition delays for faster UI responsiveness.",
                technicalDescription = "Sets window_animation_scale, transition_animation_scale, and animator_duration_scale to 0.5. Improves perceived UI speed without altering CPU clock frequencies.",
                category = OptimizationCategory.SYSTEM_UI,
                risk = RiskLevel.SAFE,
                backend = backendForPrivileged,
                reversible = true,
                backupRequired = true,
                gamingRelevance = "Reduces time spent waiting on app-opening and recents transitions",
                batteryImpact = "Neutral",
                thermalImpact = "Neutral",
                verificationMethod = "Reads Settings.Global animation scale keys",
                settingNamespace = "global",
                settingKey = "animator_duration_scale",
                commandTemplate = "settings put global window_animation_scale 0.5 && settings put global transition_animation_scale 0.5 && settings put global animator_duration_scale 0.5",
                targetValue = "0.5"
            )
        )

        actions.add(
            OptimizationAction(
                id = "anim_scale_off",
                title = "Disable Animations (Instant UI)",
                description = "Completely disables window and transition animations.",
                technicalDescription = "Sets window_animation_scale, transition_animation_scale, and animator_duration_scale to 0.0. Eliminates render-wait frames.",
                category = OptimizationCategory.SYSTEM_UI,
                risk = RiskLevel.SAFE,
                backend = backendForPrivileged,
                reversible = true,
                backupRequired = true,
                gamingRelevance = "Instantaneous task switching with zero frame transition overhead",
                batteryImpact = "Slight reduction in GPU transition rendering",
                thermalImpact = "Neutral",
                verificationMethod = "Reads Settings.Global animation scale keys",
                settingNamespace = "global",
                settingKey = "animator_duration_scale",
                commandTemplate = "settings put global window_animation_scale 0 && settings put global transition_animation_scale 0 && settings put global animator_duration_scale 0",
                targetValue = "0"
            )
        )

        actions.add(
            OptimizationAction(
                id = "anim_scale_default",
                title = "Reset Animations (1.0x Default)",
                description = "Restores system default animation pacing and fluidity.",
                technicalDescription = "Restores window_animation_scale, transition_animation_scale, and animator_duration_scale to 1.0.",
                category = OptimizationCategory.SYSTEM_UI,
                risk = RiskLevel.SAFE,
                backend = backendForPrivileged,
                reversible = true,
                backupRequired = false,
                gamingRelevance = "Default system fluid motion",
                batteryImpact = "Standard",
                thermalImpact = "Standard",
                verificationMethod = "Reads Settings.Global animation scale keys",
                settingNamespace = "global",
                settingKey = "animator_duration_scale",
                commandTemplate = "settings put global window_animation_scale 1.0 && settings put global transition_animation_scale 1.0 && settings put global animator_duration_scale 1.0",
                targetValue = "1.0"
            )
        )

        // --- 2. DISPLAY & REFRESH RATE ---
        val has120Hz = deviceInfo.supportedRefreshRates.any { it >= 119f }
        val has90Hz = deviceInfo.supportedRefreshRates.any { it in 89f..95f }

        if (has120Hz) {
            actions.add(
                OptimizationAction(
                    id = "force_120hz",
                    title = "Lock Display to 120 Hz",
                    description = "Locks minimum and peak refresh rate to 120 Hz to eliminate frame drops.",
                    technicalDescription = "Sets user_refresh_rate=120 and min_refresh_rate=120 in Settings.System/Secure. Prevents dynamic dropping to 60Hz inside games.",
                    category = OptimizationCategory.DISPLAY,
                    risk = RiskLevel.LOW,
                    backend = backendForPrivileged,
                    reversible = true,
                    backupRequired = true,
                    gamingRelevance = "Essential for 120 FPS high-refresh-rate titles (PUBG, COD, Brawl Stars)",
                    batteryImpact = "Moderate (~10-15% higher panel power draw)",
                    thermalImpact = "Slight panel warming under prolonged use",
                    verificationMethod = "Queries active Display.refreshRate from DisplayManager",
                    settingNamespace = "system",
                    settingKey = "user_refresh_rate",
                    commandTemplate = "settings put system user_refresh_rate 120 && settings put system min_refresh_rate 120.0 && settings put system peak_refresh_rate 120.0",
                    targetValue = "120"
                )
            )
        }

        if (has90Hz) {
            actions.add(
                OptimizationAction(
                    id = "force_90hz",
                    title = "Balance Display at 90 Hz",
                    description = "Locks panel refresh rate to 90 Hz for smooth fluidity with lower power draw.",
                    technicalDescription = "Sets user_refresh_rate=90 and peak_refresh_rate=90. Balances smoothness and battery life.",
                    category = OptimizationCategory.DISPLAY,
                    risk = RiskLevel.LOW,
                    backend = backendForPrivileged,
                    reversible = true,
                    backupRequired = true,
                    gamingRelevance = "Optimal balance for battery-conscious gaming",
                    batteryImpact = "Mild",
                    thermalImpact = "Minimal",
                    verificationMethod = "Queries active Display.refreshRate from DisplayManager",
                    settingNamespace = "system",
                    settingKey = "user_refresh_rate",
                    commandTemplate = "settings put system user_refresh_rate 90 && settings put system peak_refresh_rate 90.0",
                    targetValue = "90"
                )
            )
        }

        actions.add(
            OptimizationAction(
                id = "reset_refresh_rate",
                title = "Reset Dynamic Refresh Rate (Default)",
                description = "Restores OEM dynamic adaptive refresh rate behavior.",
                technicalDescription = "Removes fixed refresh rate overrides, allowing the panel to drop to 60Hz when idle.",
                category = OptimizationCategory.DISPLAY,
                risk = RiskLevel.SAFE,
                backend = backendForPrivileged,
                reversible = true,
                backupRequired = false,
                gamingRelevance = "Standard battery-preserving adaptive rate",
                batteryImpact = "Optimal",
                thermalImpact = "Lowest",
                verificationMethod = "Reads system settings",
                settingNamespace = "system",
                settingKey = "user_refresh_rate",
                commandTemplate = "settings put system min_refresh_rate 60.0 && settings put system peak_refresh_rate 120.0",
                targetValue = "default"
            )
        )

        // --- 3. GAMING & PERFORMANCE MODE ---
        if (deviceInfo.apiLevel >= 31) { // Android 12+ Game Mode API
            actions.add(
                OptimizationAction(
                    id = "game_mode_global",
                    title = "Android Game Mode (Performance State)",
                    description = "Activates standard Android Game Mode scheduler priority.",
                    technicalDescription = "Interacts with 'cmd game' Android system service to apply PERFORMANCE mode heuristics to recognized game packages.",
                    category = OptimizationCategory.GAMING,
                    risk = RiskLevel.LOW,
                    backend = backendForPrivileged,
                    reversible = true,
                    backupRequired = false,
                    gamingRelevance = "Signals game scheduler to prioritize CPU core allocation for active game",
                    batteryImpact = "Moderate",
                    thermalImpact = "Device dependent",
                    verificationMethod = "Queries 'cmd game mode <pkg>'",
                    commandTemplate = "cmd game set --mode 2 {VALUE}",
                    targetValue = "performance"
                )
            )
        }

        // --- 4. XIAOMI / HYPEROS / POCO SPECIFIC TWEAKS ---
        if (deviceInfo.isHyperOs || deviceInfo.manufacturer.equals("Xiaomi", ignoreCase = true)) {
            actions.add(
                OptimizationAction(
                    id = "hyperos_touch_game",
                    title = "HyperOS Touch Game Mode Input Pacing",
                    description = "Forces the Xiaomi input driver into high-frequency sampling mode.",
                    technicalDescription = "Writes 'touch_game_mode=1' to Settings.Secure. Synchronizes touch digitizer interrupt polling with display vsync.",
                    category = OptimizationCategory.INPUT,
                    risk = RiskLevel.SAFE,
                    backend = backendForPrivileged,
                    reversible = true,
                    backupRequired = true,
                    gamingRelevance = "Lowers touch latency during fast aiming and swiping in games",
                    batteryImpact = "Negligible",
                    thermalImpact = "Negligible",
                    verificationMethod = "Reads Settings.Secure touch_game_mode",
                    settingNamespace = "secure",
                    settingKey = "touch_game_mode",
                    commandTemplate = "settings put secure touch_game_mode 1",
                    targetValue = "1"
                )
            )
        }

        // --- 5. MEMORY & BACKGROUND APPS ---
        actions.add(
            OptimizationAction(
                id = "kill_bg_apps",
                title = "Trim Background Memory Caches",
                description = "Trims inactive background process memory using native Android ActivityManager.",
                technicalDescription = "Calls ActivityManager.killBackgroundProcesses(). Frees temporary memory without affecting foreground apps or system services.",
                category = OptimizationCategory.MEMORY,
                risk = RiskLevel.SAFE,
                backend = Backend.NATIVE_API,
                reversible = false,
                backupRequired = false,
                gamingRelevance = "Reclaims available physical RAM prior to launching memory-heavy games",
                batteryImpact = "Neutral",
                thermalImpact = "Neutral",
                verificationMethod = "Monitors ActivityManager.MemoryInfo availMem",
                targetValue = "trim"
            )
        )

        return actions
    }
}
