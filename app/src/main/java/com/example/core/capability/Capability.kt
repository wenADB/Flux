package com.example.core.capability

enum class Capability(
    val title: String,
    val description: String
) {
    DISPLAY_REFRESH_RATE("Display Refresh Rate", "Dynamic configuration of panel refresh rate (60Hz, 90Hz, 120Hz)"),
    DISPLAY_RESOLUTION("Display Resolution", "Physical rendering resolution inspection and modes"),
    DISPLAY_DENSITY("Display Density", "Screen DPI scale factor adjustment"),
    ANIMATION_SCALE("UI Animation Scales", "Window, transition, and animator duration scaling"),
    GAME_MODE("Game Mode API", "Standard Android Game Mode configuration (Performance/Battery)"),
    GAME_DRIVER("Game Graphic Driver", "Per-app graphics driver preference (System/Game Driver)"),
    APP_STANDBY("App Standby Buckets", "Inspection and management of app standby buckets"),
    DOZE("Doze / Idle State", "System deep sleep and battery optimization exceptions"),
    BATTERY_OPTIMIZATION("Battery Optimization Policy", "Per-package battery saver whitelist and restriction"),
    BACKGROUND_RESTRICTION("Background Execution Limits", "Control over background service and broadcast limits"),
    PACKAGE_DISABLE("Package State Management", "Disabling or freezing non-critical user and OEM packages"),
    PACKAGE_SUSPEND("Package Suspension", "Temporarily suspending app execution without uninstalling"),
    PACKAGE_CLEAR_CACHE("Package Cache Cleanup", "Reclaiming temporary app caches safely"),
    PACKAGE_COMPILE("ART Ahead-Of-Time Compilation", "Dex2oat compilation modes (speed-profile, speed, verify)"),
    APPOPS("AppOps Privilege Control", "Granular runtime operation permissions for packages"),
    PERMISSIONS("Runtime Permissions", "System permission grants and revokes"),
    THERMAL_MONITORING("Thermal Telemetry", "Hardware thermal status, headroom and battery sensor readings"),
    CPU_MONITORING("CPU Telemetry", "Core cluster frequencies, scaling governor, and load inspection"),
    RAM_MONITORING("RAM Telemetry", "Memory pressure, active usage, cache, and swap/zRAM inspection"),
    GPU_MONITORING("GPU Telemetry", "Renderer information, driver version, and load indicators"),
    FRAME_TIMING("Frame Timing & Jank Analysis", "Choreographer frame intervals, vsync pacing, and dropped frames"),
    NETWORK_MONITORING("Network Quality Telemetry", "Link speed, cellular/wifi latency, and connectivity state"),
    DND("Do Not Disturb", "Suppression of notifications and interruptions during gaming"),
    SCREEN_TIMEOUT("Display Timeout Override", "Adjusting screen turn-off timeout for game sessions"),
    BRIGHTNESS("Brightness Locking", "Locking brightness levels to prevent ambient sensor dimming"),
    POWER_STATE("Power State Telemetry", "Battery voltage, current, temperature, and charging technology")
}

data class CapabilityResult(
    val capability: Capability,
    val supported: Boolean,
    val backend: Backend,
    val reason: String?,
    val requiresPermission: Boolean = false,
    val permissionName: String? = null,
    val reversible: Boolean = true,
    val risk: RiskLevel = RiskLevel.SAFE
)
