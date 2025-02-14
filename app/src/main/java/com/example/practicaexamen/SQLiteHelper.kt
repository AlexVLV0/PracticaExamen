package com.example.practicaexamen

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "bares_db"
        const val DATABASE_VERSION = 1

        const val TABLE_NAME = "bares"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_ADDRESS = "address"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_WEBSITE = "website"
        const val COLUMN_RATING = "rating"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAME TEXT, " +
                "$COLUMN_ADDRESS TEXT, " +
                "$COLUMN_LATITUDE REAL, " +
                "$COLUMN_LONGITUDE REAL, " +
                "$COLUMN_WEBSITE TEXT, " +
                "$COLUMN_RATING REAL)"
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(dropTableQuery)
        onCreate(db)
    }

    fun insertBar(bar: Bar) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, bar.name)
        values.put(COLUMN_ADDRESS, bar.address)
        values.put(COLUMN_LATITUDE, bar.latitude)
        values.put(COLUMN_LONGITUDE, bar.longitude)
        values.put(COLUMN_WEBSITE, bar.website)
        values.put(COLUMN_RATING, bar.rating)
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    @SuppressLint("Range")
    fun getAllBars(): List<Bar> {
        val bars = mutableListOf<Bar>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                val address = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS))
                val latitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE))
                val longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE))
                val website = cursor.getString(cursor.getColumnIndex(COLUMN_WEBSITE))
                val rating = cursor.getFloat(cursor.getColumnIndex(COLUMN_RATING))
                val bar = Bar(id, name, address, latitude, longitude, website, rating)
                bars.add(bar)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return bars
    }

    fun updateBar(bar: Bar) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, bar.name)
        values.put(COLUMN_ADDRESS, bar.address)
        values.put(COLUMN_LATITUDE, bar.latitude)
        values.put(COLUMN_LONGITUDE, bar.longitude)
        values.put(COLUMN_WEBSITE, bar.website)
        values.put(COLUMN_RATING, bar.rating)
        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(bar.id.toString()))
        db.close()
    }

    fun deleteBar(barId: Int) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(barId.toString()))
        db.close()
    }
}