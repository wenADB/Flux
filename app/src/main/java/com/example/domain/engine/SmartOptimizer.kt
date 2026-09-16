package com.example.domain.engine

import com.example.core.capability.Backend
import com.example.core.executor.OptimizationAction
import com.example.data.device.DeviceInfo
import com.example.data.performance.PerformanceTelemetry
import com.example.data.shizuku.ShizukuManager
import com.example.data.shizuku.ShizukuStatus
import com.example.data.thermal.ThermalTelemetry
import com.example.domain.OptimizationRegistry

data class SmartRecommendation(
    val id: String,
    val title: String,
    val reason: String,
    val expectedGain: String,
    val action: OptimizationAction?,
    val isExecutable: Boolean
)

data class SystemHealthScore(
    val overallScore: Int, // 0 - 100
    val thermalScore: Int,
    val memoryScore: Int,
    val displayScore: Int,
    val privilegeScore: Int,
    val formulaExplanation: String
)

class SmartOptimizer(
    private val shizukuManager: ShizukuManager
) {

    fun calculateHealthScore(
        deviceInfo: DeviceInfo,
        perf: PerformanceTelemetry,
        thermal: ThermalTelemetry
    ): SystemHealthScore {
        // 1. Thermal score (0-25): 25 if <= 35C, dropping to 5 if >= 45C
        val temp = thermal.temperatureCelsius
        val thermalScore = when {
            temp <= 35f -> 25
            temp <= 39f -> 20
            temp <= 42f -> 14
            temp <= 45f -> 7
            else -> 2
        }

        // 2. Memory score (0-25): Based on free RAM percentage
        val ramPct = perf.ramUsagePct
        val memoryScore = when {
            ramPct < 60 -> 25
            ramPct < 75 -> 20
            ramPct < 85 -> 15
            ramPct < 92 -> 8
            else -> 4
        }

        // 3. Display score (0-25): 25 if utilizing max panel refresh rate
        val maxSupportedHz = deviceInfo.supportedRefreshRates.maxOrNull() ?: 60f
        val displayScore = if (kotlin.math.abs(deviceInfo.currentRefreshRate - maxSupportedHz) < 1f) 25 else 16

        // 4. Privilege score (0-25): 25 if Shizuku authorized
        val privilegeScore = if (shizukuManager.hasPermission()) 25 else 10

        val total = (thermalScore + memoryScore + displayScore + privilegeScore).coerceIn(0, 100)

        val formula = "Calculated from: Thermal Headroom ($thermalScore/25) + RAM Pressure ($memoryScore/25) + Display Refresh ($displayScore/25) + Shizuku Privilege ($privilegeScore/25)"

        return SystemHealthScore(
            overallScore = total,
            thermalScore = thermalScore,
            memoryScore = memoryScore,
            displayScore = displayScore,
            privilegeScore = privilegeScore,
            formulaExplanation = formula
        )
    }

    fun generateRecommendations(
        deviceInfo: DeviceInfo,
        perf: PerformanceTelemetry,
        thermal: ThermalTelemetry
    ): List<SmartRecommendation> {
        val recs = mutableListOf<SmartRecommendation>()
        val hasShizuku = shizukuManager.hasPermission()
        val actions = OptimizationRegistry.getAllActions(deviceInfo, hasShizuku)

        // 1. Thermal constraint alert
        if (thermal.isThermallyConstrained) {
            recs.add(
                SmartRecommendation(
                    id = "thermal_warning",
                    title = "Thermal Throttling Active",
                    reason = "Device temperature is ${thermal.temperatureCelsius}°C (Status: ${thermal.thermalStatus}). High-performance changes are temporarily paused to protect hardware.",
                    expectedGain = "Cooling down prevents hardware throttling",
                    action = null,
                    isExecutable = false
                )
            )
        }

        // 2. Refresh Rate Recommendation
        val maxHz = deviceInfo.supportedRefreshRates.maxOrNull() ?: 60f
        if (maxHz > 60f && deviceInfo.currentRefreshRate < (maxHz - 5f)) {
            val action120 = actions.firstOrNull { it.id == "force_120hz" || it.id == "force_90hz" }
            recs.add(
                SmartRecommendation(
                    id = "rec_refresh_rate",
                    title = "Enable High Refresh Rate (${maxHz.toInt()}Hz)",
                    reason = "Your screen supports ${maxHz.toInt()}Hz, but current operating mode is ${deviceInfo.currentRefreshRate.toInt()}Hz.",
                    expectedGain = "Substantially smoother scrolling and 2x higher input fluidity",
                    action = action120,
                    isExecutable = hasShizuku && action120 != null
                )
            )
        }

        // 3. Shizuku Authorization Recommendation
        if (!hasShizuku) {
            val statusMsg = when (shizukuManager.state.value.status) {
                ShizukuStatus.UNAVAILABLE -> "Install Shizuku to unlock rootless system tweaking."
                ShizukuStatus.NOT_RUNNING -> "Start Shizuku service via Wireless Debugging to activate privileged execution."
                else -> "Authorize FLUX in Shizuku to grant permission."
            }
            recs.add(
                SmartRecommendation(
                    id = "rec_shizuku",
                    title = "Authorize Shizuku Privilege Bridge",
                    reason = statusMsg,
                    expectedGain = "Enables refresh rate locking, Game Mode, and UI scaling without root",
                    action = null,
                    isExecutable = true
                )
            )
        }

        // 4. Animation scale optimization
        val fastAnimAction = actions.firstOrNull { it.id == "anim_scale_fast" }
        if (fastAnimAction != null) {
            recs.add(
                SmartRecommendation(
                    id = "rec_anim_scale",
                    title = "Accelerate UI Transition Delay (0.5x)",
                    reason = "Standard system transitions introduce 1.0x animation waits on app switching.",
                    expectedGain = "Immediate perceived UI responsiveness across app launches",
                    action = fastAnimAction,
                    isExecutable = hasShizuku
                )
            )
        }

        // 5. Memory trim recommendation if RAM usage high
        if (perf.ramUsagePct > 75) {
            val trimAction = actions.firstOrNull { it.id == "kill_bg_apps" }
            recs.add(
                SmartRecommendation(
                    id = "rec_trim_ram",
                    title = "Trim Inactive Background App Caches",
                    reason = "System RAM usage is high (${perf.ramUsagePct}% used). Inactive background caches can be reclaimed.",
                    expectedGain = "Frees physical RAM without restarting system services",
                    action = trimAction,
                    isExecutable = true
                )
            )
        }

        // 6. HyperOS Touch Pacing
        if (deviceInfo.isHyperOs && hasShizuku) {
            val touchAction = actions.firstOrNull { it.id == "hyperos_touch_game" }
            if (touchAction != null) {
                recs.add(
                    SmartRecommendation(
                        id = "rec_hyperos_touch",
                        title = "Activate HyperOS Touch Digitizer Pacing",
                        reason = "Xiaomi HyperOS supports synchronized touch digitizer polling in games.",
                        expectedGain = "Reduces input registration lag during gaming",
                        action = touchAction,
                        isExecutable = true
                    )
                )
            }
        }

        return recs
    }
}
