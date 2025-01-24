package com.example.weatherapp.presentation.details

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.example.weatherapp.domain.entity.City
import com.example.weatherapp.domain.entity.Forecast
import com.example.weatherapp.domain.usecase.ChangeFavouriteStateUseCase
import com.example.weatherapp.domain.usecase.GetForecastUseCase
import com.example.weatherapp.domain.usecase.ObserveFavouriteStateUseCase
import com.example.weatherapp.presentation.details.DetailsStore.Intent
import com.example.weatherapp.presentation.details.DetailsStore.Label
import com.example.weatherapp.presentation.details.DetailsStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

interface DetailsStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data object ClickBack : Intent
        data object ChangeFavouriteStatusClick : Intent
    }

    data class State(
        val city: City,
        val isFavourite: Boolean,
        val forecastState: ForecastState
    )

    sealed interface ForecastState {
        data object Initial : ForecastState
        data object Loading : ForecastState
        data class Loaded(val forecast: Forecast) : ForecastState
        data object Error : ForecastState
    }

    sealed interface Label {
        //При клике назад - это одноразовое действие, поэтому добавляем его в лейбл
        data object ClickBack : Label
    }
}

class DetailsStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val getForecastUseCase: GetForecastUseCase,
    private val changeFavouriteStateUseCase: ChangeFavouriteStateUseCase,
    private val observeFavouriteStateUseCase: ObserveFavouriteStateUseCase
) {

    fun create(city: City): DetailsStore =
        object : DetailsStore, Store<Intent, State, Label> by storeFactory.create(
            name = "DetailsStore",
            initialState = State(
                city,
                isFavourite = false,
                forecastState = DetailsStore.ForecastState.Initial
            ),
            bootstrapper = BootstrapperImpl(city),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {
        //Что нам отправляет репозиторий
        data class FavouriteStatusChange(val isFavourite: Boolean) : Action
        data class ForecastLoaded(val forecast: Forecast) : Action
        data object ForecastStartLoading : Action
        data object ForecastLoadingError : Action
    }

    private sealed interface Msg {
        //Все эти действия будут менять стейт экрана, потому добавляем их в Msg
        data class FavouriteStatusChange(val isFavourite: Boolean) : Msg
        data class ForecastLoaded(val forecast: Forecast) : Msg
        data object ForecastStartLoading : Msg
        data object ForecastLoadingError : Msg
    }

    private inner class BootstrapperImpl(private val city: City) : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            scope.launch {
                observeFavouriteStateUseCase(cityId = city.id).collect {
                    dispatch(Action.FavouriteStatusChange(it))
                }
            }
            scope.launch {
                dispatch(Action.ForecastStartLoading)
                try {
                    val forecast = getForecastUseCase(cityId = city.id)
                    dispatch(Action.ForecastLoaded(forecast))
                } catch (e: Exception) {
                    dispatch(Action.ForecastLoadingError)
                }
            }
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                Intent.ChangeFavouriteStatusClick -> {
                    scope.launch {
                        //Перед тем как менять статус, нужно определить его текущий статус. Можно взять его из стейта
                        val state = getState()
                        if (state.isFavourite) {
                            changeFavouriteStateUseCase.removeFromFavourite(cityId = state.city.id)
                        } else {
                            changeFavouriteStateUseCase.addToFavourite(city = state.city)
                        }
                    }
                }

                Intent.ClickBack -> {
                    publish(Label.ClickBack)
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when (action) {
                //Здесь нужно просто пробросить дальше сообщение, что стейт экрана поменялся
                is Action.FavouriteStatusChange -> {
                    dispatch(Msg.FavouriteStatusChange(action.isFavourite))
                }

                is Action.ForecastLoaded -> {
                    dispatch(Msg.ForecastLoaded(action.forecast))
                }

                Action.ForecastLoadingError -> {
                    dispatch(Msg.ForecastLoadingError)
                }

                Action.ForecastStartLoading -> {
                    dispatch(Msg.ForecastStartLoading)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.FavouriteStatusChange -> {
                    copy(isFavourite = msg.isFavourite)
                }

                is Msg.ForecastLoaded -> {
                    copy(forecastState = DetailsStore.ForecastState.Loaded(msg.forecast))
                }

                Msg.ForecastLoadingError -> {
                    copy(forecastState = DetailsStore.ForecastState.Error)
                }

                Msg.ForecastStartLoading -> {
                    copy(forecastState = DetailsStore.ForecastState.Loading)
                }
            }
    }
}