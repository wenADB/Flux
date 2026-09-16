package com.example.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.FluxApplication
import com.example.core.capability.CapabilityResult
import com.example.core.executor.OptimizationAction
import com.example.core.executor.OptimizationResult
import com.example.core.logging.FluxLogger
import com.example.core.logging.LogCategory
import com.example.core.logging.LogEntry
import com.example.data.art.ArtCompilationResult
import com.example.data.database.BackupEntity
import com.example.data.database.GameProfileEntity
import com.example.data.database.OptimizationHistoryEntity
import com.example.data.device.DeviceInfo
import com.example.data.gaming.GameItem
import com.example.data.network.NetworkTelemetry
import com.example.data.packages.PackageItem
import com.example.data.performance.PerformanceTelemetry
import com.example.data.shizuku.ShizukuState
import com.example.data.thermal.ThermalTelemetry
import com.example.domain.OptimizationRegistry
import com.example.domain.engine.SmartOptimizer
import com.example.domain.engine.SmartRecommendation
import com.example.domain.engine.SystemHealthScore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ExecutionUiPhase {
    object Idle : ExecutionUiPhase()
    data class Scanning(val message: String) : ExecutionUiPhase()
    data class Preparing(val actionTitle: String) : ExecutionUiPhase()
    data class Applying(val actionTitle: String) : ExecutionUiPhase()
    data class Verifying(val actionTitle: String) : ExecutionUiPhase()
    data class Completed(val result: OptimizationResult) : ExecutionUiPhase()
}

class FluxViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as FluxApplication).container

    val shizukuState: StateFlow<ShizukuState> = container.shizukuManager.state
    val performanceTelemetry: StateFlow<PerformanceTelemetry> = container.performanceRepository.telemetry
    val thermalTelemetry: StateFlow<ThermalTelemetry> = container.thermalRepository.telemetry
    val logs: StateFlow<List<LogEntry>> = FluxLogger.logsState

    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    val deviceInfo: StateFlow<DeviceInfo?> = _deviceInfo.asStateFlow()

    private val _capabilities = MutableStateFlow<List<CapabilityResult>>(emptyList())
    val capabilities: StateFlow<List<CapabilityResult>> = _capabilities.asStateFlow()

    private val _recommendations = MutableStateFlow<List<SmartRecommendation>>(emptyList())
    val recommendations: StateFlow<List<SmartRecommendation>> = _recommendations.asStateFlow()

    private val _healthScore = MutableStateFlow<SystemHealthScore?>(null)
    val healthScore: StateFlow<SystemHealthScore?> = _healthScore.asStateFlow()

    private val _networkTelemetry = MutableStateFlow<NetworkTelemetry?>(null)
    val networkTelemetry: StateFlow<NetworkTelemetry?> = _networkTelemetry.asStateFlow()

    private val _installedPackages = MutableStateFlow<List<PackageItem>>(emptyList())
    val installedPackages: StateFlow<List<PackageItem>> = _installedPackages.asStateFlow()

    private val _gamesList = MutableStateFlow<List<GameItem>>(emptyList())
    val gamesList: StateFlow<List<GameItem>> = _gamesList.asStateFlow()

    private val _executionPhase = MutableStateFlow<ExecutionUiPhase>(ExecutionUiPhase.Idle)
    val executionPhase: StateFlow<ExecutionUiPhase> = _executionPhase.asStateFlow()

    private val _artResult = MutableStateFlow<ArtCompilationResult?>(null)
    val artResult: StateFlow<ArtCompilationResult?> = _artResult.asStateFlow()

    val historyEntries: StateFlow<List<OptimizationHistoryEntity>> =
        container.database.historyDao().getAllHistory()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBackups: StateFlow<List<BackupEntity>> =
        container.backupManager.activeBackups
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        scanDevice()
        loadPackagesAndGames()
        refreshNetwork()
    }

    fun scanDevice() {
        viewModelScope.launch {
            _executionPhase.value = ExecutionUiPhase.Scanning("Scanning hardware and system properties...")
            val info = container.deviceScanner.scan()
            _deviceInfo.value = info

            container.shizukuManager.refreshState()
            val caps = container.capabilityResolver.resolveCapabilities(info)
            _capabilities.value = caps

            updateRecommendationsAndScore(info)
            _executionPhase.value = ExecutionUiPhase.Idle
        }
    }

    fun requestShizukuPermission() {
        container.shizukuManager.requestPermission()
    }

    fun refreshNetwork() {
        viewModelScope.launch {
            _networkTelemetry.value = container.networkRepository.getNetworkTelemetry()
        }
    }

    fun loadPackagesAndGames() {
        viewModelScope.launch {
            val packages = container.packageAnalyzer.getInstalledPackages()
            _installedPackages.value = packages
            val games = container.gameManagerRepository.getInstalledGames()
            _gamesList.value = games
        }
    }

    fun executeAction(action: OptimizationAction, customValue: String? = null) {
        val info = _deviceInfo.value ?: return
        viewModelScope.launch {
            _executionPhase.value = ExecutionUiPhase.Preparing(action.title)
            kotlinx.coroutines.delay(200)

            _executionPhase.value = ExecutionUiPhase.Applying(action.title)
            val result = container.executionEngine.executePipeline(action, customValue, info)

            _executionPhase.value = ExecutionUiPhase.Verifying(action.title)
            kotlinx.coroutines.delay(250)

            _executionPhase.value = ExecutionUiPhase.Completed(result)

            // Refresh device info and recommendations
            scanDevice()
        }
    }

    fun dismissExecutionResult() {
        _executionPhase.value = ExecutionUiPhase.Idle
    }

    fun restoreBackup(backupId: Long) {
        viewModelScope.launch {
            _executionPhase.value = ExecutionUiPhase.Applying("Restoring previous state...")
            container.backupManager.restoreSingle(backupId)
            scanDevice()
            _executionPhase.value = ExecutionUiPhase.Idle
        }
    }

    fun forceStopApp(packageName: String) {
        viewModelScope.launch {
            container.packageAnalyzer.forceStopPackage(packageName)
            loadPackagesAndGames()
        }
    }

    fun compileApp(packageName: String, mode: String) {
        viewModelScope.launch {
            _executionPhase.value = ExecutionUiPhase.Applying("Running dex2oat compilation ($mode)...")
            val res = container.artOptimizer.compilePackage(packageName, mode)
            _artResult.value = res
            _executionPhase.value = ExecutionUiPhase.Idle
        }
    }

    fun resetAppCompilation(packageName: String) {
        viewModelScope.launch {
            val res = container.artOptimizer.resetCompilation(packageName)
            _artResult.value = res
        }
    }

    fun saveGameProfile(profile: GameProfileEntity) {
        viewModelScope.launch {
            container.gameManagerRepository.saveGameProfile(profile)
            loadPackagesAndGames()
        }
    }

    fun launchGame(profile: GameProfileEntity) {
        val info = _deviceInfo.value ?: return
        viewModelScope.launch {
            container.gameManagerRepository.launchGameWithProfile(profile, info)
        }
    }

    fun clearDebugLogs() {
        FluxLogger.clear()
    }

    private fun updateRecommendationsAndScore(info: DeviceInfo) {
        val perf = container.performanceRepository.telemetry.value
        val thermal = container.thermalRepository.telemetry.value

        val score = container.smartOptimizer.calculateHealthScore(info, perf, thermal)
        _healthScore.value = score

        val recs = container.smartOptimizer.generateRecommendations(info, perf, thermal)
        _recommendations.value = recs
    }
}
