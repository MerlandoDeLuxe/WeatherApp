//package com.example.weatherapp.di
//
//import com.arkivanov.mvikotlin.core.store.StoreFactory
//import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object PresentationModules {
//
//    @Provides
//    @Singleton
//    fun providesStoreFactory() : StoreFactory {
//        return DefaultStoreFactory()
//    }
//}