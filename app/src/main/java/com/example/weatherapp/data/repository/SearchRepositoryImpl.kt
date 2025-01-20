package com.example.weatherapp.data.repository

import com.example.weatherapp.data.mapper.toEntities
import com.example.weatherapp.data.model.db.FavouriteCitiesDao
import com.example.weatherapp.data.network.api.ApiFactory
import com.example.weatherapp.data.network.api.ApiService
import com.example.weatherapp.domain.entity.City
import com.example.weatherapp.domain.repository.SearchRepository
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val apiService: ApiService
): SearchRepository {

    override suspend fun searchCity(query: String): List<City> {
        return apiService.searchCity(query).toEntities()
    }
}