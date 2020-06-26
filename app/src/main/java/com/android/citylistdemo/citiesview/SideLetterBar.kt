package com.android.citylistdemo.citiesview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.android.citylistdemo.R

class SideLetterBar : View {

    private val LETTERS = arrayOf("#", "A", "B", "C", "D", "E", "F","G","H","I","J","K","L","M","N", "O","P","Q", "R","S","T","U", "V","W","X","Y","Z")
    private var mChoose = -1
    private val mPaint = Paint()
    private var mShowBackground = true
    private var mTextColor :Int
    private var mTextSize :Float
    private var onLetterChangedListener: (String) -> Unit = {}
    private var mOverlay: TextView? = null

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle)

    init {
        mTextColor = ContextCompat.getColor(context, R.color.colorAccent)
        mTextSize =TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            15f,
            context.resources.displayMetrics
        )
    }

    /**
     * 设置悬浮的textview
     *
     * @param overlay
     */
    fun setOverlay(overlay: TextView?) {
        this.mOverlay = overlay
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mShowBackground) {
            canvas.drawColor(Color.TRANSPARENT)
        }
        val height = height
        val width = width
        val singleHeight = height / LETTERS.size
        for (i in LETTERS.indices) {
            mPaint.textSize = mTextSize
            mPaint.color = mTextColor
            mPaint.isAntiAlias = true
            if (i == mChoose) {
                mPaint.color = ContextCompat.getColor(context, R.color.colorAccent)
                mPaint.isFakeBoldText = true //加粗
            }
            val xPos =
                width / 2 - mPaint.measureText(LETTERS[i]) / 2
            val yPos = singleHeight * i + singleHeight.toFloat()
            canvas.drawText(LETTERS[i], xPos, yPos, mPaint)
            mPaint.reset()
        }
    }

    fun setTextColor(color:Int){
        mTextColor = color
        invalidate()
    }

    fun setTextSize(size:Float){
        mTextSize = size
        invalidate()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        val y = event.y
        val oldChoose = mChoose
        val listener = onLetterChangedListener
        val c = (y / height * LETTERS.size).toInt()
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mShowBackground = true
                if (oldChoose != c) {
                    if (c >= 0 && c < LETTERS.size) {
                        listener.invoke(LETTERS[c])
                        mChoose = c
                        invalidate()
                        mOverlay?.visibility = VISIBLE
                        mOverlay?.text = LETTERS[c]
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> if (oldChoose != c) {
                if (c >= 0 && c < LETTERS.size) {
                    listener.invoke(LETTERS[c])
                    mChoose = c
                    invalidate()
                    mOverlay?.visibility = VISIBLE
                    mOverlay?.text = LETTERS[c]
                }
            }
            MotionEvent.ACTION_UP -> {
                mShowBackground = false
                mChoose = -1
                invalidate()
                    mOverlay?.visibility = GONE
            }
        }
        return true
    }

    fun setOnLetterChangedListener(block: (String) -> Unit) {
        this.onLetterChangedListener = block
    }
}