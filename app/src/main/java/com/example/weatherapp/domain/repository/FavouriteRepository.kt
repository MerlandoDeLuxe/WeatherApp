package com.example.weatherapp.domain.repository

import com.example.weatherapp.domain.entity.City
import kotlinx.coroutines.flow.Flow

interface FavouriteRepository {

    //Метод, возвращающий список избранных городов
    //Должна быть возможность на него подписаться, чтобы получать обновления
    val favouriteCities: Flow<List<City>>

    //Возвращает добавлен ли город в избранное или нет
    //Flow - чтобы была возможность на этот объект подписаться
    fun observeIsFavourite(cityId: Int) : Flow<Boolean>

    //Методы чтобы добавлять и удалять из избранного
    //Эту информацию мы будем сохранять в базу, такую работу нужно выносить в фоновый поток
    suspend fun addToFavourite(city: City)

    suspend fun removeFromFavourite(cityId: Int)
}