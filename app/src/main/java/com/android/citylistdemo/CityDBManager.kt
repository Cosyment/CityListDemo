package com.android.citylistdemo

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.util.*

class CityDBManager private constructor(private val context: Context) {
    private var DB_PATH: String = (File.separator + "data"
            + Environment.getDataDirectory()
        .absolutePath + File.separator
            + context.packageName + File.separator + "databases" + File.separator)

    companion object {
        private var INSTANCE: CityDBManager? = null
        private const val ASSETS_NAME = "china_cities.db"
        private const val DB_NAME = ASSETS_NAME
        private const val TABLE_NAME = "city"
        private const val NAME = "name"
        private const val PINYIN = "pinyin"
        private const val BUFFER_SIZE = 1024

        fun getInstance(): CityDBManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: CityDBManager(App.getInstance()).also { INSTANCE = it }
            }
    }


    init {
        copyDBFile()
    }

    private fun copyDBFile() {
        val dir = File(DB_PATH)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val dbFile = File(DB_PATH + DB_NAME)
        if (!dbFile.exists()) {

            try {
                val `is` = context.resources.assets.open(ASSETS_NAME)
                `is`.use {
                    val os = FileOutputStream(dbFile)
                    val buffer = ByteArray(BUFFER_SIZE)
                    var length: Int
                    os.use {
                        while (`is`.read(buffer, 0, buffer.size).also { length = it } > 0) {
                            it.write(buffer, 0, length)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val allCities: ArrayList<CityBean>
        get() {
            val db =
                SQLiteDatabase.openOrCreateDatabase(DB_PATH + DB_NAME, null)
            val result = ArrayList<CityBean>()
            db.use {
                val cursor = it.rawQuery("select * from $TABLE_NAME", null)
                cursor.use {
                    while (cursor.moveToNext()) {
                        val name =
                            cursor.getString(cursor.getColumnIndex(NAME))
                        val pinyin =
                            cursor.getString(cursor.getColumnIndex(PINYIN))
                        result.add(CityBean(name, pinyin))
                    }
                }
            }
            Collections.sort(result, CityComparator())
            return result
        }

    fun searchCity(keyword: String): List<CityBean> {
        val db =
            SQLiteDatabase.openOrCreateDatabase(DB_PATH + DB_NAME, null)
        val result: MutableList<CityBean> = ArrayList()

        db.use {
            val cursor = it.rawQuery(
                "select * from " + TABLE_NAME + " where name like \"%" + keyword
                        + "%\" or pinyin like \"%" + keyword + "%\"", null
            )
            cursor.use {
                while (cursor.moveToNext()) {
                    val name = cursor.getString(cursor.getColumnIndex(NAME))
                    val pinyin = cursor.getString(cursor.getColumnIndex(PINYIN))
                    result.add(CityBean(name, pinyin))
                }
            }
        }
        Collections.sort(result, CityComparator())
        return result
    }

    /**
     * sort by a-z
     */
    private inner class CityComparator : Comparator<CityBean> {
        override fun compare(lhs: CityBean, rhs: CityBean): Int {
            val a = lhs.pinyin!!.substring(0, 1)
            val b = rhs.pinyin!!.substring(0, 1)
            return a.compareTo(b)
        }
    }
}