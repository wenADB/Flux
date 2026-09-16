package com.example.data.packages

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import com.example.core.logging.FluxLogger
import com.example.core.logging.LogCategory
import com.example.core.safety.PackageCategory
import com.example.core.safety.ProtectedPackages
import com.example.data.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PackageItem(
    val packageName: String,
    val appLabel: String,
    val uid: Int,
    val isSystem: Boolean,
    val isEnabled: Boolean,
    val isGame: Boolean,
    val category: PackageCategory,
    val isProtected: Boolean
)

class PackageAnalyzer(
    private val context: Context,
    private val shizukuManager: ShizukuManager
) {

    suspend fun getInstalledPackages(): List<PackageItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val result = mutableListOf<PackageItem>()

        for (app in installed) {
            val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isGame = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                app.category == ApplicationInfo.CATEGORY_GAME
            } else {
                (app.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }

            val label = try {
                pm.getApplicationLabel(app).toString()
            } catch (_: Exception) {
                app.packageName
            }

            val category = ProtectedPackages.classifyPackage(app.packageName, isSystem, isGame)
            val isProtected = ProtectedPackages.isProtected(app.packageName)

            result.add(
                PackageItem(
                    packageName = app.packageName,
                    appLabel = label,
                    uid = app.uid,
                    isSystem = isSystem,
                    isEnabled = app.enabled,
                    isGame = isGame,
                    category = category,
                    isProtected = isProtected
                )
            )
        }

        // Sort games and user apps first, then OEM optional, then system
        result.sortedWith(
            compareBy<PackageItem> {
                when (it.category) {
                    PackageCategory.GAME -> 0
                    PackageCategory.USER_APP -> 1
                    PackageCategory.OEM_OPTIONAL -> 2
                    else -> 3
                }
            }.thenBy { it.appLabel.lowercase() }
        )
    }

    fun openAppInfo(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            FluxLogger.e(LogCategory.GENERAL, "PackageAnalyzer", "Failed to open App Info: ${e.message}")
        }
    }

    suspend fun forceStopPackage(packageName: String): Boolean = withContext(Dispatchers.IO) {
        if (ProtectedPackages.isProtected(packageName)) {
            FluxLogger.w(LogCategory.SAFETY, "PackageAnalyzer", "Cannot force-stop protected package: $packageName")
            return@withContext false
        }

        if (shizukuManager.hasPermission()) {
            val cmd = "am force-stop $packageName"
            val out = shizukuManager.executePrivilegedOperation(cmd)
            FluxLogger.i(LogCategory.EXECUTION, "PackageAnalyzer", "Force stopped $packageName via Shizuku (exit=${out.exitCode})")
            return@withContext out.exitCode == 0
        } else {
            // Fallback to native ActivityManager
            val actMgr = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            actMgr?.killBackgroundProcesses(packageName)
            return@withContext true
        }
    }

    suspend fun togglePackageState(packageName: String, disable: Boolean): Boolean = withContext(Dispatchers.IO) {
        if (ProtectedPackages.isProtected(packageName)) {
            FluxLogger.w(LogCategory.SAFETY, "PackageAnalyzer", "Cannot disable protected package: $packageName")
            return@withContext false
        }

        if (!shizukuManager.hasPermission()) {
            FluxLogger.w(LogCategory.SAFETY, "PackageAnalyzer", "Shizuku authorization required to modify package state")
            return@withContext false
        }

        val cmd = if (disable) "pm disable-user --user 0 $packageName" else "pm enable $packageName"
        val out = shizukuManager.executePrivilegedOperation(cmd)
        FluxLogger.i(LogCategory.EXECUTION, "PackageAnalyzer", "Toggle package $packageName (disable=$disable) exit=${out.exitCode}")
        return@withContext out.exitCode == 0
    }
}
