package com.example.paging.ui.items.list.di

import androidx.lifecycle.ViewModel
import com.example.paging.architecture.viewModel.AssistedSavedStateViewModelFactory
import com.example.paging.di.ViewModelKey
import com.example.paging.ui.items.list.viewModel.ItemListViewModel
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@AssistedModule
@Module(
    subcomponents = [ItemListComponent::class],
    includes = [AssistedInject_ItemListModule::class]
)
abstract class ItemListModule {

    @Binds
    @IntoMap
    @ViewModelKey(ItemListViewModel::class)
    abstract fun bindViewModelFactory(factory: ItemListViewModel.Factory): AssistedSavedStateViewModelFactory<out ViewModel>
}