package com.example.exoplayerdemo.seekbar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import com.example.exoplayerdemo.R
import com.example.exoplayerdemo.utils.SizeUtils
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.roundToInt

class IndicatorSeekBar : View {

    companion object {
        private const val THUMB_MAX_WIDTH = 30
    }

    private var mStockPaint = Paint()
    private var mTextPaint = TextPaint()
    private var mRect = Rect()
    private var mProgressTrack = RectF()
    private var mBackgroundTrack = RectF()

    private var mMax: Float = 0F
    private var mMin: Float = 0F
    private var mProgress: Float = 0F
    private var mOnlyThumbDraggable: Boolean = false
    private var mBackgroundTrackSize: Int = 0
    private var mProgressTrackSize: Int = 0
    private var mBackgroundTrackColor: Int = 0
    private var mProgressTrackColor: Int = 0
    private var mTrackRoundedCorners: Boolean = false
    private var mThumbSize: Int = 0
    private var mThumbDrawable: Drawable? = null
    private var mAdjustAuto: Boolean = false
    private var mTicksCount: Int = 0
    private var mTickMarksSize: Int = 0
    private var mTickMarksDrawable: Drawable? = null
    private var mShowTickText: Boolean = false
    private var mTickTextsSize: Int = 0
    private var mShowIndicatorType: Int = 0
    private var mIndicatorColor: Int = 0
    private var mIndicatorTextSize: Int = 0
    private var mIndicatorTextColor: Int = 0
    private var mThumbColor: Int = 0
    private var mPressedThumbColor: Int = 0
    private var mShowTickMarksType: Int = 0
    private var mSelectedTickMarksColor: Int = 0
    private var mUnSelectedTickMarksColor: Int = 0
    private var mUnselectedTextsColor: Int = 0
    private var mSelectedTextsColor: Int = 0
    private var mHoveredTextColor: Int = 0
    private var mTextTypeface: Typeface? = null
    private var mThumbRadius: Float = 0F
    private var mThumbTouchRadius: Float = 0F
    private var mTickRadius: Float = 0F
    private var mCustomDrawableMaxHeight: Float = 0F
    private var mTickTextsHeight: Int = 0
    private var lastProgress: Float = 0F
    private var mTickMarksX: FloatArray? = null
    private var mTextCenterX: FloatArray? = null
    private var mTickTextsWidth: FloatArray? = null
    private var mProgressArr: FloatArray? = null
    private var mMeasuredWidth: Int = 0
    private var mPaddingLeft: Int = 0
    private var mPaddingRight: Int = 0
    private var mPaddingTop: Int = 0
    private var mSeekLength: Float = 0F
    private var mSeekBlockLength: Float = 0F
    private var mTickTextY: Float = 0F
    private var mTickTextsArr: Array<Indicator?>? = null
    private var mTickTextsCustomArray: CharSequence? = null
    private var mSelectTickMarksBitmap: Bitmap? = null
    private var mUnselectTickMarksBitmap: Bitmap? = null
    private var mThumbBitmap: Bitmap? = null
    private var mPressedThumbBitmap: Bitmap? = null
    private var mIsTouching: Boolean = false
    private var mFaultTolerance: Float = -1F
    private var mScale: Int = 1
    private var mIndicatorStayAlways: Boolean = true
    private var mSelectedTickMarksOddColor: Int = 0
    private var mUnSelectedTickMarksOddColor: Int = 0
    private var mTickMarksOddDrawable: Drawable? = null

    private var mSeekChangeListener: OnSeekChangeListener? = null
    private var mSeekParams: SeekParams? = null

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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = (mCustomDrawableMaxHeight + paddingTop + paddingBottom).roundToInt()
        setMeasuredDimension(
            resolveSize(SizeUtils.dp2px(context, 170F), widthMeasureSpec),
            height + mTickTextsHeight
        )
        initSeekBarInfo()
        refreshSeekBarLocation()
    }

    @Synchronized
    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            drawTrack(it)
            drawTickMarks(it)
            drawTickTexts(it)
            drawThumb(it)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        if (enabled == isEnabled) return
        super.setEnabled(enabled)
        alpha = if (isEnabled) {
            1.0F
        } else {
            0.3F
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        post { requestLayout() }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val parent = parent ?: return super.dispatchTouchEvent(event)
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> parent.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP or MotionEvent.ACTION_CANCEL -> parent.requestDisallowInterceptTouchEvent(
                false
            )
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled) return false
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                performClick()
                val mX = event.x
                if (isTouchSeekBar(mX, event.y)) {
                    if (mOnlyThumbDraggable && !isTouchThumb(mX)) return false
                    mIsTouching = true
                    mSeekChangeListener?.onStartTrackingTouch(this@IndicatorSeekBar)
                    refreshSeekBar(event)
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                refreshSeekBar(event)
            }
            MotionEvent.ACTION_UP or MotionEvent.ACTION_CANCEL -> {
                mIsTouching = false
                mSeekChangeListener?.onStopTrackingTouch(this)
            }
        }
        return super.onTouchEvent(event)
    }

    private fun initAttrs(attrs: AttributeSet?) {
        val builder = IndicatorSeekBarBuilder(context)
        if (attrs == null) {
            applyBuilder(builder)
            return
        }
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.IndicatorSeekBar)
        typeArray.let {
            mMax = it.getFloat(R.styleable.IndicatorSeekBar_isb_max, builder.max)
            mMin = it.getFloat(R.styleable.IndicatorSeekBar_isb_min, builder.min)
            mProgress = it.getFloat(R.styleable.IndicatorSeekBar_isb_progress, builder.progress)
            mOnlyThumbDraggable = it.getBoolean(
                R.styleable.IndicatorSeekBar_isb_only_thumb_draggable,
                builder.onlyThumbDraggable
            )
            mBackgroundTrackSize = it.getDimensionPixelSize(
                R.styleable.IndicatorSeekBar_isb_track_background_size,
                builder.trackBackgroundSize
            )
            mProgressTrackSize = it.getDimensionPixelSize(
                R.styleable.IndicatorSeekBar_isb_track_progress_size,
                builder.trackProgressSize
            )
            mBackgroundTrackColor = it.getColor(
                R.styleable.IndicatorSeekBar_isb_track_background_color,
                builder.trackBackgroundColor
            )
            mProgressTrackColor = it.getColor(
                R.styleable.IndicatorSeekBar_isb_track_progress_color,
                builder.trackProgressColor
            )
            mTrackRoundedCorners = it.getBoolean(
                R.styleable.IndicatorSeekBar_isb_track_rounded_corners,
                builder.trackRoundedCorners
            )
            mThumbSize = it.getDimensionPixelSize(
                R.styleable.IndicatorSeekBar_isb_thumb_size,
                builder.thumbSize
            )
            mThumbDrawable = it.getDrawable(R.styleable.IndicatorSeekBar_isb_thumb_drawable)
            mAdjustAuto = it.getBoolean(R.styleable.IndicatorSeekBar_isb_thumb_adjust_auto, true)
            initThumbColor(
                it.getColor(
                    R.styleable.IndicatorSeekBar_isb_thumb_color,
                    builder.thumbColor
                )
            )
            mTicksCount = it.getInt(R.styleable.IndicatorSeekBar_isb_ticks_count, builder.tickCount)
            mShowTickMarksType = it.getInt(
                R.styleable.IndicatorSeekBar_isb_show_tick_marks_type,
                builder.showTickMarksType
            )
            mTickMarksSize = it.getDimensionPixelSize(
                R.styleable.IndicatorSeekBar_isb_tick_marks_size,
                builder.tickMarksSize
            )
            initTickMarksColor(
                it.getColor(
                    R.styleable.IndicatorSeekBar_isb_tick_marks_color,
                    builder.tickMarksColor
                )
            )
            mTickMarksDrawable =
                it.getDrawable(R.styleable.IndicatorSeekBar_isb_tick_marks_drawable)
            mShowTickText = it.getBoolean(
                R.styleable.IndicatorSeekBar_isb_show_tick_texts,
                builder.showTickText
            )
            mTickTextsSize = it.getDimensionPixelSize(
                R.styleable.IndicatorSeekBar_isb_tick_texts_size,
                builder.tickTextsSize
            )
            initTickTextsColor(
                it.getColorStateList(R.styleable.IndicatorSeekBar_isb_tick_texts_color),
                builder.tickTextsColor
            )
            initTextsTypeface(
                it.getInt(R.styleable.IndicatorSeekBar_isb_tick_texts_typeface, -1),
                builder.tickTextsTypeFace
            )
            mShowIndicatorType = it.getInt(
                R.styleable.IndicatorSeekBar_isb_show_indicator,
                builder.showIndicatorType
            )
            mIndicatorColor = it.getColor(
                R.styleable.IndicatorSeekBar_isb_indicator_color,
                builder.indicatorColor
            )
            mIndicatorTextSize = it.getDimensionPixelSize(
                R.styleable.IndicatorSeekBar_isb_indicator_text_size,
                builder.indicatorTextSize
            )
            mIndicatorTextColor = it.getColor(
                R.styleable.IndicatorSeekBar_isb_indicator_text_color,
                builder.indicatorTextColor
            )
            initTickMarksOddColor(
                it.getColor(
                    R.styleable.IndicatorSeekBar_isb_tick_marks_odd_color,
                    builder.tickMarksOddColor
                )
            )
            mTickMarksOddDrawable =
                it.getDrawable(R.styleable.IndicatorSeekBar_isb_tick_marks_odd_drawable)
            mTickMarksOddDrawable?.let { mTickMarksDrawable = it }
        }
        typeArray.recycle()
    }

    private fun initParams() {
        initProgressRangeValue()
        if (mBackgroundTrackSize > mProgressTrackSize) mBackgroundTrackSize = mProgressTrackSize
        mThumbDrawable?.let {
            mThumbRadius =
                Math.min(SizeUtils.dp2px(context, THUMB_MAX_WIDTH.toFloat()), mThumbSize)
                    .toFloat() / 2.0F
            mThumbTouchRadius = mThumbRadius
        } ?: run {
            mThumbRadius = mThumbSize.toFloat() / 2.0F
            mThumbTouchRadius = mThumbRadius * 1.2F
        }
        mTickMarksDrawable?.let {
            mTickRadius =
                Math.min(SizeUtils.dp2px(context, THUMB_MAX_WIDTH.toFloat()), mTickMarksSize)
                    .toFloat() / 2.0F
        } ?: run {
            mTickRadius = mTickMarksSize.toFloat() / 2.0F
        }
        mCustomDrawableMaxHeight = Math.max(mThumbTouchRadius, mTickRadius) * 2.0F
        initStrokePaint()
        measureTickTextsBonds()
        lastProgress = mProgress

        collectTicksInfo()
        initDefaultPadding()
    }

    private fun initThumbColor(@ColorInt color: Int) {
        mThumbColor = color
        mPressedThumbColor = mThumbColor
    }

    private fun initTickMarksColor(@ColorInt color: Int) {
        mSelectedTickMarksColor = color
        mUnSelectedTickMarksColor = mSelectedTickMarksColor
    }

    private fun initTickMarksOddColor(@ColorInt color: Int) {
        mSelectedTickMarksOddColor = color
        mUnSelectedTickMarksOddColor = mSelectedTickMarksOddColor
    }

    private fun initTickTextsColor(colorStateList: ColorStateList?, defaultColor: Int) {
        if (colorStateList == null) {
            mUnselectedTextsColor = defaultColor
            mSelectedTextsColor = mUnselectedTextsColor
            mHoveredTextColor = mUnselectedTextsColor
            return
        }
        var states: Array<IntArray>? = null
        var colors: IntArray? = null
        val aClass: Class<out ColorStateList?> = colorStateList.javaClass
        try {
            val f = aClass.declaredFields
            for (field in f) {
                field.isAccessible = true
                if ("mStateSpecs" == field.name) {
                    states = field[colorStateList] as Array<IntArray>
                }
                if ("mColors" == field.name) {
                    colors = field[colorStateList] as IntArray
                }
            }
            if (states == null || colors == null) {
                return
            }
        } catch (e: java.lang.Exception) {
            throw RuntimeException("Something wrong happened when parseing thumb selector color.")
        }
        if (states.size == 1) {
            mUnselectedTextsColor = colors[0]
            mSelectedTextsColor = mUnselectedTextsColor
            mHoveredTextColor = mUnselectedTextsColor
        } else if (states.size == 3) {
            for (i in states.indices) {
                val attr = states[i]
                if (attr.isEmpty()) { //didn't have state,so just get color.
                    mUnselectedTextsColor = colors[i]
                    continue
                }
                when (attr[0]) {
                    android.R.attr.state_selected -> mSelectedTextsColor = colors[i]
                    android.R.attr.state_hovered -> mHoveredTextColor = colors[i]
                    else -> throw java.lang.IllegalArgumentException("the selector color file you set for the argument: isb_tick_texts_color is in wrong format.")
                }
            }
        } else {
            //the color selector file was set by a wrong format , please see above to correct.
            throw java.lang.IllegalArgumentException("the selector color file you set for the argument: isb_tick_texts_color is in wrong format.")
        }
    }

    private fun initTextsTypeface(typeface: Int, defaultTypeface: Typeface?) {
        mTextTypeface = when (typeface) {
            0 -> Typeface.DEFAULT
            1 -> Typeface.MONOSPACE
            2 -> Typeface.SANS_SERIF
            3 -> Typeface.SERIF
            else -> {
                defaultTypeface ?: Typeface.DEFAULT
            }
        }
    }

    private fun initProgressRangeValue() {
        if (mMax < mMin) {
            throw IllegalArgumentException("the Argument: MAX's value must be larger than MIN's.")
        }
        if (mProgress < mMin) {
            mProgress = mMin
        }
        if (mProgress > mMax) {
            mProgress = mMax
        }
    }

    private fun initStrokePaint() {
        if (mTrackRoundedCorners) {
            mStockPaint.strokeCap = Paint.Cap.ROUND
        }
        mStockPaint.isAntiAlias = true
        if (mBackgroundTrackSize > mProgressTrackSize) mProgressTrackSize = mBackgroundTrackSize
    }

    private fun measureTickTextsBonds() {
        if (needDrawText()) {
            initTextPaint()
            mTextPaint.typeface = mTextTypeface
            mTextPaint.getTextBounds("j", 0, 1, mRect)
            mTickTextsHeight = mRect.height() + SizeUtils.dp2px(context, 3F)
        }
    }

    private fun needDrawText(): Boolean {
        return mTicksCount != 0 || mShowTickText
    }

    private fun initTextPaint() {
        mTextPaint.apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = mTickTextsSize.toFloat()
        }
    }

    private fun collectTicksInfo() {
        if (mTicksCount < 0 || mTicksCount > 50) {
            throw IllegalArgumentException("the Argument: TICK COUNT must be limited between (0-50), Now is $mTicksCount")
        }
        if (mTicksCount != 0) {
            mTickMarksX = FloatArray(mTicksCount)
            if (mShowTickText) {
                mTextCenterX = FloatArray(mTicksCount)
                mTickTextsWidth = FloatArray(mTicksCount)
            }
            mProgressArr = FloatArray(mTicksCount)
            for (i in 0 until mProgressArr!!.size) {
                mProgressArr!![i] =
                    mMin + i * (mMax - mMin) / (if ((mTicksCount - 1) > 0) (mTicksCount - 1) else 1)
            }
        }
    }

    private fun initDefaultPadding() {
        val normalPadding = SizeUtils.dp2px(context, 16F)
        if (paddingLeft == 0) {
            setPadding(normalPadding, paddingTop, paddingRight, paddingBottom)
        }
        if (paddingRight == 0) {
            setPadding(paddingLeft, paddingTop, normalPadding, paddingBottom)
        }
    }

    private fun initSeekBarInfo() {
        mMeasuredWidth = measuredWidth
        mPaddingLeft = paddingStart
        mPaddingRight = paddingEnd
        mPaddingTop = paddingTop
        mSeekLength = (mMeasuredWidth - mPaddingLeft - mPaddingRight).toFloat()
        mSeekBlockLength = mSeekLength / (if ((mTicksCount - 1) > 0) (mTicksCount - 1) else 1)
    }

    private fun refreshSeekBarLocation() {
        initTrackLocation()
        if (needDrawText()) {
            mTextPaint.getTextBounds("j", 0, 1, mRect)
            mTickTextY =
                mPaddingTop + mCustomDrawableMaxHeight + (mRect.height() - mTextPaint.descent()).roundToInt() + SizeUtils.dp2px(
                    context,
                    3F
                )
        }
        if (mTickMarksX == null) return
        initTextsArray()
        if (mTicksCount > 2) {
            mProgress = mProgressArr!![getClosestIndex()]
            lastProgress = mProgress
        }
        refreshThumbCenterXByProgress(mProgress)
    }

    private fun initTrackLocation() {
        mProgressTrack.apply {
            left = mPaddingLeft.toFloat()
            top = mPaddingTop + mThumbTouchRadius
            right =
                (mProgress - mMin) * mSeekLength / getAmplitude() + mPaddingLeft.toFloat()
            bottom = top
        }
        mBackgroundTrack.apply {
            left = mProgressTrack.right
            top = mProgressTrack.bottom
            right = mMeasuredWidth.toFloat() - mPaddingRight.toFloat()
            bottom = mProgressTrack.bottom
        }
    }

    private fun getAmplitude(): Float {
        return if ((mMax - mMin) > 0) (mMax - mMin) else 1F
    }

    private fun initTextsArray() {
        if (mTicksCount == 0) return
        mTickTextsArr = arrayOfNulls(mTicksCount)
        for (i in 0 until mTickMarksX!!.size) {
            if (mShowTickText) {
                mTickTextsArr!![i] = getTickTextByPosition(i)
                mTextPaint.getTextBounds(
                    mTickTextsArr!![i]!!.tickText,
                    0,
                    mTickTextsArr!![i]!!.tickText.length,
                    mRect
                )
                mTickTextsWidth!![i] = mRect.width().toFloat()
                mTextCenterX!![i] = mPaddingLeft + mSeekBlockLength * i
            }
            mTickMarksX!![i] = mPaddingLeft + mSeekBlockLength * i
        }
    }

    private fun getTickTextByPosition(index: Int): Indicator {
//        if (mTickTextsCustomArray == null) return getProgressString(mProgressArr!![index])
//        if (index < mTickTextsCustomArray!!.length) return mTickTextsCustomArray!![index].toString()
//        return ""
        return getProgressString(mProgressArr!![index])
    }

    private fun getProgressString(progress: Float): Indicator {
        if (progress % 1F == 0F) return Indicator().apply {
            tickText = progress.roundToInt().toString()
            isInteger = true
        }
        return Indicator().apply {
            tickText = progress.toString()
            isInteger = false
        }
    }

    private fun getClosestIndex(): Int {
        var closestIndex = 0
        var amplitude = abs(mMax - mMin)
        for (i in 0 until mProgressArr!!.size) {
            val amplitudeTemp = abs(mProgressArr!![i] - mProgress)
            if (amplitudeTemp <= amplitude) {
                amplitude = amplitudeTemp
                closestIndex = i
            }
        }
        return closestIndex
    }

    private fun refreshThumbCenterXByProgress(progress: Float) {
        mProgressTrack.right = (progress - mMin) * mSeekLength / getAmplitude() + mPaddingLeft
        mBackgroundTrack.left = mProgressTrack.right
    }

    private fun drawTrack(canvas: Canvas) {
        mStockPaint.color = mProgressTrackColor
        mStockPaint.strokeWidth = mProgressTrackSize.toFloat()
        canvas.drawLine(
            mProgressTrack.left,
            mProgressTrack.top,
            mProgressTrack.right,
            mProgressTrack.bottom,
            mStockPaint
        )
        mStockPaint.color = mBackgroundTrackColor
        mStockPaint.strokeWidth = mBackgroundTrackSize.toFloat()
        canvas.drawLine(
            mBackgroundTrack.left,
            mBackgroundTrack.top,
            mBackgroundTrack.right,
            mBackgroundTrack.bottom,
            mStockPaint
        )
    }

    private fun drawTickMarks(canvas: Canvas) {
        if (mTicksCount == 0 || (mShowTickMarksType == TickMarkType.NONE && mTickMarksDrawable == null)) return
        val thumbCenterX = getThumbCenterX()
        for (i in 0 until mTickMarksX!!.size) {
            val thumbPosFloat = getThumbPosOnTickFloat()
            if (i == getThumbPosOnTick() && mTicksCount > 2) continue
            if (i <= thumbPosFloat) {
                mStockPaint.color = getLeftSideTickColor()
            } else {
                mStockPaint.color = getRightSideTickColor()
            }
            if (mTickMarksDrawable != null && mTickTextsArr!![i]!!.isInteger) {
                if (mSelectTickMarksBitmap == null || mUnselectTickMarksBitmap == null)
                    initTickMarksBitmap()
                require(!(mSelectTickMarksBitmap == null || mUnselectTickMarksBitmap == null)) {
                    //please check your selector drawable's format and correct.
                    "the format of the selector TickMarks drawable is wrong!"
                }
                if (i <= thumbPosFloat) {
                    canvas.drawBitmap(
                        mSelectTickMarksBitmap!!,
                        mTickMarksX!![i] - mUnselectTickMarksBitmap!!.width / 2.0f,
                        mProgressTrack.top - mUnselectTickMarksBitmap!!.height / 2.0f,
                        mStockPaint
                    )
                } else {
                    canvas.drawBitmap(
                        mUnselectTickMarksBitmap!!,
                        mTickMarksX!![i] - mUnselectTickMarksBitmap!!.width / 2.0f,
                        mProgressTrack.top - mUnselectTickMarksBitmap!!.height / 2.0f,
                        mStockPaint
                    )
                }
                continue
            }

            when (mShowTickMarksType) {
                TickMarkType.OVAL -> {
                    canvas.drawCircle(
                        mTickMarksX!![i],
                        mProgressTrack.top,
                        mTickRadius,
                        mStockPaint
                    )
                }
                TickMarkType.DIVIDER -> {
                    val rectWidth = SizeUtils.dp2px(context, 1F)
                    val dividerTickHeight =
                        if (thumbCenterX >= mTickMarksX!![i]) getLeftSideTrackSize() else getRightSideTrackSize()
                    canvas.drawRect(
                        mTickMarksX!![i] - rectWidth,
                        mProgressTrack.top - dividerTickHeight / 2F,
                        mTickMarksX!![i] + rectWidth,
                        mProgressTrack.top + dividerTickHeight / 2.0F,
                        mStockPaint
                    )
                }
                TickMarkType.SQUARE -> {
                    canvas.drawRect(
                        mTickMarksX!![i] - mTickMarksSize / 2F,
                        mProgressTrack.top - mTickMarksSize / 2F,
                        mTickMarksX!![i] + mTickMarksSize / 2F,
                        mProgressTrack.top + mTickMarksSize / 2F,
                        mStockPaint
                    )
                }
            }
        }
    }

    private fun getThumbCenterX() = mProgressTrack.right

    private fun getThumbPosOnTickFloat(): Float {
        if (mTicksCount != 0) return (getThumbCenterX() - mPaddingLeft) / mSeekBlockLength
        return 0F
    }

    private fun getThumbPosOnTick(): Int {
        if (mTicksCount != 0) return ((getThumbCenterX() - mPaddingLeft) / mSeekBlockLength).roundToInt()
        return 0
    }

    private fun getLeftSideTickColor(): Int = mSelectedTickMarksColor

    private fun getRightSideTickColor(): Int = mUnSelectedTickMarksColor

    private fun getLeftSideTickOddColor(): Int = mSelectedTickMarksOddColor

    private fun getRightSideTickOddColor(): Int = mUnSelectedTickMarksOddColor

    private fun initTickMarksBitmap() {
        if (mTickMarksDrawable is StateListDrawable) {
            val listDrawable = mTickMarksDrawable as StateListDrawable
            try {
                val aClass: Class<out StateListDrawable> = listDrawable.javaClass
                val getStateCount = aClass.getMethod("getStateCount")
                val stateCount = getStateCount.invoke(listDrawable) as Int
                if (stateCount == 2) {
                    val getStateSet = aClass.getMethod("getStateSet", Int::class.javaPrimitiveType)
                    val getStateDrawable = aClass.getMethod(
                        "getStateDrawable",
                        Int::class.javaPrimitiveType
                    )
                    for (i in 0 until stateCount) {
                        val stateSet = getStateSet.invoke(listDrawable, i) as IntArray
                        if (stateSet.isNotEmpty()) {
                            mSelectTickMarksBitmap =
                                if (stateSet[0] == android.R.attr.state_selected) {
                                    val stateDrawable =
                                        getStateDrawable.invoke(listDrawable, i) as Drawable
                                    getDrawBitmap(stateDrawable, false)
                                } else {
                                    //please check your selector drawable's format, please see above to correct.
                                    throw java.lang.IllegalArgumentException("the state of the selector TickMarks drawable is wrong!")
                                }
                        } else {
                            val stateDrawable = getStateDrawable.invoke(listDrawable, i) as Drawable
                            mUnselectTickMarksBitmap = getDrawBitmap(stateDrawable, false)
                        }
                    }
                } else {
                    //please check your selector drawable's format, please see above to correct.
                    throw java.lang.IllegalArgumentException("the format of the selector TickMarks drawable is wrong!")
                }
            } catch (e: Exception) {
                mUnselectTickMarksBitmap = getDrawBitmap(mTickMarksDrawable!!, false)
                mSelectTickMarksBitmap = mUnselectTickMarksBitmap
            }
        } else {
            mUnselectTickMarksBitmap = getDrawBitmap(mTickMarksDrawable!!, false)
            mSelectTickMarksBitmap = mUnselectTickMarksBitmap
        }
    }

    private fun getLeftSideTrackSize(): Int = mProgressTrackSize

    private fun getRightSideTrackSize(): Int = mBackgroundTrackSize

    private fun drawTickTexts(canvas: Canvas) {
        if (mTickTextsArr!!.isEmpty()) return
        val thumbPosFloat = getThumbPosOnTickFloat()
        for (i in mTickTextsArr!!.indices) {
            if (mTickTextsArr!![i]!!.isInteger) {
                if (i == getThumbPosOnTick() && i == thumbPosFloat.roundToInt()) {
                    mTextPaint.color = mSelectedTextsColor
                } else if (i < thumbPosFloat) {
                    mTextPaint.color = getLeftSideTickTextsColor()
                } else {
                    mTextPaint.color = getRightSideTickTextsColor()
                }
                when (i) {
                    0 -> {
                        canvas.drawText(
                            mTickTextsArr!![i]!!.tickText,
                            mTextCenterX!![i] + mTickTextsWidth!![i] / 2F,
                            mTickTextY,
                            mTextPaint
                        )
                    }
                    mTickTextsArr!!.size - 1 -> {
                        canvas.drawText(
                            mTickTextsArr!![i]!!.tickText,
                            mTextCenterX!![i] - mTickTextsWidth!![i] / 2.0F,
                            mTickTextY,
                            mTextPaint
                        )
                    }
                    else -> {
                        canvas.drawText(
                            mTickTextsArr!![i]!!.tickText,
                            mTextCenterX!![i],
                            mTickTextY,
                            mTextPaint
                        )
                    }
                }
            }
        }
    }

    private fun getLeftSideTickTextsColor(): Int = mUnselectedTextsColor

    private fun getRightSideTickTextsColor(): Int = mUnselectedTextsColor

    private fun drawThumb(canvas: Canvas) {
        val thumbCenterX = getThumbCenterX()
        if (mThumbDrawable != null) {
            if (mThumbBitmap == null || mPressedThumbBitmap == null) {
                initThumbBitmap()
            }
            if (mThumbBitmap == null || mPressedThumbBitmap == null)
                throw IllegalArgumentException("the format of the selector thumb drawable is wrong!")
            mStockPaint.alpha = 255
            if (mIsTouching) {
                canvas.drawBitmap(
                    mPressedThumbBitmap!!,
                    thumbCenterX - mPressedThumbBitmap!!.width / 2F,
                    mProgressTrack.top - mPressedThumbBitmap!!.height / 2F,
                    mStockPaint
                )
            } else {
                canvas.drawBitmap(
                    mThumbBitmap!!,
                    thumbCenterX - mThumbBitmap!!.width / 2F,
                    mProgressTrack.top - mThumbBitmap!!.height / 2F,
                    mStockPaint
                )
            }
        } else {
            if (mIsTouching) {
                mStockPaint.color = mPressedThumbColor
            } else {
                mStockPaint.color = mThumbColor
            }
            canvas.drawCircle(
                thumbCenterX,
                mProgressTrack.top,
                if (mIsTouching) mThumbTouchRadius else mThumbRadius,
                mStockPaint
            )
        }
    }

    private fun initThumbBitmap() {
        if (mThumbBitmap == null) return
        mThumbBitmap = mThumbDrawable?.let { getDrawBitmap(it, true) }
        mPressedThumbBitmap = mThumbBitmap
    }

    private fun getDrawBitmap(drawable: Drawable, isThumb: Boolean): Bitmap {
        var width: Int
        var height: Int
        val maxRange = SizeUtils.dp2px(context, THUMB_MAX_WIDTH.toFloat())
        val intrinsicWidth = drawable.intrinsicWidth
        if (intrinsicWidth > maxRange) {
            width = if (isThumb) {
                mThumbSize
            } else {
                mTickMarksSize
            }
            height = getHeightByRatio(drawable, width)

            if (width > maxRange) {
                width = maxRange
                height = getHeightByRatio(drawable, width)
            }
        } else {
            width = drawable.intrinsicWidth
            height = drawable.intrinsicHeight
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun getHeightByRatio(drawable: Drawable, width: Int): Int {
        val intrinsicWidth = drawable.intrinsicWidth
        val intrinsicHeight = drawable.intrinsicHeight
        return (1.0f * width * intrinsicHeight / intrinsicWidth).roundToInt()
    }

    private fun isTouchSeekBar(mX: Float, mY: Float): Boolean {
        if (mFaultTolerance == -1F) {
            mFaultTolerance = SizeUtils.dp2px(context, 5F).toFloat()
        }
        val inWidthRange =
            mX >= (mPaddingLeft - 2 * mFaultTolerance) && mX <= (mMeasuredWidth - mPaddingRight + 2 * mFaultTolerance)
        val inHeightRange =
            mY >= mProgressTrack.top - mThumbTouchRadius - mFaultTolerance && mY <= mProgressTrack.top + mThumbTouchRadius + mFaultTolerance
        return inWidthRange && inHeightRange
    }

    private fun isTouchThumb(mX: Float): Boolean {
        refreshThumbCenterXByProgress(mProgress)
        val rawTouchX = mProgressTrack.right
        return rawTouchX - mThumbSize / 2F <= mX && mX <= rawTouchX + mThumbSize / 2F
    }

    fun setOnSeekChangeListener(@NonNull listener: OnSeekChangeListener) {
        mSeekChangeListener = listener
    }

    private fun refreshSeekBar(event: MotionEvent) {
        refreshThumbCenterXByProgress(calculateProgress(calculateTouchX(adjustTouchX(event))))
        setSeekListener(true)
        updateIndicator()
        invalidate()
    }

    private fun calculateProgress(touchX: Float): Float {
        lastProgress = mProgress
        mProgress = mMin + (getAmplitude() * (touchX - mPaddingLeft)) / mSeekLength
        return mProgress
    }

    private fun calculateTouchX(touchX: Float): Float {
        return when {
            mTicksCount > 2 -> mSeekBlockLength * ((touchX - mPaddingLeft) / mSeekBlockLength).roundToInt() + mPaddingLeft
            else -> touchX
        }
    }

    private fun adjustTouchX(event: MotionEvent): Float {
        return when {
            event.x < mPaddingLeft -> mPaddingLeft.toFloat()
            event.x > mMeasuredWidth - mPaddingRight -> (mMeasuredWidth - mPaddingRight).toFloat()
            else -> event.x
        }
    }

    private fun setSeekListener(fromUser: Boolean) {
        mSeekChangeListener?.onSeeking(collectParams(fromUser)) ?: return
    }

    private fun collectParams(fromUser: Boolean): SeekParams {
        if (mSeekParams == null)
            mSeekParams = SeekParams(this).apply {
                this.progress = getProgress()
                this.progressFloat = getProgressFloat()
                this.fromUser = fromUser
            }
        if (mTicksCount > 2) {
            val rawThumbPos = getThumbPosOnTick()
            if (mShowTickText && !mTickTextsArr.isNullOrEmpty()) {
                mSeekParams!!.tickText = mTickTextsArr!![rawThumbPos]!!.tickText
            }
            mSeekParams!!.thumbPosition = rawThumbPos
        }
        return mSeekParams!!
    }

    private fun getProgress() = mProgress.roundToInt()

    @Synchronized
    fun getProgressFloat(): Float {
        val bigDecimal = BigDecimal.valueOf(mProgress.toDouble())
        return bigDecimal.setScale(mScale, BigDecimal.ROUND_HALF_UP).toFloat()
    }

    private fun updateIndicator() {

    }

    private fun applyBuilder(builder: IndicatorSeekBarBuilder) {
        this.mMax = builder.max
        this.mMin = builder.min
        this.mProgress = builder.progress
        this.mOnlyThumbDraggable = builder.onlyThumbDraggable
        this.mBackgroundTrackSize = builder.trackBackgroundSize
        this.mProgressTrackSize = builder.trackProgressSize
        this.mBackgroundTrackColor = builder.trackBackgroundColor
        this.mProgressTrackColor = builder.trackProgressColor
        this.mTrackRoundedCorners = builder.trackRoundedCorners
        this.mThumbSize = builder.thumbSize
        this.mThumbDrawable = builder.thumbDrawable
        this.mTicksCount = builder.tickCount
        this.mTickMarksSize = builder.tickMarksSize
        this.mTickMarksDrawable = builder.tickMarksDrawable
        this.mShowTickText = builder.showTickText
        this.mTickTextsSize = builder.tickTextsSize
        this.mShowIndicatorType = builder.showIndicatorType
        this.mIndicatorColor = builder.indicatorColor
        this.mIndicatorTextSize = builder.indicatorTextSize
        this.mIndicatorTextColor = builder.indicatorTextColor
    }
}