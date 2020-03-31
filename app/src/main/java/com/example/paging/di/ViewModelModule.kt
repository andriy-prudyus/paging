package com.example.paging.di

import androidx.lifecycle.ViewModel
import com.example.paging.architecture.viewModel.AssistedSavedStateViewModelFactory
import com.example.paging.ui.items.details.viewModel.ItemDetailsViewModel
import com.example.paging.ui.items.list.viewModel.ItemListViewModel
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@AssistedModule
@Module(includes = [AssistedInject_ViewModelModule::class])
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(ItemListViewModel::class)
    abstract fun bindItemListViewModelFactory(
        factory: ItemListViewModel.Factory
    ): AssistedSavedStateViewModelFactory<out ViewModel>

    @Binds
    @IntoMap
    @ViewModelKey(ItemDetailsViewModel::class)
    abstract fun bindItemDetailsViewModeFactory(
        factory: ItemDetailsViewModel.Factory
    ): AssistedSavedStateViewModelFactory<out ViewModel>
}