package com.example.paging.architecture.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.paging.architecture.state.State

abstract class ObserveSingleResult<T : Any>(
    private val liveData: LiveData<State<T>>
) : Observer<State<T>> {

    abstract fun onChange(state: State<T>)

    override fun onChanged(t: State<T>?) {
        when (t) {
            is State.Loading -> onChange(t)
            is State.Success, is State.Failure -> {
                liveData.removeObserver(this)
                onChange(t)
            }
        }
    }
}