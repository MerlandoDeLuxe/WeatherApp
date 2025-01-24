package com.example.weatherapp.presentation.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.example.weatherapp.presentation.details.DetailsComponent
import com.example.weatherapp.presentation.favourite.FavouriteComponent
import com.example.weatherapp.presentation.search.SearchComponent

interface RootComponent {

    //Тип Value - чтобы снаружи можно было подписаться на этот объект
    //Тип конфигурации нигде наружу торчать не должен, он используется только в реализации Root компонента
    //Нигде на вью мы к нему не обращаемся, поэтому указываем в качестве конфига может использоваться любой тип *
    //Второй тип - интерфейс Child, который мы только что создали
    val stack: Value<ChildStack<*, Child>>

    sealed interface Child {

        data class Favourite(val component: FavouriteComponent) : Child
        data class Details(val component: DetailsComponent) : Child
        data class Search(val component: SearchComponent) : Child

    }
}