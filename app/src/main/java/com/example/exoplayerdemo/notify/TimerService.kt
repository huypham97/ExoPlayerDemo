package com.example.exoplayerdemo.notify

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.exoplayerdemo.R
import java.util.*

class TimerService : Service() {

    companion object {
        const val CHANNEL_ID = "Timer_Notifications"
        const val UPCOMING_CLASS_TIME = "UPCOMING_CLASS_TIME"
    }

    private lateinit var notificationManager: NotificationManager
    private var startTimer = Timer()
    private var updateTimer = Timer()
    private var timeElapsed: Int = 0
    private var isFirstTime = true

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()
        getNotificationManager()

        val upcomingTime = intent?.getLongExtra(UPCOMING_CLASS_TIME, 0)!!

        if (isFirstTime) {
            handleForeground()
            isFirstTime = false
        }

        return START_STICKY
    }

    private fun startTimer() {
        startTimer = Timer()
        startTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                timeElapsed = Calendar.getInstance().timeInMillis.toInt()
            }
        }, 0, 1000)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(CHANNEL_ID, "Timer", NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                null
            )
            notificationChannel.setShowBadge(true)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun getNotificationManager() {
        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
    }

    private fun handleForeground() {
        startForeground(1, buildNotification())

        updateTimer = Timer()

        updateTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                timeElapsed++
                updateNotification()
            }
        }, 0, 1000)
    }

    private fun buildNotification(): Notification {
        val title = "Sắp có lịch học"
        val content = "$timeElapsed"

        val intent = Intent(this, NotifyActivity::class.java)
        val pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setOngoing(true)
            .setContentText(content)
            .setColorized(true)
            .setColor(Color.parseColor("#BEAEE2"))
            .setSmallIcon(R.drawable.ic_clock)
            .setOnlyAlertOnce(true)
            .setContentIntent(pIntent)
            .setAutoCancel(true)
            .build()
    }

    private fun updateNotification() {
        notificationManager.notify(1, buildNotification())
    }
}

enum class Type {
    FIRST, SECOND, THIRD
}