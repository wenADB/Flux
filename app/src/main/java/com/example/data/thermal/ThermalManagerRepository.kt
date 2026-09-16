package com.example.data.thermal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import com.example.core.logging.FluxLogger
import com.example.core.logging.LogCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ThermalSample(
    val timestamp: Long,
    val tempCelsius: Float,
    val status: String
)

data class ThermalTelemetry(
    val temperatureCelsius: Float,
    val thermalStatus: String,
    val batteryPct: Int,
    val isCharging: Boolean,
    val voltageMv: Int,
    val isThermallyConstrained: Boolean,
    val statusDescription: String,
    val history: List<ThermalSample>
)

class ThermalManagerRepository(private val context: Context) {

    private val _telemetry = MutableStateFlow(
        ThermalTelemetry(
            temperatureCelsius = 32.0f,
            thermalStatus = "NORMAL",
            batteryPct = 100,
            isCharging = false,
            voltageMv = 4200,
            isThermallyConstrained = false,
            statusDescription = "Optimal thermal condition. No throttling active.",
            history = emptyList()
        )
    )
    val telemetry: StateFlow<ThermalTelemetry> = _telemetry.asStateFlow()

    private val historySamples = mutableListOf<ThermalSample>()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                updateTelemetry(intent)
            }
        }
    }

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialIntent = context.registerReceiver(batteryReceiver, filter)
        initialIntent?.let { updateTelemetry(it) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.addThermalStatusListener { status ->
                FluxLogger.i(LogCategory.THERMAL, "ThermalManagerRepository", "Thermal status changed to $status")
                // Re-evaluate
                val cur = _telemetry.value
                val statusStr = mapThermalStatus(status)
                _telemetry.value = cur.copy(
                    thermalStatus = statusStr,
                    isThermallyConstrained = (status >= PowerManager.THERMAL_STATUS_MODERATE)
                )
            }
        }
    }

    private fun updateTelemetry(intent: Intent) {
        val rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 320)
        val tempCelsius = rawTemp / 10.0f
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val pct = if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).toInt() else 85
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4000)

        val thermalStatusStr = getCurrentThermalStatus()
        val isConstrained = (tempCelsius >= 42.0f) || (thermalStatusStr in listOf("MODERATE", "SEVERE", "CRITICAL", "EMERGENCY"))

        val desc = when (thermalStatusStr) {
            "NORMAL" -> "Safe operating temperature. SoC running at nominal clock rates."
            "LIGHT" -> "Light thermal load. Standard performance maintained."
            "MODERATE" -> "Moderate thermal load. Ambient panel or sustained GPU throttling may occur."
            "SEVERE" -> "High thermal load! CPU/GPU clocks stepped down to protect hardware."
            "CRITICAL" -> "Critical thermal alert! Performance throttled aggressively to prevent overheating."
            else -> "Thermal monitoring active via battery sensors."
        }

        val sample = ThermalSample(System.currentTimeMillis(), tempCelsius, thermalStatusStr)
        synchronized(historySamples) {
            historySamples.add(sample)
            if (historySamples.size > 40) {
                historySamples.removeAt(0)
            }
        }

        _telemetry.value = ThermalTelemetry(
            temperatureCelsius = tempCelsius,
            thermalStatus = thermalStatusStr,
            batteryPct = pct,
            isCharging = isCharging,
            voltageMv = voltage,
            isThermallyConstrained = isConstrained,
            statusDescription = desc,
            history = synchronized(historySamples) { historySamples.toList() }
        )
    }

    private fun getCurrentThermalStatus(): String {
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

    private fun mapThermalStatus(status: Int): String {
        return when (status) {
            PowerManager.THERMAL_STATUS_NONE -> "NORMAL"
            PowerManager.THERMAL_STATUS_LIGHT -> "LIGHT"
            PowerManager.THERMAL_STATUS_MODERATE -> "MODERATE"
            PowerManager.THERMAL_STATUS_SEVERE -> "SEVERE"
            PowerManager.THERMAL_STATUS_CRITICAL -> "CRITICAL"
            PowerManager.THERMAL_STATUS_EMERGENCY -> "EMERGENCY"
            PowerManager.THERMAL_STATUS_SHUTDOWN -> "SHUTDOWN"
            else -> "NORMAL"
        }
    }
}
