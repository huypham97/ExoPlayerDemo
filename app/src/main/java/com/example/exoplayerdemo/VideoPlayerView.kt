package com.example.exoplayerdemo

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.example.exoplayerdemo.databinding.ViewVideoPlayerBinding
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

class VideoPlayerView : FrameLayout {

    companion object {
        private const val TAG = "VideoPlayerView"
    }

    private lateinit var binding: ViewVideoPlayerBinding

    private var player: ExoPlayer? = null

    private var stateListener: Player.Listener = object : Player.Listener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

            val stateString: String = when (playbackState) {
                Player.STATE_IDLE -> "ExoPlayer.STATE_IDLE"
                Player.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING"
                Player.STATE_READY -> "ExoPlayer.STATE_READY"
                Player.STATE_ENDED -> "ExoPlayer.STATE_ENDED"
                else -> "UNKNOWN_STATE"
            }
            Log.d(TAG, "onPlayerStateChanged: $stateString")
        }

        override fun onRenderedFirstFrame() {
            super.onRenderedFirstFrame()
        }
    }

    constructor(context: Context) : super(context) {
        this.initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.initView()
    }

    private fun initView() {
        this.binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.view_video_player,
            this,
            true
        )
        this.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        this.binding.videoView.setShowMultiWindowTimeBar(true)
    }

    fun setData(link: List<String>) {
        releasePlayer()
        initPlayer(link)
    }

    private fun releasePlayer() {
        this.player?.let { player ->
            player.stop()
            player.removeListener(this.stateListener)
            player.release()
        }
        this.binding.videoView.player = null
        this.player = null
    }

    private fun initPlayer(urls: List<String>) {
        releasePlayer()
        this.player = ExoPlayer.Builder(this.context).build()
        val newItems: List<MediaItem> = urls.map {
            MediaItem.fromUri(it)
        }
        this.player?.let { player ->
            this.binding.videoView.player = player
            player.addListener(stateListener)
            player.addMediaItems(newItems)
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            player.prepare()
        }
    }

    fun start() {
        this.player?.play()
    }

    fun pause() {
        this.player?.playWhenReady = false
    }

    fun stop() {
        releasePlayer()
    }

}