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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainModule {

    @Provides
    @Singleton
    fun provideFavouriteRepository(impl: FavouriteRepositoryImpl): FavouriteRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideSearchRepository(impl: SearchRepositoryImpl): SearchRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return ApiFactory.apiService
    }

    @Provides
    @Singleton
    fun provideWeatherAppDatabase(application: Application): FavouriteDatabase {
        return Room.databaseBuilder(
            application,
            FavouriteDatabase::class.java,
            DB_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFavouriteCitiesDao(database: FavouriteDatabase): FavouriteCitiesDao {
        return database.favouriteCitiesDao()
    }

    private const val DB_NAME = "weather_db"

}