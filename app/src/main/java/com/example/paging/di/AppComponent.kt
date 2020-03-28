package com.example.paging.di

import android.content.Context
import com.example.database.di.DatabaseModule
import com.example.network.di.NetworkModule
import com.example.paging.MainActivity
import com.example.paging.ui.items.list.di.ItemListModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ItemListModule::class,
        NetworkModule::class,
        DatabaseModule::class
    ]
)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance applicationContext: Context): AppComponent
    }

    fun inject(activity: MainActivity)
}