package com.example.exoplayerdemo.notify

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.exoplayerdemo.R

class AlarmBroadcast : BroadcastReceiver() {

    companion object {
        const val NOTIFY_ID = "NOTIFY_ID"
    }

    private lateinit var notificationManager: NotificationManager
    private var notifyId = -1

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            notifyId = it.getIntExtra(NOTIFY_ID, -1)
        }
        context?.let {
            createChannel(it)
            getNotificationManager(it)
            if (notifyId != -1)
                notificationManager.notify(notifyId, buildNotification(it))
        }
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(
                    TimerService.CHANNEL_ID,
                    "Timer",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            notificationChannel.setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                null
            )
            notificationChannel.setShowBadge(true)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun getNotificationManager(context: Context) {
        notificationManager = getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager
    }

    private fun buildNotification(context: Context): Notification {
        val title = "Sắp có lịch học"
        val content = "Học sinh ${notifyId + 1}"

        val intent = Intent(context, NotifyActivity::class.java)
        val pIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_ONE_SHOT)

        return NotificationCompat.Builder(context, TimerService.CHANNEL_ID)
            .setContentTitle(title)
            .setOngoing(true)
            .setContentText(content)
            .setColorized(true)
            .setColor(Color.parseColor("#BEAEE2"))
            .setSmallIcon(R.drawable.ic_clock)
            .setOnlyAlertOnce(false)
            .setContentIntent(pIntent)
            .setAutoCancel(true)
            .build()
    }
}