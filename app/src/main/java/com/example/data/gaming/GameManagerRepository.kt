package com.example.data.gaming

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.core.executor.FluxExecutionEngine
import com.example.core.executor.OptimizationAction
import com.example.core.executor.OptimizationCategory
import com.example.core.logging.FluxLogger
import com.example.core.logging.LogCategory
import com.example.data.database.GameProfileDao
import com.example.data.database.GameProfileEntity
import com.example.data.device.DeviceInfo
import com.example.data.shizuku.ShizukuManager
import com.example.domain.OptimizationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class GameItem(
    val packageName: String,
    val appName: String,
    val profile: GameProfileEntity?
)

class GameManagerRepository(
    private val context: Context,
    private val gameProfileDao: GameProfileDao,
    private val shizukuManager: ShizukuManager,
    private val executionEngine: FluxExecutionEngine
) {

    val savedProfiles: Flow<List<GameProfileEntity>> = gameProfileDao.getAllProfiles()

    suspend fun getInstalledGames(): List<GameItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val games = mutableListOf<GameItem>()

        for (app in apps) {
            val isGame = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                app.category == ApplicationInfo.CATEGORY_GAME
            } else {
                (app.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }

            // Also check saved profiles to allow manually added games
            val existingProfile = gameProfileDao.getProfile(app.packageName)

            if (isGame || existingProfile != null) {
                val label = try {
                    pm.getApplicationLabel(app).toString()
                } catch (_: Exception) {
                    app.packageName
                }
                games.add(GameItem(app.packageName, label, existingProfile))
            }
        }
        games.sortedBy { it.appName.lowercase() }
    }

    suspend fun saveGameProfile(profile: GameProfileEntity) = withContext(Dispatchers.IO) {
        gameProfileDao.saveProfile(profile)
        FluxLogger.i(LogCategory.GENERAL, "GameManagerRepository", "Saved profile for ${profile.packageName} (${profile.profileName})")
    }

    suspend fun launchGameWithProfile(
        profile: GameProfileEntity,
        deviceInfo: DeviceInfo
    ): Boolean = withContext(Dispatchers.IO) {
        FluxLogger.i(LogCategory.EXECUTION, "GameManagerRepository", "Applying profile '${profile.profileName}' for ${profile.packageName}")

        // 1. Configure Android Game Mode if Android 12+ and Shizuku is authorized
        if (deviceInfo.apiLevel >= 31 && shizukuManager.hasPermission()) {
            val modeCode = when (profile.gameMode) {
                1 -> 2 // Performance
                2 -> 3 // Battery
                else -> 1 // Standard
            }
            shizukuManager.executePrivilegedOperation("cmd game set --mode $modeCode ${profile.packageName}")
        }

        // 2. Set Refresh Rate if 120Hz or 90Hz requested and supported
        if (profile.refreshRate >= 119f && deviceInfo.supportedRefreshRates.any { it >= 119f } && shizukuManager.hasPermission()) {
            shizukuManager.executePrivilegedOperation("settings put system user_refresh_rate 120 && settings put system min_refresh_rate 120.0")
        }

        // 3. Trim background memory if configured
        if (profile.forceStopBackgroundApps) {
            val trimAction = OptimizationRegistry.getAllActions(deviceInfo, shizukuManager.hasPermission())
                .firstOrNull { it.id == "kill_bg_apps" }
            if (trimAction != null) {
                executionEngine.executePipeline(trimAction, null, deviceInfo)
            }
        }

        // 4. Launch game activity
        return@withContext try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(profile.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                FluxLogger.w(LogCategory.GENERAL, "GameManagerRepository", "No launch intent found for ${profile.packageName}")
                false
            }
        } catch (e: Exception) {
            FluxLogger.e(LogCategory.GENERAL, "GameManagerRepository", "Failed to launch game: ${e.message}")
            false
        }
    }
}
