package com.android.citylistdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        citiesView.setCurrentCity("深圳")

        citiesView.setItems(
            arrayListOf(
                CityBean(name = "北京"),
                CityBean(name = "上海"),
                CityBean(name = "广州"),
                CityBean(name = "深圳"),
                CityBean(name = "武汉"),
                CityBean(name = "重庆"),
                CityBean(name = "成都")
            ), CityDBManager.getInstance().allCities
        )

        citiesView.setOnItemClickListener {
            Log.e("TAG","it ${it}")
        }
    }

}