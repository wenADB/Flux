package com.example.data.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.view.Display
import android.view.WindowManager
import com.example.core.logging.FluxLogger
import com.example.core.logging.LogCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.util.Locale

class DeviceScanner(private val context: Context) {

    suspend fun scan(): DeviceInfo = withContext(Dispatchers.IO) {
        FluxLogger.i(LogCategory.SCAN, "DeviceScanner", "Beginning full hardware and software telemetry scan")

        val manufacturer = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
        val brand = Build.BRAND.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
        val model = Build.MODEL
        val device = Build.DEVICE
        val product = Build.PRODUCT
        val hardware = Build.HARDWARE
        val androidVer = Build.VERSION.RELEASE ?: "Unknown"
        val apiLevel = Build.VERSION.SDK_INT
        val securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH
        } else {
            "Unavailable"
        }

        val kernelVer = readKernelVersion()
        val socPlatform = getSystemProp("ro.soc.model", getSystemProp("ro.board.platform", hardware))
        val isQualcomm = socPlatform.contains("qcom", ignoreCase = true) ||
                socPlatform.contains("sm", ignoreCase = true) ||
                hardware.contains("qcom", ignoreCase = true) ||
                hardware.contains("bengal", ignoreCase = true) ||
                hardware.contains("lahaina", ignoreCase = true) ||
                device.contains("redwood", ignoreCase = true)

        val isPoco = brand.contains("poco", ignoreCase = true) || model.contains("poco", ignoreCase = true)

        val (romFamily, romVersion, isHyperOs) = detectRom(manufacturer, brand)

        // Memory telemetry
        val actMgr = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actMgr?.getMemoryInfo(memInfo)
        val totalRam = memInfo.totalMem
        val availRam = memInfo.availMem
        val isLowRam = actMgr?.isLowRamDevice ?: false

        // Display telemetry
        val (width, height, density, curHz, supportedHz) = getDisplayMetrics()

        // Battery & Thermal
        val (batteryPct, batteryTemp) = getBatteryTelemetry()
        val thermalStatus = getThermalStatus()

        // CPU cores and clusters
        val cpuCores = Runtime.getRuntime().availableProcessors()
        val cpuClusters = readCpuClusters(cpuCores)

        // Root check (purely diagnostic to confirm FLUX operates no-root)
        val isRooted = checkSuBinary()

        val deviceInfo = DeviceInfo(
            manufacturer = manufacturer,
            brand = brand,
            model = model,
            device = device,
            product = product,
            hardware = hardware,
            socModel = socPlatform,
            androidVersion = androidVer,
            apiLevel = apiLevel,
            securityPatch = securityPatch,
            kernelVersion = kernelVer,
            romFamily = romFamily,
            romVersion = romVersion,
            isHyperOs = isHyperOs,
            isPocoDevice = isPoco,
            isQualcommSnapdragon = isQualcomm,
            totalRamBytes = totalRam,
            availableRamBytes = availRam,
            isLowRamDevice = isLowRam,
            screenWidthPx = width,
            screenHeightPx = height,
            screenDensityDpi = density,
            currentRefreshRate = curHz,
            supportedRefreshRates = supportedHz,
            batteryPct = batteryPct,
            batteryTempCelsius = batteryTemp,
            thermalStatus = thermalStatus,
            isRooted = isRooted,
            cpuCores = cpuCores,
            cpuClusters = cpuClusters,
            gpuRenderer = getSystemProp("ro.hardware.egl", "Adreno (Qualcomm)")
        )

        FluxLogger.i(
            LogCategory.SCAN,
            "DeviceScanner",
            "Detected $manufacturer $model ($device), ROM=$romFamily $romVersion, API=$apiLevel, RAM=${totalRam / (1024 * 1024)}MB, Panel=${curHz}Hz"
        )

        deviceInfo
    }

    private fun detectRom(manufacturer: String, brand: String): Triple<RomFamily, String, Boolean> {
        val hyperOsProp = getSystemProp("ro.mi.os.version.name", "")
        val miuiVer = getSystemProp("ro.miui.ui.version.name", "")
        val oneUiVer = getSystemProp("ro.build.version.oneui", "")
        val oxygenVer = getSystemProp("ro.oxygen.version", "")
        val colorVer = getSystemProp("ro.build.version.opporom", "")

        val isXiaomiFamily = manufacturer.equals("Xiaomi", ignoreCase = true) ||
                brand.equals("POCO", ignoreCase = true) ||
                brand.equals("Redmi", ignoreCase = true)

        if (hyperOsProp.isNotEmpty() || (isXiaomiFamily && getSystemProp("ro.mi.os.version.incremental", "").isNotEmpty())) {
            val ver = if (hyperOsProp.isNotEmpty()) hyperOsProp else getSystemProp("ro.mi.os.version.incremental", "HyperOS")
            return Triple(RomFamily.HYPER_OS, ver, true)
        }

        if (miuiVer.isNotEmpty() || (isXiaomiFamily && getSystemProp("ro.miui.version.code_flag", "").isNotEmpty())) {
            val ver = if (miuiVer.isNotEmpty()) miuiVer else getSystemProp("ro.build.version.incremental", "MIUI")
            return Triple(RomFamily.MIUI, ver, false)
        }

        if (manufacturer.equals("Samsung", ignoreCase = true) || oneUiVer.isNotEmpty()) {
            return Triple(RomFamily.ONE_UI, if (oneUiVer.isNotEmpty()) oneUiVer else "One UI", false)
        }

        if (manufacturer.equals("Google", ignoreCase = true)) {
            return Triple(RomFamily.PIXEL, "Pixel Experience", false)
        }

        if (oxygenVer.isNotEmpty() || manufacturer.equals("OnePlus", ignoreCase = true)) {
            return Triple(RomFamily.OXYGEN_OS, if (oxygenVer.isNotEmpty()) oxygenVer else "OxygenOS", false)
        }

        if (colorVer.isNotEmpty() || manufacturer.equals("OPPO", ignoreCase = true)) {
            return Triple(RomFamily.COLOR_OS, if (colorVer.isNotEmpty()) colorVer else "ColorOS", false)
        }

        if (manufacturer.equals("realme", ignoreCase = true)) {
            return Triple(RomFamily.REALME_UI, "realme UI", false)
        }

        return Triple(RomFamily.AOSP, "Stock/AOSP", false)
    }

    private fun getDisplayMetrics(): DisplayTelemetry {
        return try {
            val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            val display = dm?.getDisplay(Display.DEFAULT_DISPLAY)

            var width = 1080
            var height = 2400
            var density = 440
            var curHz = 60f
            val supportedHz = mutableListOf<Float>()

            if (display != null) {
                curHz = display.refreshRate
                val mode = display.mode
                width = mode.physicalWidth
                height = mode.physicalHeight

                val modes = display.supportedModes
                for (m in modes) {
                    val rate = (m.refreshRate * 10).toInt() / 10f
                    if (!supportedHz.contains(rate)) {
                        supportedHz.add(rate)
                    }
                }
            }

            if (supportedHz.isEmpty()) {
                supportedHz.add(60f)
            }
            supportedHz.sort()

            density = context.resources.displayMetrics.densityDpi
            DisplayTelemetry(width, height, density, curHz, supportedHz)
        } catch (_: Exception) {
            DisplayTelemetry(1080, 2400, 440, 60f, listOf(60f, 120f))
        }
    }

    private fun getBatteryTelemetry(): Pair<Int, Float> {
        return try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val intent = context.registerReceiver(null, filter)
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val pct = if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).toInt() else 85
            val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 320
            val tempCelsius = tempTenths / 10.0f
            Pair(pct, tempCelsius)
        } catch (_: Exception) {
            Pair(100, 32.0f)
        }
    }

    private fun getThermalStatus(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            when (pm?.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> "NORMAL"
                PowerManager.THERMAL_STATUS_LIGHT -> "LIGHT"
                PowerManager.THERMAL_STATUS_MODERATE -> "MODERATE"
                PowerManager.THERMAL_STATUS_SEVERE -> "SEVERE"
                PowerManager.THERMAL_STATUS_CRITICAL -> "CRITICAL"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "EMERGENCY"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "SHUTDOWN"
                else -> "NORMAL"
            }
        } else {
            "NORMAL"
        }
    }

    private fun readKernelVersion(): String {
        return try {
            val file = File("/proc/version")
            if (file.canRead()) {
                val line = file.readText().trim()
                if (line.length > 50) line.substring(0, 50) + "..." else line
            } else {
                System.getProperty("os.version") ?: "Linux 5.x"
            }
        } catch (_: Exception) {
            System.getProperty("os.version") ?: "Linux 5.x"
        }
    }

    private fun readCpuClusters(coreCount: Int): List<CpuCluster> {
        val clusters = mutableListOf<CpuCluster>()
        try {
            var curFreq = 0L
            val curFreqFile = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            if (curFreqFile.canRead()) {
                curFreq = curFreqFile.readText().trim().toLongOrNull() ?: 0L
            }
            var maxFreq = 0L
            val maxFreqFile = File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
            if (maxFreqFile.canRead()) {
                maxFreq = maxFreqFile.readText().trim().toLongOrNull() ?: 0L
            }

            clusters.add(
                CpuCluster(
                    id = 0,
                    coreCount = coreCount,
                    minFreqKhz = 300000,
                    maxFreqKhz = if (maxFreq > 0) maxFreq else 2400000,
                    curFreqKhz = if (curFreq > 0) curFreq else 1800000
                )
            )
        } catch (_: Exception) {
            clusters.add(CpuCluster(0, coreCount, 300000, 2400000, 1800000))
        }
        return clusters
    }

    private fun checkSuBinary(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    private fun getSystemProp(key: String, defaultValue: String): String {
        return try {
            val c = Class.forName("android.os.SystemProperties")
            val get = c.getMethod("get", String::class.java, String::class.java)
            get.invoke(null, key, defaultValue) as? String ?: defaultValue
        } catch (_: Exception) {
            defaultValue
        }
    }

    private data class DisplayTelemetry(
        val width: Int,
        val height: Int,
        val density: Int,
        val curHz: Float,
        val supportedHz: List<Float>
    )
}
