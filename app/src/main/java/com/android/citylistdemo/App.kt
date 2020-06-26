package com.android.citylistdemo

import android.app.Application

/**
 * @PackageName : com.android.citylistdemo
 * @Author : Waiting
 * @Date :   2020/6/25 12:42
 */
class App:Application() {


    companion object {
        private var INSTANCE: App? = null
        fun getInstance(): App {
            return INSTANCE!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }
}