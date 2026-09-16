package com.example.data.shizuku

import android.content.Context
import android.content.pm.PackageManager
import com.example.core.logging.FluxLogger
import com.example.core.logging.LogCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

enum class ShizukuStatus(val label: String) {
    UNAVAILABLE("Shizuku Not Installed"),
    NOT_RUNNING("Shizuku Installed (Service Not Running)"),
    PERMISSION_MISSING("Shizuku Running (Permission Required)"),
    PERMISSION_GRANTED("Shizuku Connected & Authorized"),
    PERMISSION_REVOKED("Shizuku Permission Denied/Revoked")
}

data class ShizukuState(
    val status: ShizukuStatus = ShizukuStatus.UNAVAILABLE,
    val version: Int = 0,
    val uid: Int = -1,
    val isAvailable: Boolean = false,
    val statusMessage: String = "Checking Shizuku status..."
)

data class ShizukuCommandOutput(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
)

class ShizukuManager(private val context: Context) {

    private val _state = MutableStateFlow(ShizukuState())
    val state: StateFlow<ShizukuState> = _state.asStateFlow()

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        val granted = grantResult == PackageManager.PERMISSION_GRANTED
        FluxLogger.i(
            LogCategory.SHIZUKU,
            "ShizukuManager",
            "Permission result received for req=$requestCode, granted=$granted"
        )
        refreshState()
    }

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        FluxLogger.i(LogCategory.SHIZUKU, "ShizukuManager", "Shizuku binder connected")
        refreshState()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        FluxLogger.w(LogCategory.SHIZUKU, "ShizukuManager", "Shizuku binder died")
        refreshState()
    }

    init {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionListener)
        } catch (e: Throwable) {
            FluxLogger.w(LogCategory.SHIZUKU, "ShizukuManager", "Failed to register Shizuku listeners: ${e.message}")
        }
        refreshState()
    }

    fun isInstalled(): Boolean {
        return try {
            val pm = context.packageManager
            pm.getPackageInfo("moe.shizuku.privileged.api", 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun isRunning(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }
    }

    fun hasPermission(): Boolean {
        return try {
            if (!isRunning()) return false
            if (Shizuku.isPre_V11()) return false
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }
    }

    fun getVersion(): Int {
        return try {
            if (isRunning()) Shizuku.getVersion() else 0
        } catch (_: Throwable) {
            0
        }
    }

    fun getUid(): Int {
        return try {
            if (isRunning()) Shizuku.getUid() else -1
        } catch (_: Throwable) {
            -1
        }
    }

    fun requestPermission(requestCode: Int = 1001) {
        try {
            if (!isRunning()) {
                FluxLogger.w(LogCategory.SHIZUKU, "ShizukuManager", "Cannot request permission: Shizuku is not running")
                return
            }
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                FluxLogger.i(LogCategory.SHIZUKU, "ShizukuManager", "Requesting Shizuku permission with code $requestCode")
                Shizuku.requestPermission(requestCode)
            } else {
                refreshState()
            }
        } catch (e: Throwable) {
            FluxLogger.e(LogCategory.SHIZUKU, "ShizukuManager", "Exception requesting Shizuku permission: ${e.message}")
        }
    }

    fun refreshState() {
        val installed = isInstalled()
        val running = isRunning()
        val hasPerm = hasPermission()
        val ver = getVersion()
        val uid = getUid()

        val status = when {
            !installed -> ShizukuStatus.UNAVAILABLE
            !running -> ShizukuStatus.NOT_RUNNING
            !hasPerm -> ShizukuStatus.PERMISSION_MISSING
            else -> ShizukuStatus.PERMISSION_GRANTED
        }

        val message = when (status) {
            ShizukuStatus.UNAVAILABLE -> "Shizuku is not installed. Install Shizuku app to enable privileged operations without root."
            ShizukuStatus.NOT_RUNNING -> "Shizuku is installed, but service is not running. Start Shizuku via Wireless Debugging or ADB."
            ShizukuStatus.PERMISSION_MISSING -> "Shizuku service is active, but FLUX has not been granted permission yet."
            ShizukuStatus.PERMISSION_GRANTED -> "Connected to Shizuku v$ver (UID: $uid). Privileged system operations unlocked."
            ShizukuStatus.PERMISSION_REVOKED -> "Shizuku permission was denied or revoked."
        }

        _state.value = ShizukuState(
            status = status,
            version = ver,
            uid = uid,
            isAvailable = (status == ShizukuStatus.PERMISSION_GRANTED),
            statusMessage = message
        )
    }

    suspend fun executePrivilegedOperation(command: String): ShizukuCommandOutput = withContext(Dispatchers.IO) {
        if (!hasPermission()) {
            FluxLogger.w(LogCategory.SHIZUKU, "ShizukuManager", "Attempted privileged execution without permission")
            return@withContext ShizukuCommandOutput(
                exitCode = -1,
                stdout = "",
                stderr = "Shizuku permission not granted"
            )
        }

        try {
            FluxLogger.d(LogCategory.SHIZUKU, "ShizukuManager", "Executing privileged command: $command")
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)

            val stdoutBuilder = StringBuilder()
            val stderrBuilder = StringBuilder()

            val outReader = BufferedReader(InputStreamReader(process.inputStream))
            val errReader = BufferedReader(InputStreamReader(process.errorStream))

            var line: String?
            while (outReader.readLine().also { line = it } != null) {
                stdoutBuilder.append(line).append("\n")
            }
            while (errReader.readLine().also { line = it } != null) {
                stderrBuilder.append(line).append("\n")
            }

            val exitCode = process.waitFor()
            val stdout = stdoutBuilder.toString().trim()
            val stderr = stderrBuilder.toString().trim()

            FluxLogger.d(
                LogCategory.SHIZUKU,
                "ShizukuManager",
                "Command finished with code=$exitCode, outLen=${stdout.length}, errLen=${stderr.length}"
            )

            ShizukuCommandOutput(exitCode, stdout, stderr)
        } catch (e: Throwable) {
            FluxLogger.e(LogCategory.SHIZUKU, "ShizukuManager", "Failed to execute Shizuku command: ${e.message}")
            ShizukuCommandOutput(
                exitCode = -1,
                stdout = "",
                stderr = e.message ?: "Unknown Shizuku execution error"
            )
        }
    }
}
