package com.example.weatherapp.data.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherapp.data.model.CityDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteCitiesDao {

    @Query("select * from favourite_cities")
    fun getFavouriteCities(): Flow<List<CityDbModel>>
    //Получение списка избранных городов
    //На этот объект мы будем подписываться, поэтому возвращаемый тип Flow

    @Query("select exists (select * from favourite_cities where id =:cityId limit 1)")
    fun observeIsFavorite(cityId: Int): Flow<Boolean>
    //Метод также возвращает Flow: присутствует объект в базе данных или нет
    //exists возвращает true или false

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCityToFavourite(city: CityDbModel)

    @Query("delete from favourite_cities where id =:cityId")
    suspend fun removeFromFavourite(cityId: Int)

}