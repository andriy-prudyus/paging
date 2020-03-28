package com.example.paging.architecture.state

sealed class State<T : Any> {

    abstract val data: T?

    data class Loading<T : Any>(
        override val data: T? = null
    ) : State<T>()

    data class Success<T : Any>(
        override val data: T
    ) : State<T>()

    data class Failure<T : Any>(
        val throwable: Throwable,
        override val data: T? = null
    ) : State<T>()
}