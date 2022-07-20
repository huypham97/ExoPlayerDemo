package com.example.exoplayerdemo.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.exoplayerdemo.R

class NotifyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notify)
        val button = findViewById<Button>(R.id.button)

//        val timerService = Intent(this, TimerService::class.java)
//        timerService.putExtra(TimerService.UPCOMING_CLASS_TIME, 20L)
//        startService(timerService)

        button.setOnClickListener {
            setAlarm(0, 6 * 1000)
            setAlarm(1, 10 * 1000)
        }

    }

    private fun setAlarm(id: Int, long: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmBroadcast::class.java)
        intent.putExtra(AlarmBroadcast.NOTIFY_ID, id)
        val pendingIntent =
            PendingIntent.getBroadcast(this, id, intent, 0)
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            SystemClock.elapsedRealtime() + long,
            pendingIntent
        )/*
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            SystemClock.elapsedRealtime() + 10000,
            pendingIntent
        )
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            SystemClock.elapsedRealtime() + 15000,
            pendingIntent
        )*/
    }
}