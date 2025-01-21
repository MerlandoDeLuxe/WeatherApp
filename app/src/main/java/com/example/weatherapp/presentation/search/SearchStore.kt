package com.example.weatherapp.presentation.search

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.example.weatherapp.domain.entity.City
import com.example.weatherapp.domain.usecase.ChangeFavouriteStateUseCase
import com.example.weatherapp.domain.usecase.SearchCityUseCase
import com.example.weatherapp.presentation.search.SearchStore.Intent
import com.example.weatherapp.presentation.search.SearchStore.Label
import com.example.weatherapp.presentation.search.SearchStore.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

interface SearchStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class ChangeSearchQuery(val searchQuery: String) : Intent

        data object ClickBack : Intent

        data object SearchClick : Intent

        data class ClickCity(val city: City) :
            Intent //Если какой-то город был найден в поиске, то пользователь может по нему кликнуть
    }

    data class State(
        val searchQuery: String,
//        val openReason: OpenReason,   //При открытии это состояние меняться не будет, его можно передать разово при открытии в фабрику
        val searchState: SearchState
    )

    sealed interface SearchState {
        data object Initial : SearchState
        data object Loading : SearchState
        data object Error : SearchState
        data object EmptyResult : SearchState //Если в результате поиска такого города не найдено
        data class SuccessLoaded(val cities: List<City>) : SearchState
    }

    sealed interface Label {
        data object ClickBack : Label

        //В зависимости от того, какая была причина открытия данного экрана
        //мы либо переходим в окно детальной информации, либо добавляем этот городв  избранное и закрываем экран
        data object SavedToFavourite : Label
        data class OpenForecast(val city: City) : Label
    }
}

class SearchStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val searchCityUseCase: SearchCityUseCase,
    private val changeFavouriteStateUseCase: ChangeFavouriteStateUseCase //Этот usecase нужен в случае,
    // если экран поиска был открыт для того чтобы добавить город в избранное
) {

    //В метод create() будем передавать причину открытия данного экрана
    fun create(openReason: OpenReason): SearchStore =
        object : SearchStore, Store<Intent, State, Label> by storeFactory.create(
            name = "SearchStore",
            initialState = State(
                searchQuery = "",
                searchState = SearchStore.SearchState.Initial
            ),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl(openReason = openReason) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Action

    private sealed interface Msg {
        //Месседжи, которые меняют стейт экрана - начало загрузки, успешна или неуспешная
        data class ChangeSearchQuery(val searchQuery: String) : Msg

        data object LoadingSearchResult : Msg

        data object SearchResultError : Msg

        data class SearchResultLoaded(val cities: List<City>) : Msg

    }

    //В Bootstrappere нам не нужно ни на что подписываться и соответственно нам не нужно будет отправлять никаких Action
    //Оставляем интерфейс Action пустым
    private class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
        }
    }

    private inner class ExecutorImpl(private val openReason: OpenReason) :
        CoroutineExecutor<Intent, Action, State, Msg, Label>() {

        private var searchJob: Job? = null

        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                Intent.ClickBack -> {
                    publish(Label.ClickBack)
                }

                is Intent.ClickCity -> {
                    when (openReason) {
                        OpenReason.AddToFavourite -> {
                            scope.launch {
                                changeFavouriteStateUseCase.addToFavourite(intent.city)
                                publish(Label.SavedToFavourite)
                            }
                        }

                        OpenReason.RegularSearch -> {
                            publish(Label.OpenForecast(intent.city))
                        }
                    }
                }

                Intent.SearchClick -> {
                    //чтобы при очередном клике на поиске, пока поиск еще не закончен, отменять старый поиск и стартовать новый,
                    //нужно отменить старый Job
                    searchJob?.cancel()
                    searchJob = scope.launch {
                        dispatch(Msg.LoadingSearchResult)
                        try {
                            val state = getState()
                            val cities = searchCityUseCase(state.searchQuery)
                            dispatch(Msg.SearchResultLoaded(cities = cities))
                        } catch (e: Exception) {
                            dispatch(Msg.SearchResultError)
                        }
                    }
                }

                is Intent.ChangeSearchQuery -> {
                    dispatch(Msg.ChangeSearchQuery(intent.searchQuery))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.ChangeSearchQuery -> {
                    copy(searchQuery = msg.searchQuery)
                }

                Msg.LoadingSearchResult -> {
                    copy(searchState = SearchStore.SearchState.Loading)
                }

                Msg.SearchResultError -> {
                    copy(searchState = SearchStore.SearchState.Error)
                }

                is Msg.SearchResultLoaded -> {
                    val searchState = if (msg.cities.isEmpty()) {
                        SearchStore.SearchState.EmptyResult
                    } else {
                        SearchStore.SearchState.SuccessLoaded(msg.cities)
                    }
                    copy(searchState = searchState)
                }
            }
    }
}
