package com.example.weatherapp.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface PresentationModule {

    //Реализацию StoreFactory мы будем создавать самостоятельно, поэтому будем использоать Provide метод
    companion object {
        @Provides
        @ApplicationScope
        fun providesStoreFactory(): StoreFactory {
            return DefaultStoreFactory()
        }
    }
}