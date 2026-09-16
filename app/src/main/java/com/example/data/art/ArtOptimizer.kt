package com.example.data.art

import com.example.core.logging.FluxLogger
import com.example.core.logging.LogCategory
import com.example.data.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ArtCompilationResult(
    val success: Boolean,
    val packageName: String,
    val mode: String,
    val outputMessage: String
)

class ArtOptimizer(
    private val shizukuManager: ShizukuManager
) {

    suspend fun getCompilationStatus(packageName: String): String = withContext(Dispatchers.IO) {
        if (!shizukuManager.hasPermission()) {
            return@withContext "Status requires Shizuku permission"
        }

        try {
            val cmd = "dumpsys package $packageName | grep -iE '(dexopt|compiler filter|compilation)' | head -n 3"
            val out = shizukuManager.executePrivilegedOperation(cmd)
            if (out.exitCode == 0 && out.stdout.isNotBlank()) {
                out.stdout.trim()
            } else {
                "Standard JIT / Default Profile"
            }
        } catch (_: Exception) {
            "Unknown compilation status"
        }
    }

    suspend fun compilePackage(
        packageName: String,
        mode: String = "speed-profile"
    ): ArtCompilationResult = withContext(Dispatchers.IO) {
        if (!shizukuManager.hasPermission()) {
            return@withContext ArtCompilationResult(
                success = false,
                packageName = packageName,
                mode = mode,
                outputMessage = "Shizuku authorization is required for ART compilation"
            )
        }

        // Validate allowed mode
        val validModes = listOf("speed-profile", "speed", "verify", "quicken")
        val targetMode = if (mode in validModes) mode else "speed-profile"

        FluxLogger.i(LogCategory.EXECUTION, "ArtOptimizer", "Triggering ART dex2oat for $packageName (mode=$targetMode)")

        val cmd = "cmd package compile -m $targetMode -f $packageName"
        val out = shizukuManager.executePrivilegedOperation(cmd)

        val success = out.exitCode == 0 && (out.stdout.contains("Success", ignoreCase = true) || out.stdout.isBlank())

        ArtCompilationResult(
            success = success,
            packageName = packageName,
            mode = targetMode,
            outputMessage = if (success) "ART compilation completed ($targetMode). Application code pre-compiled to native dex."
            else "Compilation returned code ${out.exitCode}: ${out.stderr.ifBlank { out.stdout }}"
        )
    }

    suspend fun resetCompilation(packageName: String): ArtCompilationResult = withContext(Dispatchers.IO) {
        if (!shizukuManager.hasPermission()) {
            return@withContext ArtCompilationResult(
                success = false,
                packageName = packageName,
                mode = "reset",
                outputMessage = "Shizuku authorization required"
            )
        }

        val cmd = "cmd package compile --reset $packageName"
        val out = shizukuManager.executePrivilegedOperation(cmd)
        val success = out.exitCode == 0

        ArtCompilationResult(
            success = success,
            packageName = packageName,
            mode = "reset",
            outputMessage = if (success) "ART compilation cache reset for $packageName."
            else "Reset returned: ${out.stderr}"
        )
    }
}
