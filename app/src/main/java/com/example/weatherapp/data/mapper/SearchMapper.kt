package com.example.weatherapp.data.mapper

import com.example.weatherapp.data.network.dto.CityDto
import com.example.weatherapp.domain.entity.City

fun CityDto.toEntity(): City = City(id = id, name = name, country = country)

fun List<CityDto>.toEntities(): List<City> = map {
    it.toEntity()
}