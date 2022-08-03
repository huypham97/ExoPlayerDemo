package com.example.exoplayerdemo.seekbar

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import com.example.exoplayerdemo.utils.SizeUtils

class IndicatorSeekBarBuilder(val context: Context) {
    var max = 100F
    var min = 0F
    var progress = 0F
    var onlyThumbDraggable = false
    var showIndicatorType = IndicatorType.ROUNDED_RECTANGLE
    var indicatorColor = Color.parseColor("#FF4081")
    var indicatorTextColor = Color.parseColor("#FFFFFF")
    var indicatorTextSize = 0
    var trackBackgroundSize = 0
    var trackBackgroundColor = Color.parseColor("#D7D7D7")
    var trackProgressSize = 0
    var trackProgressColor = Color.parseColor("#FF4081")
    var trackRoundedCorners = false
    var thumbSize = 0
    var thumbColor = Color.parseColor("#FF4081")
    var thumbDrawable: Drawable? = null
    var showTickText: Boolean = true
    var tickTextsColor = Color.parseColor("#FF4081")
    var tickTextsSize = 0
    var tickTextsTypeFace = Typeface.DEFAULT
    var tickCount = 0
    var showTickMarksType = TickMarkType.NONE
    var tickMarksColor = Color.parseColor("#FF4081")
    var tickMarksSize = 0
    var tickMarksDrawable: Drawable? = null

    init {
        this.indicatorTextSize = SizeUtils.sp2px(context, 14F)
        this.trackBackgroundSize = SizeUtils.sp2px(context, 2F)
        this.trackProgressSize = SizeUtils.sp2px(context, 2F)
        this.tickMarksSize = SizeUtils.sp2px(context, 10F)
        this.tickTextsSize = SizeUtils.sp2px(context, 13F)
        this.thumbSize = SizeUtils.sp2px(context, 14F)
    }
}