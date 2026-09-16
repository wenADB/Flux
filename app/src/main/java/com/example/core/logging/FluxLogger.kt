package com.example.core.logging

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}

enum class LogCategory {
    SCAN, CAPABILITY, EXECUTION, VERIFICATION, THERMAL, SHIZUKU, BACKUP, ROLLBACK, GENERAL
}

data class LogEntry(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTime: String = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(timestamp)),
    val level: LogLevel,
    val category: LogCategory,
    val tag: String,
    val message: String
)

object FluxLogger {
    private const val MAX_LOGS = 300
    private val buffer = ConcurrentLinkedQueue<LogEntry>()
    private val _logsState = MutableStateFlow<List<LogEntry>>(emptyList())
    val logsState: StateFlow<List<LogEntry>> = _logsState.asStateFlow()

    fun log(level: LogLevel, category: LogCategory, tag: String, message: String) {
        // Sanitize any sensitive tokens/passwords just in case
        val sanitized = sanitize(message)
        val entry = LogEntry(level = level, category = category, tag = tag, message = sanitized)

        buffer.offer(entry)
        while (buffer.size > MAX_LOGS) {
            buffer.poll()
        }
        _logsState.value = buffer.toList().reversed()

        when (level) {
            LogLevel.DEBUG -> Log.d("FLUX-$tag", "[$category] $sanitized")
            LogLevel.INFO -> Log.i("FLUX-$tag", "[$category] $sanitized")
            LogLevel.WARN -> Log.w("FLUX-$tag", "[$category] $sanitized")
            LogLevel.ERROR -> Log.e("FLUX-$tag", "[$category] $sanitized")
        }
    }

    fun d(category: LogCategory, tag: String, message: String) = log(LogLevel.DEBUG, category, tag, message)
    fun i(category: LogCategory, tag: String, message: String) = log(LogLevel.INFO, category, tag, message)
    fun w(category: LogCategory, tag: String, message: String) = log(LogLevel.WARN, category, tag, message)
    fun e(category: LogCategory, tag: String, message: String) = log(LogLevel.ERROR, category, tag, message)

    fun clear() {
        buffer.clear()
        _logsState.value = emptyList()
    }

    private fun sanitize(input: String): String {
        return input.replace(Regex("(?i)(password|token|secret|auth|bearer)\\s*[:=]\\s*\\S+"), "$1=[REDACTED]")
    }
}
