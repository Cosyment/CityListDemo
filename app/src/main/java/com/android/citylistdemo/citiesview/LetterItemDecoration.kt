package com.android.citylistdemo.citiesview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.citylistdemo.CityBean


class LetterItemDecoration(
    context: Context,
    items: List<CityBean>,
    background: Int = android.graphics.Color.parseColor("#FFFFFF"),
    textColor: Int = android.graphics.Color.parseColor("#FF000000"),
    textSize: Float = 15F
) :
    RecyclerView.ItemDecoration() {

    private val DEFAULT_PADDING_LEFT = 20F
    private var mDatas: List<CityBean> = items
    private var mPaint: Paint = Paint()
    private var mBounds: Rect = Rect()

    private var mTitleHeight = 0

    private val TITLE_BG_COLOR: Int = background

    private val TITLE_TEXT_COLOR: Int = textColor

    init {
        mTitleHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            30f,
            context.resources.displayMetrics
        ).toInt()
        mPaint.textSize = textSize
        mPaint.isAntiAlias = true
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount
        if (mDatas.isEmpty()) return
        for (i in 0 until childCount) {
            val child: View = parent.getChildAt(i)
            val params = child
                .layoutParams as RecyclerView.LayoutParams
            val position = params.viewLayoutPosition
            if (position > -1) {
                if (position == 0) { //等于0的时候绘制title
                    drawTitle(c, left, right, child, params, position)
                } else {
                    if (mDatas[position]
                            .isCategoryFirstOne
                    ) {
                        //字母不为空，并且不等于前一个，也要title
                        drawTitle(c, left, right, child, params, position)
                    }
                }
            }
        }
    }

    /**
     * 绘制Title区域背景和文字的方法
     * 最先调用，绘制最下层的title
     * @param c
     * @param left
     * @param right
     * @param child
     * @param params
     * @param position
     */
    private fun drawTitle(
        c: Canvas,
        left: Int,
        right: Int,
        child: View,
        params: RecyclerView.LayoutParams,
        position: Int
    ) {
        if (mDatas.isEmpty()) return
        mPaint.color = TITLE_BG_COLOR
        c.drawRect(
            left.toFloat() + DEFAULT_PADDING_LEFT,
            (child.top - params.topMargin - mTitleHeight).toFloat(),
            right.toFloat() - DEFAULT_PADDING_LEFT,
            (child.top - params.topMargin).toFloat(),
            mPaint
        )
        mPaint.color = TITLE_TEXT_COLOR
        mPaint.getTextBounds(
            mDatas[position].letter,
            0,
            mDatas[position].letter.length,
            mBounds
        )
        c.drawText(
            mDatas[position].letter,
            child.paddingStart.toFloat() + DEFAULT_PADDING_LEFT * 3,
            (child.top - params.topMargin - (mTitleHeight / 2 - mBounds.height() / 2)).toFloat(),
            mPaint
        )
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (mDatas.isEmpty()) return
        val position =
            when (parent.layoutManager) {
                is LinearLayoutManager -> (parent.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                is GridLayoutManager -> (parent.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
                else -> -1
            }

        if (position == -1) return  //在搜索到没有的索引的时候position可能等于-1，所以在这里判断一下
        val tag: String = mDatas[position].letter
        val child: View = parent.findViewHolderForLayoutPosition(position)!!.itemView
        //Canvas是否位移过的标志
        var flag = false
        if (position + 1 < mDatas.size) {
            //当前第一个可见的Item的字母索引，不等于其后一个item的字母索引，说明悬浮的View要切换了
            if (tag != mDatas[position + 1].letter) {
                //当第一个可见的item在屏幕中剩下的高度小于title的高度时，开始悬浮Title的动画
                if (child.height + child.top < mTitleHeight) {
                    c.save()
                    flag = true
                    /**
                     * 下边的索引把上边的索引顶上去的效果
                     */
                    c.translate(0F, (child.height + child.top - mTitleHeight).toFloat())
                    /**
                     * 头部折叠起来的视效（下边的索引慢慢遮住上边的索引）
                     */
//                    c.clipRect(
//                        parent.paddingLeft,
//                        parent.paddingTop,
//                        parent.right - parent.paddingRight,
//                        parent.paddingTop + child.height + child.top
//                    )
                }
            }
        }
        mPaint.color = TITLE_BG_COLOR
        c.drawRect(
            parent.paddingLeft.toFloat() + DEFAULT_PADDING_LEFT,
            parent.paddingTop.toFloat(),
            (parent.right - parent.paddingRight).toFloat() - DEFAULT_PADDING_LEFT,
            (parent.paddingTop + mTitleHeight).toFloat(), mPaint
        )
        mPaint.color = TITLE_TEXT_COLOR
        mPaint.getTextBounds(tag, 0, tag.length, mBounds)
        c.drawText(
            tag,
            child.paddingStart + DEFAULT_PADDING_LEFT * 3,
            (parent.paddingTop + mTitleHeight - (mTitleHeight / 2 - mBounds.height() / 2)).toFloat(),
            mPaint
        )
        if (flag) c.restore() //恢复画布到之前保存的状态
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val position =
            (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
        if (mDatas.isEmpty())
            return
        if (position > -1) {
            //等于0的时候绘制title
            if (position == 0) {
                outRect.set(0, mTitleHeight, 0, 0)
            } else {
                if (mDatas[position].isCategoryFirstOne
                ) {
                    //字母不为空，并且不等于前一个，绘制title
                    outRect.set(0, mTitleHeight, 0, 0)
                } else {
                    if (parent.layoutManager is GridLayoutManager) {
                        outRect.set(
                            0,
                            if (mDatas[position].isTopThree) mTitleHeight else 0,
                            0,
                            0
                        )
                    } else {
                        outRect.set(0, 0, 0, 0)
                    }
                }
            }
        }
    }
}