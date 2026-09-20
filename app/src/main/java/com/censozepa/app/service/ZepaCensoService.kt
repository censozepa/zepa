package com.censozepa.app.service

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.censozepa.app.MainActivity
import kotlinx.coroutines.*
import java.util.Locale

class ZepaCensoService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var timerJob: Job? = null
    var elapsedSeconds = 0L
        private set

    companion object {
        const val CHANNEL_ID = "zepa_census_channel"
        const val NOTIFICATION_ID = 1001
        var activeZepaId: String? = null
            private set
        var instance: ZepaCensoService? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "STOP_SERVICE") {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val zepaId = intent?.getStringExtra("ZEPA_ID") ?: ""
        val zepaName = intent?.getStringExtra("ZEPA_NAME") ?: "ZEPA"
        activeZepaId = zepaId
        
        startForeground(NOTIFICATION_ID, createNotification(zepaName, "00:00"))
        
        if (timerJob == null || !timerJob!!.isActive) {
            elapsedSeconds = 0L
            timerJob = serviceScope.launch {
                while (isActive) {
                    delay(1000L)
                    elapsedSeconds++
                    val mins = elapsedSeconds / 60
                    val secs = elapsedSeconds % 60
                    val timeStr = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
                    updateNotification(zepaName, timeStr)
                }
            }
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Censo ZEPA en Curso",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene el temporizador y el seguimiento activo en segundo plano"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(zepaName: String, timeStr: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("NAVIGATE_TO_ZEPA_ID", activeZepaId)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Censo Activo: $zepaName")
            .setContentText("Tiempo transcurrido: $timeStr")
            .setSmallIcon(R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(zepaName: String, timeStr: String) {
        val notification = createNotification(zepaName, timeStr)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
        instance = null
        activeZepaId = null
    }
}
