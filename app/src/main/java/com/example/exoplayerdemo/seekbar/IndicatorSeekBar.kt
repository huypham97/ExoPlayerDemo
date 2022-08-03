package com.example.exoplayerdemo.seekbar

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.example.exoplayerdemo.R
import com.example.exoplayerdemo.utils.SizeUtils
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
    private var mTickTextsArr = arrayOf<String>()
    private var mTickTextsCustomArray: CharSequence? = null
    private var mSelectTickMarksBitmap: Bitmap? = null
    private var mUnselectTickMarksBitmap: Bitmap? = null
    private var mThumbBitmap: Bitmap? = null
    private var mPressedThumbBitmap: Bitmap? = null

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
            mThumbColor =
                it.getColor(R.styleable.IndicatorSeekBar_isb_thumb_color, builder.thumbColor)
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
                it.getColor(
                    R.styleable.IndicatorSeekBar_isb_tick_texts_color,
                    builder.tickTextsColor
                )
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

    private fun initTickMarksColor(color: Int) {
        mSelectedTickMarksColor = color
        mUnSelectedTickMarksColor = mSelectedTickMarksColor
    }

    private fun initTickTextsColor(color: Int) {
        mUnselectedTextsColor = color
        mSelectedTextsColor = mUnselectedTextsColor
        mHoveredTextColor = mUnselectedTextsColor
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
        for (i in 0 until mTickMarksX!!.size) {
            if (mShowTickText) {
                mTickTextsArr[i] = getTickTextByPosition(i)
                mTextPaint.getTextBounds(mTickTextsArr[i], 0, mTickTextsArr[i].length, mRect)
                mTickTextsWidth!![i] = mRect.width().toFloat()
                mTextCenterX!![i] = mPaddingLeft + mSeekBlockLength * i
            }
            mTickMarksX!![i] = mPaddingLeft + mSeekBlockLength * i
        }
    }

    private fun getTickTextByPosition(index: Int): String {
        if (mTickTextsCustomArray == null) return getProgressString(mProgressArr!![index])
        if (index < mTickTextsCustomArray!!.length) return mTickTextsCustomArray!![index].toString()
        return ""
    }

    private fun getProgressString(progress: Float): String {
        return progress.roundToInt().toString()
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
        mStockPaint.color = mProgressTrackColor
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
            if (mTickMarksDrawable != null) {
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

    private fun initTickMarksBitmap() {
        //
    }

    private fun getLeftSideTrackSize(): Int = mProgressTrackSize

    private fun getRightSideTrackSize(): Int = mBackgroundTrackSize

    private fun drawTickTexts(canvas: Canvas) {
        if (mTickTextsArr.isEmpty()) return
        val thumbPosFloat = getThumbPosOnTickFloat()
        for (i in mTickTextsArr.indices) {
            if (i == getThumbPosOnTick() && i == thumbPosFloat.toInt()) {
                mTextPaint.color = mHoveredTextColor
            } else if (i < thumbPosFloat) {
                mTextPaint.color = getLeftSideTickTextsColor()
            } else {
                mTextPaint.color = getRightSideTickTextsColor()
            }
            when (i) {
                0 -> {
                    canvas.drawText(
                        mTickTextsArr[i],
                        mTextCenterX!![i] + mTickTextsWidth!![i] / 2F,
                        mTickTextY,
                        mTextPaint
                    )
                }
                mTickTextsArr.size - 1 -> {
                    canvas.drawText(
                        mTickTextsArr[i],
                        mTextCenterX!![i] - mTickTextsWidth!![i] / 2.0F,
                        mTickTextY,
                        mTextPaint
                    )
                }
                else -> {
                    canvas.drawText(mTickTextsArr[i], mTextCenterX!![i], mTickTextY, mTextPaint)
                }
            }
        }
    }

    private fun getLeftSideTickTextsColor(): Int = mSelectedTextsColor

    private fun getRightSideTickTextsColor(): Int = mUnselectedTextsColor

    private fun drawThumb(canvas: Canvas) {
        val thumbCenterX = getThumbCenterX()
        if (mThumbDrawable != null) {
            if (mThumbBitmap == null || mPressedThumbBitmap == null) {
                initThumbBitmap()
            }
        }
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