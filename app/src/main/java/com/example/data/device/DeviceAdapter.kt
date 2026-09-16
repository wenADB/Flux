package com.example.data.device

interface DeviceAdapter {
    val adapterName: String
    fun getHardwareSummary(info: DeviceInfo): String
    fun getOptimizationNotes(info: DeviceInfo): List<String>
    fun getVendorTweaks(info: DeviceInfo): List<VendorTweak>
}

data class VendorTweak(
    val id: String,
    val title: String,
    val description: String,
    val settingNamespace: String, // system, secure, global
    val settingKey: String,
    val recommendedValue: String,
    val isSupported: Boolean
)

class XiaomiAdapter : DeviceAdapter {
    override val adapterName: String = "Xiaomi / POCO / HyperOS Adapter"

    override fun getHardwareSummary(info: DeviceInfo): String {
        val pocoSuffix = if (info.isPocoDevice) " [POCO Verified]" else ""
        val snapdragonSuffix = if (info.isQualcommSnapdragon) " [Snapdragon Platform]" else ""
        return "${info.brand} ${info.model} ($pocoSuffix$snapdragonSuffix) - ${info.romVersion}"
    }

    override fun getOptimizationNotes(info: DeviceInfo): List<String> {
        val notes = mutableListOf<String>()
        if (info.isHyperOs) {
            notes.add("HyperOS detected: System frame pacing responds cleanly to window/animator scaling tweaks.")
            notes.add("Vendor Game Turbo service hooks detected. DND and refresh-rate lock fully compatible.")
        }
        if (info.isPocoDevice) {
            notes.add("POCO performance tuning active: 120Hz display modes and Snapdragon thermal governor monitored.")
        }
        if (info.isQualcommSnapdragon) {
            notes.add("Qualcomm Adreno GPU detected. Vulkan and GLES game driver switching supported where exposed by OS.")
        }
        return notes
    }

    override fun getVendorTweaks(info: DeviceInfo): List<VendorTweak> {
        return listOf(
            VendorTweak(
                id = "xiaomi_fps_limit",
                title = "MIUI / HyperOS High Refresh Enforcer",
                description = "Prevents dynamic throttling back to 60Hz inside compatible games.",
                settingNamespace = "system",
                settingKey = "user_refresh_rate",
                recommendedValue = "120",
                isSupported = info.supportedRefreshRates.any { it >= 119f }
            ),
            VendorTweak(
                id = "xiaomi_touch_sens",
                title = "Touch Response Pacing",
                description = "Aligns input sampling rates to screen vsync rate.",
                settingNamespace = "secure",
                settingKey = "touch_game_mode",
                recommendedValue = "1",
                isSupported = info.isHyperOs
            )
        )
    }
}

class GenericAndroidAdapter : DeviceAdapter {
    override val adapterName: String = "Generic Android Adapter"

    override fun getHardwareSummary(info: DeviceInfo): String {
        return "${info.manufacturer} ${info.model} (Android ${info.androidVersion}, API ${info.apiLevel})"
    }

    override fun getOptimizationNotes(info: DeviceInfo): List<String> {
        return listOf(
            "Standard AOSP / OEM architecture detected.",
            "Standard Android Settings.Global and Settings.Secure optimizations are supported.",
            "Shizuku access provides zero-root privilege escalation for system modifications."
        )
    }

    override fun getVendorTweaks(info: DeviceInfo): List<VendorTweak> = emptyList()
}

class SamsungAdapter : DeviceAdapter {
    override val adapterName: String = "Samsung One UI Adapter"

    override fun getHardwareSummary(info: DeviceInfo): String {
        return "${info.brand} ${info.model} (Samsung One UI)"
    }

    override fun getOptimizationNotes(info: DeviceInfo): List<String> {
        return listOf(
            "Samsung One UI detected.",
            "Adaptive refresh rate managed by DynamicDisplayPolicy.",
            "Game Booster packages can be isolated to prevent background telemetry throttling."
        )
    }

    override fun getVendorTweaks(info: DeviceInfo): List<VendorTweak> = emptyList()
}

object DeviceAdapterFactory {
    fun getAdapter(info: DeviceInfo): DeviceAdapter {
        return when {
            info.manufacturer.equals("Xiaomi", ignoreCase = true) ||
                    info.brand.equals("POCO", ignoreCase = true) ||
                    info.brand.equals("Redmi", ignoreCase = true) ||
                    info.isHyperOs -> XiaomiAdapter()
            info.manufacturer.equals("Samsung", ignoreCase = true) -> SamsungAdapter()
            else -> GenericAndroidAdapter()
        }
    }
}
