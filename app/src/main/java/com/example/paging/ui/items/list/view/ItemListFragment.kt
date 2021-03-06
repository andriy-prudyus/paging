package com.example.paging.ui.items.list.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.paging.R
import com.example.paging.architecture.adapter.ItemsDecoration
import com.example.paging.architecture.adapter.PagedRecyclerViewAdapter
import com.example.paging.architecture.delegate.AutoClearedValue
import com.example.paging.architecture.state.PagingState
import com.example.paging.architecture.state.State
import com.example.paging.architecture.viewModel.InjectingSavedStateViewModelFactory
import com.example.paging.architecture.viewModel.ObserveSingleResult
import com.example.paging.databinding.FragmentItemListBinding
import com.example.paging.ui.items.list.adapter.ItemListAdapter
import com.example.paging.ui.items.list.dataSource.ItemListDataSource
import com.example.paging.ui.items.list.model.Item
import com.example.paging.ui.items.list.viewModel.ItemListViewModel
import com.example.paging.utils.*

class ItemListFragment(
    private val viewModelFactory: InjectingSavedStateViewModelFactory
) : Fragment(), ItemListAdapter.ActionListener {

    private val viewModel by viewModels<ItemListViewModel> { viewModelFactory.create(this) }

    private var binding by AutoClearedValue<FragmentItemListBinding> { viewLifecycleOwner }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentItemListBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initSwipeRefreshLayout()
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            isMotionEventSplittingEnabled = false
            addItemDecoration(ItemsDecoration(context, null, null, null, R.dimen.item_spacing))

            adapter = ItemListAdapter().apply {
                listener = this@ItemListFragment
            }
        }
    }

    private fun initSwipeRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener(::refresh)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getItems()
    }

    private fun getItems() {
        viewModel.items().observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is State.Loading -> {
                    binding.recyclerView.isVisible = false
                    binding.progressBar.isVisible = true
                    binding.placeholder.isVisible = false
                }
                is State.Success -> {
                    (state.data.dataSource as? ItemListDataSource)?.let {
                        observeLoadInitial(it)
                        observeLoadAfter(it)
                        observeLoadBefore(it)
                    }

                    (binding.recyclerView.adapter as? ItemListAdapter)?.submitList(state.data) {
                        (binding.recyclerView.layoutManager as LinearLayoutManager)
                            .scrollToPositionWithOffset(
                                if (state.data.config.enablePlaceholders) {
                                    viewModel.state.getItemPositionAbsolute()
                                } else {
                                    viewModel.state.getItemPositionRelative()
                                },
                                viewModel.state.getItemTopOffset()
                            )
                    }
                }
                is State.Failure -> {
                    binding.recyclerView.isVisible = false
                    binding.progressBar.isVisible = false
                    binding.placeholder.showError(state.throwable)
                }
            }
        })
    }

    private fun observeLoadInitial(dataSource: ItemListDataSource) {
        dataSource.loadInitial().observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is PagingState.Initial.Loading -> {
                    binding.recyclerView.isVisible = false
                    binding.progressBar.isVisible = true
                    binding.placeholder.isVisible = false
                }
                is PagingState.Initial.Success -> {
                    binding.recyclerView.isVisible = true
                    binding.progressBar.isVisible = false
                    binding.placeholder.isVisible = false
                }
                is PagingState.Initial.Failure -> {
                    binding.recyclerView.isVisible = false
                    binding.progressBar.isVisible = false
                    binding.placeholder.showError(state.throwable)
                }
            }
        })
    }

    private fun observeLoadAfter(dataSource: ItemListDataSource) {
        dataSource.loadAfter().observe(viewLifecycleOwner, Observer { state ->
            val adapter = binding.recyclerView.adapter as? ItemListAdapter

            when (state) {
                is PagingState.After.Loading, is PagingState.After.Success -> {
                    if (adapter?.currentList?.config?.enablePlaceholders == false) {
                        adapter.loadAfterState = state
                    }
                }
                is PagingState.After.Failure -> {
                    if (adapter?.currentList?.config?.enablePlaceholders == false) {
                        adapter.loadAfterState = state
                    } else {
                        view?.let { showErrorSnackbar(it, state.throwable) }
                    }
                }
            }
        })
    }

    private fun observeLoadBefore(dataSource: ItemListDataSource) {
        dataSource.loadBefore().observe(viewLifecycleOwner, Observer { state ->
            val adapter = binding.recyclerView.adapter as? ItemListAdapter

            when (state) {
                is PagingState.Before.Loading, is PagingState.Before.Success -> {
                    if (adapter?.currentList?.config?.enablePlaceholders == false) {
                        adapter.loadBeforeState = state
                    }
                }
                is PagingState.Before.Failure -> {
                    if (adapter?.currentList?.config?.enablePlaceholders == false) {
                        adapter.loadBeforeState = state
                    } else {
                        view?.let { showErrorSnackbar(it, state.throwable) }
                    }
                }
            }
        })
    }

    private fun refresh() {
        viewModel.refresh().let { liveData ->
            liveData.observe(viewLifecycleOwner, object : ObserveSingleResult<Any>(liveData) {
                override fun onChange(state: State<Any>) {
                    when (state) {
                        is State.Success -> binding.swipeRefreshLayout.isRefreshing = false
                        is State.Failure -> {
                            binding.swipeRefreshLayout.isRefreshing = false
                            view?.let { showErrorSnackbar(it, state.throwable) }
                        }
                    }
                }
            })
        }
    }

    override fun onItemClicked(item: Item) {
        findNavController().navigate(ItemListFragmentDirections.toItemDetailsFragment(item.id))
    }

    override fun onPause() {
        super.onPause()

        (binding.recyclerView.adapter as? PagedRecyclerViewAdapter<*, *, *>)?.getState()?.let {
            viewModel.state.savePagingState(it)
        }
    }
}
