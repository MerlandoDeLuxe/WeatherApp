package com.example.weatherapp.domain.repository

import com.example.weatherapp.domain.entity.City

interface SearchRepository {

    //Метод, который выполняет поиск по городам.
    //Т.к метод выполняется на сервере, данную функцию нужно сделать suspend
    suspend fun searchCity(query: String) : List<City>

}