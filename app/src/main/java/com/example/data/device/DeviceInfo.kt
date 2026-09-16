package com.example.data.device

enum class RomFamily(val displayName: String) {
    HYPER_OS("Xiaomi HyperOS"),
    MIUI("Xiaomi MIUI"),
    ONE_UI("Samsung One UI"),
    PIXEL("Google Pixel Android"),
    OXYGEN_OS("OnePlus OxygenOS"),
    COLOR_OS("OPPO ColorOS"),
    REALME_UI("realme UI"),
    NOTHING_OS("Nothing OS"),
    MOTO_MYUX("Motorola MyUX"),
    ASUS_ROG("ASUS ROG / ZenUI"),
    AOSP("AOSP / Generic")
}

data class CpuCluster(
    val id: Int,
    val coreCount: Int,
    val minFreqKhz: Long,
    val maxFreqKhz: Long,
    val curFreqKhz: Long
)

data class DeviceInfo(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val device: String,
    val product: String,
    val hardware: String,
    val socModel: String,
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val kernelVersion: String,
    val romFamily: RomFamily,
    val romVersion: String,
    val isHyperOs: Boolean,
    val isPocoDevice: Boolean,
    val isQualcommSnapdragon: Boolean,
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val isLowRamDevice: Boolean,
    val screenWidthPx: Int,
    val screenHeightPx: Int,
    val screenDensityDpi: Int,
    val currentRefreshRate: Float,
    val supportedRefreshRates: List<Float>,
    val batteryPct: Int,
    val batteryTempCelsius: Float,
    val thermalStatus: String,
    val isRooted: Boolean,
    val cpuCores: Int,
    val cpuClusters: List<CpuCluster>,
    val gpuRenderer: String
)
