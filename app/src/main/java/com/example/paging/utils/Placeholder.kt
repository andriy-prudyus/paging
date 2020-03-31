package com.example.paging.utils

import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.example.paging.R
import com.example.paging.customView.Placeholder

fun Placeholder.showMessage(@StringRes res: Int) {
    when (res) {
        R.string.no_data -> {
            binding.imageView.apply {
                setImageResource(R.drawable.ic_no_data)
                isVisible = true
            }
        }
        else -> binding.imageView.isVisible = false
    }

    binding.textView.setText(res)
    isVisible = true
}

fun Placeholder.showError(error: Throwable) {
    binding.imageView.apply {
        setImageResource(R.drawable.ic_error)
        isVisible = true
    }

    binding.textView.setText(localizedErrorMessage(error))
    isVisible = true
}