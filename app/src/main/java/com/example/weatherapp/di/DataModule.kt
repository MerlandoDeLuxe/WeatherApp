package com.example.weatherapp.di

import android.app.Application
import androidx.room.Room
import com.example.weatherapp.data.model.db.FavouriteCitiesDao
import com.example.weatherapp.data.model.db.FavouriteDatabase
import com.example.weatherapp.data.network.api.ApiFactory
import com.example.weatherapp.data.network.api.ApiService
import com.example.weatherapp.data.repository.FavouriteRepositoryImpl
import com.example.weatherapp.data.repository.SearchRepositoryImpl
import com.example.weatherapp.data.repository.WeatherRepositoryImpl
import com.example.weatherapp.domain.repository.FavouriteRepository
import com.example.weatherapp.domain.repository.SearchRepository
import com.example.weatherapp.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface DataModule {

    @Binds
    @ApplicationScope
    fun bindFavouriteRepository(impl: FavouriteRepositoryImpl): FavouriteRepository

    @Binds
    @ApplicationScope
    fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    @Binds
    @ApplicationScope
    fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    companion object {

        @Provides
        @ApplicationScope
        fun provideApiService(): ApiService {
            return ApiFactory.apiService
        }

        @Provides
        @ApplicationScope
        fun provideWeatherAppDatabase(application: Application): FavouriteDatabase {
            return Room.databaseBuilder(
                application,
                FavouriteDatabase::class.java,
                DB_NAME
            ).fallbackToDestructiveMigration()
                .build()
        }


        @Provides
        @ApplicationScope
        fun provideFavouriteCitiesDao(database: FavouriteDatabase): FavouriteCitiesDao {
            return database.favouriteCitiesDao()
        }

        private const val DB_NAME = "weather_db"
    }

}