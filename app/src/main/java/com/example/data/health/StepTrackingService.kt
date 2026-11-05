package com.example.data.health

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class StepTrackingService : Service() {

    private lateinit var stepSensorManager: StepSensorManager
    private val notificationId = 1001
    private val channelId = "tether_step_channel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        stepSensorManager = StepSensorManager(applicationContext)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    notificationId,
                    buildNotification(stepSensorManager.loadTodaySteps()),
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                )
            } else {
                startForeground(notificationId, buildNotification(stepSensorManager.loadTodaySteps()))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stepSensorManager.registerListener()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val currentSteps = stepSensorManager.loadTodaySteps()
        updateNotification(currentSteps)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stepSensorManager.unregisterListener()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Шагомер Tether",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Фоновый подсчёт шагов в течение дня"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(steps: Int): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val text = "Сегодня: %,d шагов".format(steps)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Шагомер Tether")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(steps: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(notificationId, buildNotification(steps))
    }
}

