package com.example.weatherapp.domain.usecase

import com.example.weatherapp.domain.entity.City
import com.example.weatherapp.domain.repository.FavouriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavouriteCitiesUseCase @Inject constructor(private val repository: FavouriteRepository) {

    operator fun invoke(): Flow<City> {
        return repository.favouriteCities
    }
}