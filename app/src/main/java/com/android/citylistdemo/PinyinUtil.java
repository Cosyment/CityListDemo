package com.android.citylistdemo;

import android.text.TextUtils;

import java.util.regex.Pattern;

public class PinyinUtil {
    /**
     * 获取拼音的首字母（大写）
     *
     * @param pinyin
     * @return
     */
    public static String getFirstLetter(final String pinyin) {
        if (TextUtils.isEmpty(pinyin)) return "#";
        String c = pinyin.substring(0, 1);
        Pattern pattern = Pattern.compile("^[A-Za-z]+$");
        if (pattern.matcher(c).matches()) {
            return c.toUpperCase();
        } else if ("#".equals(c)) {
            return "#";
        }
//        else if ("0".equals(c)){
//            return "定位";
//        } else if ("1".equals(c)){
//            return "热门";
//        }
        return pinyin;
    }
}
