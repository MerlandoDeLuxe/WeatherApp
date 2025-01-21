package com.example.weatherapp.presentation.favorite

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.example.weatherapp.domain.entity.City
import com.example.weatherapp.domain.usecase.GetCurrentWeatherUseCase
import com.example.weatherapp.domain.usecase.GetFavouriteCitiesUseCase
import com.example.weatherapp.presentation.favorite.FavoriteStore.Intent
import com.example.weatherapp.presentation.favorite.FavoriteStore.Label
import com.example.weatherapp.presentation.favorite.FavoriteStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

interface FavoriteStore : Store<Intent, State, Label> {

    sealed interface Intent {
        //Что может делать пользователь:
        //1. Кликать по кнопке Поиск
        //2. Кликать по кнопке Добавить город в избранное
        //3. Кликать по городу
        data object ClickSearch : Intent
        data object ClickToFavourite : Intent
        data class CityItemClick(val city: City) : Intent
    }

    //Никакие эти действия не будут менять стейт экрана, при клике на разные элементы выполняется навигация
    //поэтому создаем такие же лейблы
    sealed interface Label {
        data object ClickSearch : Label
        data object ClickToFavourite : Label
        data class CityItemClick(val city: City) : Label
    }

    data class State(
        val cityItems: List<CityItem>
    ) {

        data class CityItem(
            val city: City,
            val weatherState: WeatherState
        )

        //погода может быть в процессе загрузки из интернета или при загрузке произойти ошибка
        //поэтому нужно добавить стейты для неё
        sealed interface WeatherState {

            data object InitialState : WeatherState
            data object Loading : WeatherState
            data object Error : WeatherState
            data class Load(
                val tempC: Float,
                val iconUrl: String
            ) : WeatherState
        }
    }

}

class FavoriteStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val getFavouriteCitiesUseCase: GetFavouriteCitiesUseCase,
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase
) {

    fun create(): FavoriteStore =
        object : FavoriteStore, Store<Intent, State, Label> by storeFactory.create(
            name = "FavoriteStore",
            initialState = State(
                cityItems = listOf()
            ),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    //На стейт этого экрана не будут влиять действия пользователя
    //На стейт этого экрана будут влиять данные загрузки из репозитория
    //поэтому здесь понадобятся Action
    private sealed interface Action {

        //При старте экрана мы загрузим список любимых городов из базы
        data class FavouriteCitiesLoaded(val cities: List<City>) : Action
    }

    private sealed interface Msg {
        //1. Когда список городов будет загружен, его надо будет отобразить
        data class FavouriteCitiesLoaded(val cities: List<City>) : Msg

        //2. Когда стартанет загрузка данных для какого-то города, данные поменяются, нужно будет отобразить ProgressBar
        data class WeatherIsLoading(
            val cityId: Int
        ) : Msg

        //3. После того как погода будет загружена, нужно будет отобразить эту погоду
        data class WeatherLoaded(
            val cityId: Int,
            val tempC: Float,
            val conditionUrl: String
        ) : Msg

        //4. Отобразить какую-то ошибку, если данные загружены некорректно
        data class WeatherLoadingError(
            val cityId: Int
        ) : Msg

    }

    //Теперь в Bootstrapper мы подпишемся на Flow из репозитория,
    // в какой-то момент любимые города будут загружены
    // и в Executor мы стартуем загрузку погоды
    // и у нас получится много действий (Msg), которые могут изменить состояние экрана
    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            scope.launch {
                getFavouriteCitiesUseCase().collect {
                    dispatch(Action.FavouriteCitiesLoaded(it))
                }
            }
        }
    }

    //Загрузка погоды для каждого любимого города в Executor
    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                //Интенты никак не меняют стейт экрана, просто отправляем лейбл
                is Intent.CityItemClick -> {
                    publish(Label.CityItemClick(intent.city))
                }

                Intent.ClickSearch -> {
                    publish(Label.ClickSearch)
                }

                Intent.ClickToFavourite -> {
                    publish(Label.ClickToFavourite)
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when (action) {
                is Action.FavouriteCitiesLoaded -> {
                    val cities = action.cities
                    dispatch(Msg.FavouriteCitiesLoaded(cities))
                    //После того, как список любимых городов был получен,
                    //для каждого нужно стартовать загрузку погоды
                    cities.forEach {
                        scope.launch {
                            loadWeatherForCity(it)
                        }
                    }
                }
            }
        }

        private suspend fun loadWeatherForCity(city: City) {
            //Как только мы стартанем загрузку, нужно отправить сообщение, что для этого города была начата загрузка данных
            dispatch(Msg.WeatherIsLoading(cityId = city.id))

            //Начинаем загрузку погоды
            try {
                val weather = getCurrentWeatherUseCase(cityId = city.id)
                //Когда загрузка будет завершена, нужно будет отправить сообщение
                dispatch(
                    Msg.WeatherLoaded(
                        cityId = city.id,
                        tempC = weather.tempC,
                        conditionUrl = weather.conditionUrl
                    )
                )
            } catch (e: Exception) {
                //Если загрузка завершилась неуспешно, то нужно отправить сообщение об ошибке
                dispatch(Msg.WeatherLoadingError(cityId = city.id))
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State {
            return when (msg) {
                is Msg.FavouriteCitiesLoaded -> {
                    //Копировать нужно всегда
                    val cityItems = this.copy().cityItems.map {
                        State.CityItem(
                            city = it.city,
                            weatherState = State.WeatherState.InitialState
                        )
                    }
                    State(
                        cityItems = cityItems
                    )
                }

                is Msg.WeatherIsLoading -> {
                    //Если приходит сообщение, что для какого-то города погода загружается
                    //то нужно пройтись по всему списку городов и для того, у кого загружается погода, поменять State
                    val cityItems = this.copy().cityItems.map {
                        if (it.city.id == msg.cityId) {
                            it.copy(weatherState = State.WeatherState.Loading)
                        } else {
                            it
                        }
                    }
                    State(
                        cityItems = cityItems
                    )
//                    State(
//                        cityItems = this.cityItems.map {
//                            if (it.city.id == msg.cityId) {
//                                it.copy(weatherState = State.WeatherState.Loading)
//                            } else {
//                                it
//                            }
//                        }
//                    )
                }

                is Msg.WeatherLoaded -> {
                    val cityItems = this.copy().cityItems.map {
                        if (it.city.id == msg.cityId) {
                            it.copy(
                                weatherState = State.WeatherState.Load(
                                    tempC = msg.tempC,
                                    iconUrl = msg.conditionUrl
                                )
                            )
                        } else {
                            it
                        }
                    }
                    State(cityItems = cityItems)
                }

                is Msg.WeatherLoadingError -> {
                    val cityItems = this.copy().cityItems.map {
                        if (it.city.id == msg.cityId) {
                            it.copy(weatherState = State.WeatherState.Error)
                        } else {
                            it
                        }
                    }
                    State(cityItems = cityItems)
                }
            }
        }
    }
}
