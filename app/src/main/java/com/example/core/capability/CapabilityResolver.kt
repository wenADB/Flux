package com.example.core.capability

import com.example.data.device.DeviceInfo
import com.example.data.shizuku.ShizukuManager

class CapabilityResolver(
    private val shizukuManager: ShizukuManager
) {

    fun resolveCapabilities(deviceInfo: DeviceInfo): List<CapabilityResult> {
        val hasShizuku = shizukuManager.hasPermission()
        val results = mutableListOf<CapabilityResult>()

        // 1. Display Refresh Rate
        val hasMultipleRates = deviceInfo.supportedRefreshRates.size > 1
        results.add(
            CapabilityResult(
                capability = Capability.DISPLAY_REFRESH_RATE,
                supported = hasMultipleRates,
                backend = if (hasShizuku) Backend.SHIZUKU else Backend.NATIVE_API,
                reason = if (hasMultipleRates) "Panel supports ${deviceInfo.supportedRefreshRates.joinToString { "${it}Hz" }}"
                else "Single static refresh rate panel detected (${deviceInfo.currentRefreshRate}Hz)",
                requiresPermission = !hasShizuku,
                permissionName = "Shizuku Privileged Access",
                risk = RiskLevel.LOW
            )
        )

        // 2. Animation Scale
        results.add(
            CapabilityResult(
                capability = Capability.ANIMATION_SCALE,
                supported = true,
                backend = if (hasShizuku) Backend.SHIZUKU else Backend.NATIVE_API,
                reason = "Settings.Global window and transition animation duration scales",
                requiresPermission = !hasShizuku,
                permissionName = "WRITE_SECURE_SETTINGS or Shizuku",
                risk = RiskLevel.SAFE
            )
        )

        // 3. Android Game Mode API
        val hasGameModeApi = deviceInfo.apiLevel >= 31
        results.add(
            CapabilityResult(
                capability = Capability.GAME_MODE,
                supported = hasGameModeApi,
                backend = if (hasShizuku && hasGameModeApi) Backend.SHIZUKU else Backend.UNSUPPORTED,
                reason = if (hasGameModeApi) "Android 12+ Game Mode service (cmd game) available"
                else "Requires Android 12+ (API 31). Current is API ${deviceInfo.apiLevel}",
                requiresPermission = !hasShizuku,
                permissionName = "Shizuku Privileged Access",
                risk = RiskLevel.LOW
            )
        )

        // 4. Thermal Monitoring
        results.add(
            CapabilityResult(
                capability = Capability.THERMAL_MONITORING,
                supported = true,
                backend = Backend.NATIVE_API,
                reason = "Native Android PowerManager and BatteryManager thermal sensors",
                requiresPermission = false,
                risk = RiskLevel.SAFE
            )
        )

        // 5. RAM & Memory Telemetry
        results.add(
            CapabilityResult(
                capability = Capability.RAM_MONITORING,
                supported = true,
                backend = Backend.NATIVE_API,
                reason = "ActivityManager.MemoryInfo native kernel memory telemetry",
                requiresPermission = false,
                risk = RiskLevel.SAFE
            )
        )

        // 6. CPU Telemetry
        results.add(
            CapabilityResult(
                capability = Capability.CPU_MONITORING,
                supported = true,
                backend = Backend.NATIVE_API,
                reason = "${deviceInfo.cpuCores} active cores; SoC: ${deviceInfo.socModel}",
                requiresPermission = false,
                risk = RiskLevel.SAFE
            )
        )

        // 7. GPU Telemetry
        results.add(
            CapabilityResult(
                capability = Capability.GPU_MONITORING,
                supported = true,
                backend = Backend.NATIVE_API,
                reason = "GPU renderer: ${deviceInfo.gpuRenderer}",
                requiresPermission = false,
                risk = RiskLevel.SAFE
            )
        )

        // 8. Frame Timing
        results.add(
            CapabilityResult(
                capability = Capability.FRAME_TIMING,
                supported = true,
                backend = Backend.NATIVE_API,
                reason = "Choreographer frame callback pipeline and vsync interval analysis",
                requiresPermission = false,
                risk = RiskLevel.SAFE
            )
        )

        // 9. ART Compilation
        results.add(
            CapabilityResult(
                capability = Capability.PACKAGE_COMPILE,
                supported = true,
                backend = if (hasShizuku) Backend.SHIZUKU else Backend.UNSUPPORTED,
                reason = if (hasShizuku) "dex2oat ahead-of-time compilation accessible via Shizuku"
                else "Requires Shizuku authorization to interact with 'cmd package compile'",
                requiresPermission = !hasShizuku,
                permissionName = "Shizuku Privileged Access",
                risk = RiskLevel.LOW
            )
        )

        // 10. Background App Restriction
        results.add(
            CapabilityResult(
                capability = Capability.BACKGROUND_RESTRICTION,
                supported = true,
                backend = if (hasShizuku) Backend.SHIZUKU else Backend.NATIVE_API,
                reason = "Package standby and background execution limits",
                requiresPermission = !hasShizuku,
                risk = RiskLevel.SAFE
            )
        )

        // 11. Network Monitoring
        results.add(
            CapabilityResult(
                capability = Capability.NETWORK_MONITORING,
                supported = true,
                backend = Backend.NATIVE_API,
                reason = "ConnectivityManager network telemetry and socket latency test",
                requiresPermission = false,
                risk = RiskLevel.SAFE
            )
        )

        return results
    }
}
