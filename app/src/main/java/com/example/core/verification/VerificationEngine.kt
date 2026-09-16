package com.example.core.verification

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.provider.Settings
import android.view.Display
import com.example.core.logging.FluxLogger
import com.example.core.logging.LogCategory
import com.example.data.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class VerificationOutcome(
    val isVerified: Boolean,
    val currentValue: String?,
    val message: String
)

class VerificationEngine(
    private val context: Context,
    private val shizukuManager: ShizukuManager
) {

    suspend fun verifySetting(
        namespace: String,
        key: String,
        expectedValue: String
    ): VerificationOutcome = withContext(Dispatchers.IO) {
        try {
            // First attempt native reading
            var actual: String? = when (namespace.lowercase()) {
                "global" -> Settings.Global.getString(context.contentResolver, key)
                "secure" -> Settings.Secure.getString(context.contentResolver, key)
                "system" -> Settings.System.getString(context.contentResolver, key)
                else -> null
            }

            // If null and Shizuku is authorized, query via privileged shell
            if (actual == null && shizukuManager.hasPermission()) {
                val out = shizukuManager.executePrivilegedOperation("settings get $namespace $key")
                if (out.exitCode == 0 && out.stdout.isNotBlank() && out.stdout != "null") {
                    actual = out.stdout.trim()
                }
            }

            if (actual == null) {
                FluxLogger.w(LogCategory.VERIFICATION, "VerificationEngine", "Setting $key value unreadable for verification")
                return@withContext VerificationOutcome(
                    isVerified = false,
                    currentValue = null,
                    message = "Applied; verification unavailable (read access restricted for $key)"
                )
            }

            val matches = actual.trim().equals(expectedValue.trim(), ignoreCase = true)
            FluxLogger.i(
                LogCategory.VERIFICATION,
                "VerificationEngine",
                "Verify setting $namespace:$key -> Expected='$expectedValue', Actual='$actual' (Matches=$matches)"
            )

            VerificationOutcome(
                isVerified = matches,
                currentValue = actual,
                message = if (matches) "Verified: $key successfully set to $actual"
                else "Verification discrepancy: Expected '$expectedValue' but found '$actual'"
            )
        } catch (e: Throwable) {
            FluxLogger.e(LogCategory.VERIFICATION, "VerificationEngine", "Verification exception: ${e.message}")
            VerificationOutcome(
                isVerified = false,
                currentValue = null,
                message = "Applied; verification unavailable (${e.message})"
            )
        }
    }

    suspend fun verifyPackageState(
        packageName: String,
        expectedDisabled: Boolean
    ): VerificationOutcome = withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            val state = pm.getApplicationEnabledSetting(packageName)
            val isDisabled = state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                    state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER

            val verified = (expectedDisabled == isDisabled)
            VerificationOutcome(
                isVerified = verified,
                currentValue = if (isDisabled) "DISABLED" else "ENABLED",
                message = if (verified) "Verified: $packageName state confirmed as ${if (isDisabled) "Disabled" else "Enabled"}"
                else "Verification failed: Package state did not match expected"
            )
        } catch (e: Exception) {
            VerificationOutcome(
                isVerified = false,
                currentValue = null,
                message = "Applied; verification unavailable for package $packageName"
            )
        }
    }

    suspend fun verifyRefreshRate(expectedHz: Float): VerificationOutcome = withContext(Dispatchers.IO) {
        try {
            val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            val curRate = dm?.getDisplay(Display.DEFAULT_DISPLAY)?.refreshRate ?: -1f
            val matches = kotlin.math.abs(curRate - expectedHz) < 1.0f

            VerificationOutcome(
                isVerified = matches,
                currentValue = "${curRate}Hz",
                message = if (matches) "Verified: Display operating at ${curRate}Hz"
                else "Applied; current active refresh rate is ${curRate}Hz (system policy may override based on app)"
            )
        } catch (e: Exception) {
            VerificationOutcome(
                isVerified = false,
                currentValue = null,
                message = "Applied; refresh rate verification unavailable"
            )
        }
    }
}
