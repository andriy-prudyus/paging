package com.example.paging.architecture.state

import androidx.paging.PageKeyedDataSource.*

sealed class PagingState<K, V> {

    sealed class Initial<K, V> : PagingState<K, V>() {

        abstract val params: LoadInitialParams<K>

        data class Loading<K, V>(override val params: LoadInitialParams<K>) : Initial<K, V>()

        data class Success<K, V>(override val params: LoadInitialParams<K>) : Initial<K, V>()

        data class Failure<K, V>(
            override val params: LoadInitialParams<K>,
            val callback: LoadInitialCallback<K, V>,
            val throwable: Throwable
        ) : Initial<K, V>()
    }

    sealed class After<K, V> : PagingState<K, V>() {

        abstract val params: LoadParams<K>

        data class Loading<K, V>(override val params: LoadParams<K>) : After<K, V>()

        data class Success<K, V>(override val params: LoadParams<K>) : After<K, V>()

        data class Failure<K, V>(
            override val params: LoadParams<K>,
            val callback: LoadCallback<K, V>,
            val throwable: Throwable
        ) : After<K, V>()
    }

    sealed class Before<K, V> : PagingState<K, V>() {

        abstract val params: LoadParams<K>

        data class Loading<K, V>(override val params: LoadParams<K>) : Before<K, V>()

        data class Success<K, V>(override val params: LoadParams<K>) : Before<K, V>()

        data class Failure<K, V>(
            override val params: LoadParams<K>,
            val callback: LoadCallback<K, V>,
            val throwable: Throwable
        ) : Before<K, V>()
    }
}