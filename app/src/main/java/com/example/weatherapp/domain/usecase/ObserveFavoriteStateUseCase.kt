package com.example.weatherapp.domain.usecase

import com.example.weatherapp.domain.repository.FavouriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFavoriteStateUseCase @Inject constructor(private val repository: FavouriteRepository) {

    operator fun invoke(cityId: Int) = repository.observeIsFavourite(cityId)
}