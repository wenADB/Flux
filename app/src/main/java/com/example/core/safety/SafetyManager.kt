package com.example.core.safety

import com.example.core.capability.Backend
import com.example.core.capability.RiskLevel
import com.example.core.logging.FluxLogger
import com.example.core.logging.LogCategory
import com.example.data.device.DeviceInfo
import com.example.data.shizuku.ShizukuManager

data class SafetyEvaluation(
    val isSafe: Boolean,
    val canProceed: Boolean,
    val riskLevel: RiskLevel,
    val warningMessage: String?,
    val blockReason: String?
)

class SafetyManager(
    private val shizukuManager: ShizukuManager
) {

    fun evaluateThermalState(deviceInfo: DeviceInfo): SafetyEvaluation {
        val temp = deviceInfo.batteryTempCelsius
        val status = deviceInfo.thermalStatus

        if (status == "SEVERE" || status == "CRITICAL" || status == "EMERGENCY" || temp >= 45.0f) {
            FluxLogger.w(
                LogCategory.THERMAL,
                "SafetyManager",
                "Thermal constraint triggered: Status=$status, Temp=${temp}°C"
            )
            return SafetyEvaluation(
                isSafe = false,
                canProceed = false,
                riskLevel = RiskLevel.HIGH,
                warningMessage = "Optimization paused because the device is thermally constrained (${temp}°C, status: $status). Allow device to cool before applying performance tweaks.",
                blockReason = "DEVICE_OVERHEATING"
            )
        }

        if (status == "MODERATE" || temp >= 40.0f) {
            return SafetyEvaluation(
                isSafe = true,
                canProceed = true,
                riskLevel = RiskLevel.MODERATE,
                warningMessage = "Device temperature is elevated (${temp}°C). Performance modes may induce temporary thermal throttling.",
                blockReason = null
            )
        }

        return SafetyEvaluation(
            isSafe = true,
            canProceed = true,
            riskLevel = RiskLevel.SAFE,
            warningMessage = null,
            blockReason = null
        )
    }

    fun evaluateAction(
        actionTitle: String,
        requiredBackend: Backend,
        risk: RiskLevel,
        targetPackage: String? = null,
        deviceInfo: DeviceInfo
    ): SafetyEvaluation {
        // 1. Thermal constraint check
        val thermalEval = evaluateThermalState(deviceInfo)
        if (!thermalEval.canProceed && risk != RiskLevel.SAFE) {
            return thermalEval
        }

        // 2. Protected package check
        if (targetPackage != null && ProtectedPackages.isProtected(targetPackage)) {
            FluxLogger.w(
                LogCategory.SAFETY,
                "SafetyManager",
                "Blocked modification of protected package: $targetPackage"
            )
            return SafetyEvaluation(
                isSafe = false,
                canProceed = false,
                riskLevel = RiskLevel.HIGH,
                warningMessage = "Package '$targetPackage' is a system-critical or UI component and cannot be disabled or stopped.",
                blockReason = "PROTECTED_SYSTEM_PACKAGE"
            )
        }

        // 3. Backend availability check
        when (requiredBackend) {
            Backend.NATIVE_API -> {
                // Native API is supported
            }
            Backend.SHIZUKU -> {
                if (!shizukuManager.hasPermission()) {
                    return SafetyEvaluation(
                        isSafe = true,
                        canProceed = false,
                        riskLevel = risk,
                        warningMessage = "This action requires Shizuku permission. Please authorize FLUX in Shizuku.",
                        blockReason = "SHIZUKU_PERMISSION_MISSING"
                    )
                }
            }
            Backend.ADB -> {
                return SafetyEvaluation(
                    isSafe = true,
                    canProceed = false,
                    riskLevel = risk,
                    warningMessage = "This action requires an active ADB shell connection or Shizuku bridge.",
                    blockReason = "ADB_REQUIRED"
                )
            }
            Backend.UNSUPPORTED -> {
                return SafetyEvaluation(
                    isSafe = false,
                    canProceed = false,
                    riskLevel = RiskLevel.UNSUPPORTED,
                    warningMessage = "This operation is not supported by your current device or Android SDK version.",
                    blockReason = "UNSUPPORTED_OPERATION"
                )
            }
        }

        // 4. Low battery safety
        if (deviceInfo.batteryPct < 15) {
            return SafetyEvaluation(
                isSafe = true,
                canProceed = true,
                riskLevel = RiskLevel.LOW,
                warningMessage = "Battery level is low (${deviceInfo.batteryPct}%). High refresh rates or background processing may accelerate discharge.",
                blockReason = null
            )
        }

        return SafetyEvaluation(
            isSafe = true,
            canProceed = true,
            riskLevel = risk,
            warningMessage = null,
            blockReason = null
        )
    }
}
