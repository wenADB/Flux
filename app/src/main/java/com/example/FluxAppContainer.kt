package com.example

import android.content.Context
import androidx.room.Room
import com.example.core.backup.BackupManager
import com.example.core.capability.CapabilityResolver
import com.example.core.executor.FluxExecutionEngine
import com.example.core.safety.SafetyManager
import com.example.core.verification.VerificationEngine
import com.example.data.art.ArtOptimizer
import com.example.data.database.FluxDatabase
import com.example.data.device.DeviceScanner
import com.example.data.gaming.GameManagerRepository
import com.example.data.network.NetworkRepository
import com.example.data.packages.PackageAnalyzer
import com.example.data.performance.PerformanceRepository
import com.example.data.shizuku.ShizukuManager
import com.example.data.thermal.ThermalManagerRepository
import com.example.domain.engine.SmartOptimizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class FluxAppContainer(val context: Context) {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: FluxDatabase by lazy {
        Room.databaseBuilder(
            context,
            FluxDatabase::class.java,
            "flux_optimizer.db"
        ).fallbackToDestructiveMigration().build()
    }

    val shizukuManager by lazy { ShizukuManager(context) }
    val deviceScanner by lazy { DeviceScanner(context) }
    val safetyManager by lazy { SafetyManager(shizukuManager) }
    val verificationEngine by lazy { VerificationEngine(context, shizukuManager) }

    val backupManager by lazy {
        BackupManager(context, database.backupDao(), shizukuManager)
    }

    val executionEngine by lazy {
        FluxExecutionEngine(
            context = context,
            shizukuManager = shizukuManager,
            safetyManager = safetyManager,
            backupManager = backupManager,
            verificationEngine = verificationEngine,
            historyDao = database.historyDao()
        )
    }

    val capabilityResolver by lazy { CapabilityResolver(shizukuManager) }
    val performanceRepository by lazy { PerformanceRepository(context, applicationScope) }
    val thermalRepository by lazy { ThermalManagerRepository(context) }
    val networkRepository by lazy { NetworkRepository(context) }
    val packageAnalyzer by lazy { PackageAnalyzer(context, shizukuManager) }
    val artOptimizer by lazy { ArtOptimizer(shizukuManager) }
    val smartOptimizer by lazy { SmartOptimizer(shizukuManager) }

    val gameManagerRepository by lazy {
        GameManagerRepository(
            context = context,
            gameProfileDao = database.gameProfileDao(),
            shizukuManager = shizukuManager,
            executionEngine = executionEngine
        )
    }
}
