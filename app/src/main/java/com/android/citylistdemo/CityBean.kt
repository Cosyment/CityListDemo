package com.android.citylistdemo

data class CityBean(
    var name: String? = null,
    var pinyin: String? = "",
    var provinceId: String? = null,
    var sort: String? = null,
    var isCurrent: Boolean = false,
    var isHot: Boolean = false,
    var areaList: ArrayList<String>? = null,
    var letter: String = "",
    var isCategoryFirstOne: Boolean = false,/*分组第一个*/
    var isCategoryLastOne: Boolean = false,/*分组的最后一个*/
    var position: Int = 0,
    var spanSize: Int = 1,/*gridlayout 使用，每条数据所占列*/
    var isTopThree: Boolean = false/*gridlayout 使用，每组前三*/
) {
    companion object {
        const val VIEW_TYPE_CURRENT = 0
        const val VIEW_TYPE_HOT = 1
        const val VIEW_TYPE_ALL = 2
    }

    var viewType = 0

    constructor(name: String?, pinyin: String?) : this() {
        this.name = name
        this.pinyin = pinyin
        this.letter = PinyinUtil.getFirstLetter(pinyin)
    }
}