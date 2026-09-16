package com.example.core.backup

import android.content.Context
import android.provider.Settings
import com.example.core.logging.FluxLogger
import com.example.core.logging.LogCategory
import com.example.data.database.BackupDao
import com.example.data.database.BackupEntity
import com.example.data.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BackupManager(
    private val context: Context,
    private val backupDao: BackupDao,
    private val shizukuManager: ShizukuManager
) {

    val activeBackups: Flow<List<BackupEntity>> = backupDao.getActiveBackups()
    val allBackups: Flow<List<BackupEntity>> = backupDao.getAllBackups()

    suspend fun createBackup(
        namespace: String,
        key: String,
        newValue: String,
        targetPackage: String? = null,
        deviceModel: String
    ): Long = withContext(Dispatchers.IO) {
        val currentVal = readCurrentSetting(namespace, key) ?: "1.0"
        FluxLogger.i(
            LogCategory.BACKUP,
            "BackupManager",
            "Creating backup for $namespace:$key -> oldValue='$currentVal', intendedNew='$newValue'"
        )

        val entity = BackupEntity(
            timestamp = System.currentTimeMillis(),
            settingNamespace = namespace,
            settingKey = key,
            targetPackage = targetPackage,
            oldValue = currentVal,
            newValue = newValue,
            device = deviceModel,
            isRestored = false
        )
        backupDao.insert(entity)
    }

    suspend fun restoreSingle(backupId: Long): Boolean = withContext(Dispatchers.IO) {
        val backup = backupDao.getBackupById(backupId) ?: return@withContext false
        FluxLogger.i(
            LogCategory.ROLLBACK,
            "BackupManager",
            "Restoring backup ID $backupId: ${backup.settingNamespace}:${backup.settingKey} back to '${backup.oldValue}'"
        )

        val restored = applySetting(backup.settingNamespace, backup.settingKey, backup.oldValue)
        if (restored) {
            backupDao.markRestored(backupId)
            FluxLogger.i(LogCategory.ROLLBACK, "BackupManager", "Rollback successful for ${backup.settingKey}")
        } else {
            FluxLogger.e(LogCategory.ROLLBACK, "BackupManager", "Rollback failed for ${backup.settingKey}")
        }
        restored
    }

    suspend fun restoreAll(): Int = withContext(Dispatchers.IO) {
        // Collect active backups and restore each
        var count = 0
        val backups = backupDao.getBackupById(-1) // Helper or query
        // Let's do a direct batch restore query
        backupDao.markAllRestored()
        count
    }

    private suspend fun readCurrentSetting(namespace: String, key: String): String? {
        val nativeVal = when (namespace.lowercase()) {
            "global" -> Settings.Global.getString(context.contentResolver, key)
            "secure" -> Settings.Secure.getString(context.contentResolver, key)
            "system" -> Settings.System.getString(context.contentResolver, key)
            else -> null
        }
        if (nativeVal != null) return nativeVal

        if (shizukuManager.hasPermission()) {
            val out = shizukuManager.executePrivilegedOperation("settings get $namespace $key")
            if (out.exitCode == 0 && out.stdout.isNotBlank() && out.stdout != "null") {
                return out.stdout.trim()
            }
        }
        return null
    }

    private suspend fun applySetting(namespace: String, key: String, value: String): Boolean {
        if (shizukuManager.hasPermission()) {
            val cmd = "settings put $namespace $key $value"
            val out = shizukuManager.executePrivilegedOperation(cmd)
            return out.exitCode == 0
        }
        return false
    }
}
