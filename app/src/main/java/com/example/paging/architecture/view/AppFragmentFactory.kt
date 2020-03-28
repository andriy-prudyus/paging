package com.example.paging.architecture.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.example.paging.architecture.viewModel.InjectingSavedStateViewModelFactory
import com.example.paging.ui.items.list.adapter.ItemListAdapter
import com.example.paging.ui.items.list.view.ItemListFragment
import javax.inject.Inject

class AppFragmentFactory @Inject constructor(
    private val viewModelFactory: InjectingSavedStateViewModelFactory,
    private val itemListAdapter: ItemListAdapter
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (loadFragmentClass(classLoader, className)) {
            ItemListFragment::class.java -> ItemListFragment(viewModelFactory, itemListAdapter)
            else -> super.instantiate(classLoader, className)
        }
    }
}