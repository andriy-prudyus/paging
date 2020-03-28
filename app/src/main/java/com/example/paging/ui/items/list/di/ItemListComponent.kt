package com.example.paging.ui.items.list.di

import dagger.Subcomponent

@Subcomponent
interface ItemListComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): ItemListComponent
    }
}