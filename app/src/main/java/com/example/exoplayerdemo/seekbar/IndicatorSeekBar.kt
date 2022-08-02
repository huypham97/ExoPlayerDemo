package com.example.exoplayerdemo.seekbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.example.exoplayerdemo.R

class IndicatorSeekBar : View {

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initAttrs(attrs)
        initParams()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttrs(attrs)
        initParams()
    }

    private fun initAttrs(attrs: AttributeSet?) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.IndicatorSeekBar)
    }

    private fun initParams() {

    }
}