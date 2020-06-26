package com.android.citylistdemo.citiesview

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.citylistdemo.BR
import com.android.citylistdemo.CityBean
import com.android.citylistdemo.R
import com.android.citylistdemo.databinding.LayoutCityListBinding


class CityView : RelativeLayout {

    companion object {
        const val CITY_LIST_STYLE_LINEAR = 0
        const val CITY_LIST_STYLE_GRID = 1
    }

    private val DEFAULT_GRID_COLUMN_COUNT = 3
    private var mTitleColor: Int = android.graphics.Color.parseColor("#333333")
    private var mTitleSize = 15F
    private var mTitleBackground: Int = android.graphics.Color.parseColor("#FFFFFF")
    private var mTextColor = android.graphics.Color.parseColor("#333333")
    private var mTextSize = 14F
    private var mSideLetterColor = android.graphics.Color.parseColor("#333333")
    private var mSideLetterSize = 14F
    private var mLayoutManager: Int
    private var mCurrentCity = ObservableField<CityBean>()/*当前城市*/
    private val mHotItems = ObservableArrayList<CityBean>()/*热门城市*/
    private val mItems = ArrayList<CityBean>()/*所有城市列表*/
    private var mOnItemClickListener: (CityBean) -> Unit = {}
    private var mLetterIndex = HashMap<String, Int>()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    ) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CityView)
        mTitleColor = typedArray.getColor(R.styleable.CityView_title_color, mTitleColor)
        mTitleSize = typedArray.getDimension(
            R.styleable.CityView_title_size, TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                15F,
                context.resources.displayMetrics
            )
        )
        mTitleBackground =
            typedArray.getColor(R.styleable.CityView_title_background, mTitleBackground)
        mTextColor = typedArray.getColor(R.styleable.CityView_android_textColor, mTextColor)
        mTextSize = typedArray.getDimension(
            R.styleable.CityView_android_textSize,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                15F,
                context.resources.displayMetrics
            )
        )
        mSideLetterColor =
            typedArray.getColor(R.styleable.CityView_side_letter_color, mSideLetterColor)
        mSideLetterSize =
            typedArray.getDimension(
                R.styleable.CityView_side_letter_size, TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    15F,
                    context.resources.displayMetrics
                )
            )
        mLayoutManager = typedArray.getInt(R.styleable.CityView_style, CITY_LIST_STYLE_LINEAR)
        initial(context)

        typedArray.recycle()
    }

    private var mBinding: LayoutCityListBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.layout_city_list,
        this,
        true
    )

    private fun initial(context: Context) {
        mBinding.apply {
            val layoutManager = GridLayoutManager(context, DEFAULT_GRID_COLUMN_COUNT)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager.spanSizeLookup =
                object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (mLayoutManager == 0 && mItems[position].viewType == CityBean.VIEW_TYPE_ALL) {
                            3
                        } else {
                            mItems[position].spanSize
                        }
                    }
                }
            mBinding.recyclerView.layoutManager = layoutManager

            letterBar.setTextColor(mSideLetterColor)
            letterBar.setTextSize(mSideLetterSize)

            letterBar.setOnLetterChangedListener {
                if ("#" == it) {
                    recyclerView.smoothScrollToPosition(0)
                } else {
                    val index = mLetterIndex[it]
                    if (index is Int && index < mItems.lastIndex) {
                        recyclerView.smoothScrollToPosition(index)
                    }
                }
            }
        }
    }

    fun setCurrentCity(city: String?) {
        mCurrentCity.set(CityBean(name = city, pinyin = "*", letter = "当前城市"))
    }

    fun setItems(hotItems: List<CityBean>?, items: List<CityBean>) {
        hotItems?.let { mHotItems.addAll(it) }

        mCurrentCity.get()?.let {
            mItems.add(
                it
            )
            mItems.first().viewType = CityBean.VIEW_TYPE_CURRENT
        }

        hotItems?.forEach {
            it.pinyin = "#"
            it.letter = "热门城市"
            it.isHot = true
            it.viewType = CityBean.VIEW_TYPE_HOT
        }
        mItems.addAll(hotItems!!)

        items.forEach { it.viewType = CityBean.VIEW_TYPE_ALL }

        mItems.addAll(items)

        var totalOffset = 0
        mItems.forEachIndexed { index, bean ->
            bean.position = index
            if (index == 0 || index > 0 && bean.letter != mItems[index - 1].letter) {
                bean.isCategoryFirstOne = true
                bean.isTopThree = true
                if (bean.isHot || mLayoutManager == CITY_LIST_STYLE_GRID) {
                    mItems[index + 1].isTopThree = true
                    mItems[index + 2].isTopThree = true
                }

                mLetterIndex[bean.letter] = index
            }

            if (index < items.lastIndex && bean.letter != mItems[index + 1].letter) {
                bean.isCategoryLastOne = true
                when ((index + totalOffset) % DEFAULT_GRID_COLUMN_COUNT) {
                    0 -> {
                        totalOffset += 2
                        bean.spanSize = DEFAULT_GRID_COLUMN_COUNT
                    }
                    1 -> {
                        totalOffset += 1
                        bean.spanSize = DEFAULT_GRID_COLUMN_COUNT - 1
                    }
                    else -> {
                        bean.spanSize = 1
                    }
                }
            }
        }

        val adapter = MultiViewTypeAdapter<ViewDataBinding>(
            R.layout.item_grid_city,
            R.layout.item_hot_city,
            if (mLayoutManager == CITY_LIST_STYLE_LINEAR) R.layout.item_linear_city else R.layout.item_grid_city,
            items = mItems
        )

        adapter.setOnItemClickListener { mOnItemClickListener.invoke(it) }

        mBinding.recyclerView.adapter = adapter

        mBinding.recyclerView.addItemDecoration(
            LetterItemDecoration(
                context,
                mItems,
                mTitleBackground, mTitleColor, mTitleSize
            )
        )
    }

    fun setOnItemClickListener(block: (CityBean) -> Unit) {
        mOnItemClickListener = block
    }

    inner class MultiViewTypeAdapter<B : ViewDataBinding>(
        private vararg val layoutIds: Int,
        private val items: List<CityBean>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var mOnItemClickListener: (CityBean) -> Unit = {}
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                layoutIds[viewType],
                parent,
                false
            )
            return BaseViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val binding = DataBindingUtil.bind<B>(holder.itemView)

            binding?.let {
                it.setVariable(BR.item, items[position])
                it.root.setOnClickListener { mOnItemClickListener(items[position]) }
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun getItemViewType(position: Int): Int {
            return items[position].viewType
        }

        fun setOnItemClickListener(block: (CityBean) -> Unit) {
            this.mOnItemClickListener = block
        }

        inner class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }
}