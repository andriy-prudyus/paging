package com.example.paging.utils

import androidx.fragment.app.Fragment
import com.example.paging.architecture.delegate.AutoClearedValue

fun <T : Any> Fragment.autoCleared() = AutoClearedValue<T> { viewLifecycleOwner }