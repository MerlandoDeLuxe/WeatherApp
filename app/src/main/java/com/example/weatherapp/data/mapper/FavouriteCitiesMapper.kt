package com.example.weatherapp.data.mapper

import com.example.weatherapp.data.model.CityDbModel
import com.example.weatherapp.domain.entity.City

fun City.toDbModel(): CityDbModel = CityDbModel(id = id, name = name, country = country)

fun CityDbModel.toEntity(): City = City(id = id, name = name, country = country)

fun List<CityDbModel>.toEntities(): List<City> = map {
    it.toEntity()
}