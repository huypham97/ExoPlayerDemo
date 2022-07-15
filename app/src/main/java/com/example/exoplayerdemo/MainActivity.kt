package com.example.exoplayerdemo

import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.Player

class MainActivity : AppCompatActivity(), Player.Listener {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var videoPlayerView: VideoPlayerView? = null
    private var btnBack: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        applyWindow(window)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        videoPlayerView = findViewById(R.id.video_player_view)
        btnBack = findViewById(R.id.iv_back)

        videoPlayerView!!.setData(
            listOf(
                getString(R.string.media_url_mp4),
                getString(R.string.media_url_mp4)
            )
        )

        this.btnBack?.setOnClickListener {
            this.videoPlayerView?.stop()
        }
    }

    override fun onResume() {
        super.onResume()
        videoPlayerView?.start()
    }

    override fun onPause() {
        super.onPause()
        videoPlayerView?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoPlayerView?.stop()
    }

    fun applyWindow(window: Window) {
        window.decorView.apply {
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            // a general rule, you should design your app to hide the status bar whenever you
            // hide the navigation bar.
            systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

}