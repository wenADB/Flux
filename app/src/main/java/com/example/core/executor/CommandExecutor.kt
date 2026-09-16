package com.example.core.executor

import android.app.ActivityManager
import android.content.Context
import com.example.core.backup.BackupManager
import com.example.core.capability.Backend
import com.example.core.logging.FluxLogger
import com.example.core.logging.LogCategory
import com.example.core.safety.SafetyEvaluation
import com.example.core.safety.SafetyManager
import com.example.core.verification.VerificationEngine
import com.example.data.database.OptimizationHistoryDao
import com.example.data.database.OptimizationHistoryEntity
import com.example.data.device.DeviceInfo
import com.example.data.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface CommandExecutor {
    suspend fun execute(action: OptimizationAction, customValue: String? = null): ExecutionResult
}

class AndroidApiExecutor(
    private val context: Context
) : CommandExecutor {
    override suspend fun execute(action: OptimizationAction, customValue: String?): ExecutionResult = withContext(Dispatchers.IO) {
        FluxLogger.i(LogCategory.EXECUTION, "AndroidApiExecutor", "Executing native action: ${action.id}")
        try {
            when (action.id) {
                "kill_bg_apps" -> {
                    val actMgr = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                    // Safe cleanup: kills background processes that have no foreground service
                    actMgr?.killBackgroundProcesses(customValue ?: context.packageName)
                    ExecutionResult.Success(
                        message = "Background caches trim signaled via ActivityManager",
                        verified = true
                    )
                }
                else -> {
                    ExecutionResult.Unsupported("No native Android API handler for ${action.id}")
                }
            }
        } catch (e: Throwable) {
            ExecutionResult.Failure("Native API execution error: ${e.message}")
        }
    }
}

class ShizukuExecutor(
    private val shizukuManager: ShizukuManager
) : CommandExecutor {
    override suspend fun execute(action: OptimizationAction, customValue: String?): ExecutionResult = withContext(Dispatchers.IO) {
        if (!shizukuManager.hasPermission()) {
            return@withContext ExecutionResult.PermissionDenied("Shizuku authorization required")
        }

        val targetVal = customValue ?: action.targetValue ?: "1"
        val command = when {
            action.commandTemplate != null -> {
                action.commandTemplate.replace("{VALUE}", targetVal)
            }
            action.settingNamespace != null && action.settingKey != null -> {
                "settings put ${action.settingNamespace} ${action.settingKey} $targetVal"
            }
            else -> null
        }

        if (command == null) {
            return@withContext ExecutionResult.Failure("No executable command configured for ${action.id}")
        }

        FluxLogger.i(LogCategory.EXECUTION, "ShizukuExecutor", "Executing privileged command: $command")
        val result = shizukuManager.executePrivilegedOperation(command)

        if (result.exitCode == 0) {
            ExecutionResult.Success(
                message = "Executed successfully via Shizuku: $command",
                verified = false,
                newValue = targetVal
            )
        } else {
            val err = if (result.stderr.isNotBlank()) result.stderr else "Exit code ${result.exitCode}"
            FluxLogger.e(LogCategory.EXECUTION, "ShizukuExecutor", "Command failed: $err")
            ExecutionResult.Failure("Command failed with code ${result.exitCode}: $err", result.exitCode, result.stderr)
        }
    }
}

class AdbExecutor : CommandExecutor {
    override suspend fun execute(action: OptimizationAction, customValue: String?): ExecutionResult {
        return ExecutionResult.Unsupported("ADB over TCP is not connected. Use Shizuku for zero-cable privileged execution.")
    }
}

class UnsupportedExecutor : CommandExecutor {
    override suspend fun execute(action: OptimizationAction, customValue: String?): ExecutionResult {
        return ExecutionResult.Unsupported("Operation is unsupported on current hardware/platform")
    }
}

class FluxExecutionEngine(
    private val context: Context,
    private val shizukuManager: ShizukuManager,
    private val safetyManager: SafetyManager,
    private val backupManager: BackupManager,
    private val verificationEngine: VerificationEngine,
    private val historyDao: OptimizationHistoryDao
) {
    private val nativeExecutor = AndroidApiExecutor(context)
    private val shizukuExecutor = ShizukuExecutor(shizukuManager)
    private val adbExecutor = AdbExecutor()
    private val unsupportedExecutor = UnsupportedExecutor()

    suspend fun executePipeline(
        action: OptimizationAction,
        customValue: String? = null,
        deviceInfo: DeviceInfo
    ): OptimizationResult = withContext(Dispatchers.IO) {
        val targetVal = customValue ?: action.targetValue ?: "1"

        // 1. SAFETY CHECK
        val safety = safetyManager.evaluateAction(
            actionTitle = action.title,
            requiredBackend = action.backend,
            risk = action.risk,
            targetPackage = if (action.id.startsWith("pkg_")) customValue else null,
            deviceInfo = deviceInfo
        )

        if (!safety.canProceed) {
            val errorMsg = safety.blockReason ?: safety.warningMessage ?: "Action blocked by safety manager"
            logHistory(action, null, targetVal, "FAILED", false, "Safety check failed", errorMsg, deviceInfo.model)
            return@withContext OptimizationResult(
                id = action.id,
                success = false,
                verified = false,
                oldValue = null,
                newValue = targetVal,
                message = "Blocked: $errorMsg",
                error = errorMsg
            )
        }

        // 2. BACKUP CURRENT STATE
        var backupId: Long = -1
        if (action.backupRequired && action.settingNamespace != null && action.settingKey != null) {
            backupId = backupManager.createBackup(
                namespace = action.settingNamespace,
                key = action.settingKey,
                newValue = targetVal,
                deviceModel = deviceInfo.model
            )
        }

        // 3. SELECT EXECUTOR
        val executor: CommandExecutor = when (action.backend) {
            Backend.NATIVE_API -> nativeExecutor
            Backend.SHIZUKU -> {
                if (shizukuManager.hasPermission()) shizukuExecutor else unsupportedExecutor
            }
            Backend.ADB -> adbExecutor
            Backend.UNSUPPORTED -> unsupportedExecutor
        }

        // 4. EXECUTE
        val execResult = executor.execute(action, targetVal)

        when (execResult) {
            is ExecutionResult.Success -> {
                // 5. VERIFY
                val verifyOutcome = if (action.settingNamespace != null && action.settingKey != null) {
                    verificationEngine.verifySetting(action.settingNamespace, action.settingKey, targetVal)
                } else if (action.id == "display_refresh_rate" || action.id == "force_120hz") {
                    verificationEngine.verifyRefreshRate(targetVal.toFloatOrNull() ?: 120f)
                } else {
                    com.example.core.verification.VerificationOutcome(
                        isVerified = false,
                        currentValue = targetVal,
                        message = "Applied; verification unavailable for this API type"
                    )
                }

                val statusStr = if (verifyOutcome.isVerified) "SUCCESS" else "SUCCESS (UNVERIFIED)"
                logHistory(
                    action = action,
                    oldValue = null,
                    newValue = targetVal,
                    result = statusStr,
                    verified = verifyOutcome.isVerified,
                    verifyMsg = verifyOutcome.message,
                    error = null,
                    deviceModel = deviceInfo.model
                )

                OptimizationResult(
                    id = action.id,
                    success = true,
                    verified = verifyOutcome.isVerified,
                    oldValue = null,
                    newValue = targetVal,
                    message = verifyOutcome.message,
                    error = null
                )
            }
            is ExecutionResult.Failure -> {
                logHistory(action, null, targetVal, "FAILED", false, "Execution failed", execResult.error, deviceInfo.model)
                OptimizationResult(
                    id = action.id,
                    success = false,
                    verified = false,
                    oldValue = null,
                    newValue = targetVal,
                    message = "Execution failed: ${execResult.error}",
                    error = execResult.error
                )
            }
            is ExecutionResult.PermissionDenied -> {
                logHistory(action, null, targetVal, "PERMISSION_DENIED", false, "Permission missing", execResult.requiredPermission, deviceInfo.model)
                OptimizationResult(
                    id = action.id,
                    success = false,
                    verified = false,
                    oldValue = null,
                    newValue = targetVal,
                    message = "Permission Denied: ${execResult.requiredPermission}",
                    error = "PERMISSION_DENIED"
                )
            }
            is ExecutionResult.Unsupported -> {
                logHistory(action, null, targetVal, "UNSUPPORTED", false, "Feature unsupported", execResult.reason, deviceInfo.model)
                OptimizationResult(
                    id = action.id,
                    success = false,
                    verified = false,
                    oldValue = null,
                    newValue = targetVal,
                    message = "Unsupported: ${execResult.reason}",
                    error = "UNSUPPORTED"
                )
            }
            else -> {
                OptimizationResult(
                    id = action.id,
                    success = false,
                    verified = false,
                    oldValue = null,
                    newValue = targetVal,
                    message = "Unknown execution status",
                    error = "UNKNOWN"
                )
            }
        }
    }

    private suspend fun logHistory(
        action: OptimizationAction,
        oldValue: String?,
        newValue: String?,
        result: String,
        verified: Boolean,
        verifyMsg: String,
        error: String?,
        deviceModel: String
    ) {
        try {
            historyDao.insert(
                OptimizationHistoryEntity(
                    timestamp = System.currentTimeMillis(),
                    device = deviceModel,
                    actionId = action.id,
                    actionTitle = action.title,
                    category = action.category.name,
                    oldValue = oldValue,
                    newValue = newValue,
                    result = result,
                    verified = verified,
                    verificationMessage = verifyMsg,
                    errorMessage = error
                )
            )
        } catch (e: Exception) {
            FluxLogger.e(LogCategory.GENERAL, "FluxExecutionEngine", "Failed to write history log: ${e.message}")
        }
    }
}
