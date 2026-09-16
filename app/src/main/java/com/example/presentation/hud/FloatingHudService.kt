package com.example.presentation.hud

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.FluxApplication
import com.example.core.logging.FluxLogger
import com.example.core.logging.LogCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FloatingHudService : Service() {

    private var windowManager: WindowManager? = null
    private var hudView: View? = null
    private var updateJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        setupFloatingWindow()
    }

    private fun startForegroundServiceNotification() {
        val channelId = "flux_hud_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "FLUX Floating HUD",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("FLUX Gaming Telemetry HUD")
            .setContentText("Real-time FPS, frame timing, and thermal overlay active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        startForeground(2001, notification)
    }

    private fun setupFloatingWindow() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 180
        }

        // Programmatic HUD card layout
        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.VERTICAL
            setPadding(28, 20, 28, 20)
            setBackgroundColor(android.graphics.Color.argb(220, 8, 11, 16))
            elevation = 16f
        }

        val title = TextView(this).apply {
            text = "FLUX HUD"
            setTextColor(android.graphics.Color.parseColor("#00E5FF"))
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        val metricsView = TextView(this).apply {
            text = "FPS: --\nCPU: --  RAM: --\nTEMP: --"
            setTextColor(android.graphics.Color.parseColor("#F0F4FC"))
            textSize = 11f
        }

        container.addView(title)
        container.addView(metricsView)

        // Drag to move
        container.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(container, params)
                        return true
                    }
                }
                return false
            }
        })

        hudView = container
        try {
            windowManager?.addView(container, params)
            FluxLogger.i(LogCategory.GENERAL, "FloatingHudService", "Floating HUD overlay window attached")
        } catch (e: Exception) {
            FluxLogger.e(LogCategory.GENERAL, "FloatingHudService", "Failed to add HUD overlay view: ${e.message}")
        }

        // Live telemetry updates
        val app = application as? FluxApplication
        val perfRepo = app?.container?.performanceRepository
        val thermalRepo = app?.container?.thermalRepository

        updateJob = serviceScope.launch {
            while (isActive) {
                val perf = perfRepo?.telemetry?.value
                val thermal = thermalRepo?.telemetry?.value

                val fps = perf?.frameMetrics?.fpsDisplay ?: "Active"
                val p90 = perf?.frameMetrics?.p90FrameTimeMs?.let { String.format("%.1fms", it) } ?: "16.6ms"
                val cpu = perf?.cpuUsagePct ?: 0
                val ram = perf?.ramUsagePct ?: 0
                val temp = thermal?.temperatureCelsius?.let { String.format("%.1f°C", it) } ?: "--"

                metricsView.text = "$fps (p90: $p90)\nCPU: $cpu%  RAM: $ram%\nBAT: ${thermal?.batteryPct ?: 0}%  $temp"

                delay(900)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
        if (hudView != null) {
            try {
                windowManager?.removeView(hudView)
            } catch (_: Exception) {}
            hudView = null
        }
        FluxLogger.i(LogCategory.GENERAL, "FloatingHudService", "Floating HUD overlay service destroyed")
    }
}
