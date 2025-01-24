package com.example.weatherapp.data.repository

import com.example.weatherapp.data.mapper.toDbModel
import com.example.weatherapp.data.mapper.toEntities
import com.example.weatherapp.data.model.db.FavouriteCitiesDao
import com.example.weatherapp.domain.entity.City
import com.example.weatherapp.domain.repository.FavouriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retry
import javax.inject.Inject

class FavouriteRepositoryImpl @Inject constructor(
    private val favouriteCitiesDao: FavouriteCitiesDao
) : FavouriteRepository {

    override val favouriteCities: Flow<List<City>>
        get() = favouriteCitiesDao.getFavouriteCities().map {
            it.toEntities()
        }.retry(3)

    override fun observeIsFavourite(cityId: Int): Flow<Boolean> =
        favouriteCitiesDao.observeIsFavourite(cityId)


    override suspend fun addToFavourite(city: City) {
        favouriteCitiesDao.addCityToFavourite(city.toDbModel())
    }

    override suspend fun removeFromFavourite(cityId: Int) {
        favouriteCitiesDao.removeFromFavourite(cityId)
    }
}