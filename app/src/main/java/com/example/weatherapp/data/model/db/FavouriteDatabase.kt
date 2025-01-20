package com.example.weatherapp.data.model.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.weatherapp.data.model.CityDbModel

@Database(entities = [CityDbModel::class], version = 1, exportSchema = false)
abstract class FavouriteDatabase : RoomDatabase() {

    abstract fun favouriteCitiesDao(): FavouriteCitiesDao
}