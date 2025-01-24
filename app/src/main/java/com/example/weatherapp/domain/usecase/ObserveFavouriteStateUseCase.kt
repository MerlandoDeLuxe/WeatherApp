package com.example.weatherapp.domain.usecase

import com.example.weatherapp.domain.repository.FavouriteRepository
import javax.inject.Inject

class ObserveFavouriteStateUseCase @Inject constructor(private val repository: FavouriteRepository) {

    operator fun invoke(cityId: Int) = repository.observeIsFavourite(cityId)
}