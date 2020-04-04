package com.example.paging.ui.items.details.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.paging.R
import com.example.paging.architecture.state.State
import com.example.paging.architecture.viewModel.InjectingSavedStateViewModelFactory
import com.example.paging.databinding.FragmentItemDetailsBinding
import com.example.paging.ui.items.details.viewModel.ItemDetailsViewModel
import com.example.paging.utils.autoCleared
import com.example.paging.utils.load
import com.example.paging.utils.showError
import com.example.paging.utils.showMessage

class ItemDetailsFragment(
    private val viewModelFactory: InjectingSavedStateViewModelFactory
) : Fragment() {

    private val viewModel by viewModels<ItemDetailsViewModel> {
        viewModelFactory.create(this, arguments)
    }

    private var binding by autoCleared<FragmentItemDetailsBinding>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentItemDetailsBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener(this::onNavigationClicked)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onNavigationClicked(view: View) {
        findNavController().popBackStack()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getItemDetails()
    }

    private fun getItemDetails() {
        viewModel.item().observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is State.Loading -> {
                    binding.contentGroup.isVisible = false
                    binding.progressBar.isVisible = true
                    binding.placeholder.isVisible = false
                }
                is State.Success -> {
                    val (item) = state.data

                    if (item == null) {
                        binding.contentGroup.isVisible = false
                        binding.progressBar.isVisible = false
                        binding.placeholder.showMessage(R.string.no_data)
                    } else {
                        binding.imageView.load(item.imageUrl)
                        binding.nameTextView.text = item.name
                        binding.contentGroup.isVisible = true
                        binding.progressBar.isVisible = false
                        binding.placeholder.isVisible = false
                    }
                }
                is State.Failure -> {
                    binding.contentGroup.isVisible = false
                    binding.progressBar.isVisible = false
                    binding.placeholder.showError(state.throwable)
                }
            }
        })
    }
}
