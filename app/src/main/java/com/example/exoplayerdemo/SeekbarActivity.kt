package com.example.exoplayerdemo

import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.exoplayerdemo.R
import com.example.exoplayerdemo.databinding.ActivitySeekbarBinding
import com.example.exoplayerdemo.seekbar.IndicatorSeekBar
import com.example.exoplayerdemo.seekbar.OnSeekChangeListener
import com.example.exoplayerdemo.seekbar.SeekParams

class SeekbarActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeekbarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seekbar)
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_seekbar)
        this.binding.seekBar.setOnSeekChangeListener(object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams) {
                binding.tv.text = seekParams.tickText
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {
                //
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {
                //
            }

        })
    }
}