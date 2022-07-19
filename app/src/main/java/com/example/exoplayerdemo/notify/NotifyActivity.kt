package com.example.exoplayerdemo.notify

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.exoplayerdemo.R

class NotifyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notify)

        val timerService = Intent(this, TimerService::class.java)
        timerService.putExtra(TimerService.UPCOMING_CLASS_TIME, 100L)
        startService(timerService)
    }
}