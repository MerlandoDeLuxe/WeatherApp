package com.example.weatherapp.presentation.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.example.weatherapp.domain.entity.City
import com.example.weatherapp.presentation.details.DefaultDetailsComponent
import com.example.weatherapp.presentation.favourite.DefaultFavouriteComponent
import com.example.weatherapp.presentation.search.DefaultSearchComponent
import com.example.weatherapp.presentation.search.OpenReason
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable

class DefaultRootComponent @AssistedInject constructor(
    private val detailsComponentFactory: DefaultDetailsComponent.Factory,
    private val favouriteComponentFactory: DefaultFavouriteComponent.Factory,
    private val searchComponentFactory: DefaultSearchComponent.Factory,
    @Assisted("componentContext") val componentContext: ComponentContext
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>>
        get() = childStack(
            source = navigation,
            initialConfiguration = Config.Favourite,
            handleBackButton = true,
            serializer = Config.serializer(),
            childFactory = ::child
        )

    @OptIn(DelicateDecomposeApi::class)
    private fun child(
        config: Config,
        componentContext: ComponentContext
    ): RootComponent.Child {
        return when (config) {
            is Config.Details -> {
                val component = detailsComponentFactory.create(
                    onBackClicked = {
                        navigation.pop()
                    },
                    city = config.city,
                    componentContext = componentContext
                )
                RootComponent.Child.Details(component)
            }

            Config.Favourite -> {
                val component = favouriteComponentFactory.create(
                    onCityItemClicked = {
                        navigation.push(Config.Details(it))
                    },
                    onSearchClick = {
                        navigation.push(Config.Search(openReason = OpenReason.RegularSearch))
                    },
                    onAddToFavouriteClick = {
                        navigation.push(Config.Search(openReason = OpenReason.AddToFavourite))
                    },
                    componentContext = componentContext
                )
                RootComponent.Child.Favourite(component)
            }

            is Config.Search -> {
                val component = searchComponentFactory.create(
                    onForecastForCityRequested = {
                        navigation.push(Config.Details(it))
                    },
                    onBackClicked = {
                        navigation.pop()
                    },
                    onCitySavedToFavourite = {
                        //Добавили в избранное и ушли с экрана
                        navigation.pop()
                    },
                    openReason = config.openReason,
                    componentContext = componentContext
                )
                RootComponent.Child.Search(component)
            }
        }
    }

    //Классы этого интерфейса будут участвовать в навигации в передаче параметров, поэтому должны быть сериализуемыми
    @Serializable
    private sealed interface Config {

        @Serializable
        data object Favourite :
            Config //Экран с избранными городами никаких аргументов не принимает, поэтому data object

        @Serializable
        data class Search(val openReason: OpenReason) :
            Config //Экран поиска принмает параметр Причина открытия

        @Serializable
        data class Details(val city: City) :
            Config //Экран детальной информации принимает город, для которого будем загружать прогноз погоды
    }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultRootComponent
    }
}