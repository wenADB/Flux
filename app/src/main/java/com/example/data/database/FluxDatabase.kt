package com.example.data.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "optimization_history")
data class OptimizationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val device: String,
    val actionId: String,
    val actionTitle: String,
    val category: String,
    val oldValue: String?,
    val newValue: String?,
    val result: String, // SUCCESS, FAILED, UNSUPPORTED, ROLLED_BACK
    val verified: Boolean,
    val verificationMessage: String,
    val errorMessage: String? = null
)

@Entity(tableName = "backups")
data class BackupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val settingNamespace: String, // global, secure, system, package
    val settingKey: String,
    val targetPackage: String? = null,
    val oldValue: String,
    val newValue: String,
    val device: String,
    val isRestored: Boolean = false
)

@Entity(tableName = "game_profiles")
data class GameProfileEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val profileName: String = "Performance",
    val refreshRate: Float = 120f,
    val gameMode: Int = 1, // 1 = Performance, 2 = Battery, 0 = Standard
    val enableDnd: Boolean = true,
    val lockBrightness: Boolean = false,
    val brightnessLevel: Float = 0.8f,
    val screenTimeoutSeconds: Int = 300,
    val forceStopBackgroundApps: Boolean = false
)

@Dao
interface OptimizationHistoryDao {
    @Query("SELECT * FROM optimization_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<OptimizationHistoryEntity>>

    @Insert
    suspend fun insert(entry: OptimizationHistoryEntity): Long

    @Query("DELETE FROM optimization_history")
    suspend fun clearAll()
}

@Dao
interface BackupDao {
    @Query("SELECT * FROM backups WHERE isRestored = 0 ORDER BY timestamp DESC")
    fun getActiveBackups(): Flow<List<BackupEntity>>

    @Query("SELECT * FROM backups ORDER BY timestamp DESC")
    fun getAllBackups(): Flow<List<BackupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(backup: BackupEntity): Long

    @Update
    suspend fun update(backup: BackupEntity)

    @Query("UPDATE backups SET isRestored = 1 WHERE id = :id")
    suspend fun markRestored(id: Long)

    @Query("UPDATE backups SET isRestored = 1")
    suspend fun markAllRestored()

    @Query("SELECT * FROM backups WHERE id = :id LIMIT 1")
    suspend fun getBackupById(id: Long): BackupEntity?
}

@Dao
interface GameProfileDao {
    @Query("SELECT * FROM game_profiles")
    fun getAllProfiles(): Flow<List<GameProfileEntity>>

    @Query("SELECT * FROM game_profiles WHERE packageName = :pkg LIMIT 1")
    suspend fun getProfile(pkg: String): GameProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: GameProfileEntity)

    @Query("DELETE FROM game_profiles WHERE packageName = :pkg")
    suspend fun deleteProfile(pkg: String)
}

@Database(
    entities = [
        OptimizationHistoryEntity::class,
        BackupEntity::class,
        GameProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FluxDatabase : RoomDatabase() {
    abstract fun historyDao(): OptimizationHistoryDao
    abstract fun backupDao(): BackupDao
    abstract fun gameProfileDao(): GameProfileDao
}
