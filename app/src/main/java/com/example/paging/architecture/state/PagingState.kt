package com.example.paging.architecture.state

import androidx.paging.PageKeyedDataSource.*

sealed class PagingState<K, V> {

    sealed class Initial<K, V> : PagingState<K, V>() {

        class Loading<K, V> : Initial<K, V>()

        class Success<K, V> : Initial<K, V>()

        data class Failure<K, V>(
            val params: LoadInitialParams<K>,
            val callback: LoadInitialCallback<K, V>,
            val throwable: Throwable
        ) : Initial<K, V>()
    }

    sealed class After<K, V> : PagingState<K, V>() {

        class Loading<K, V> : After<K, V>()

        class Success<K, V> : After<K, V>()

        data class Failure<K, V>(
            val params: LoadParams<K>,
            val callback: LoadCallback<K, V>,
            val throwable: Throwable
        ) : After<K, V>()
    }

    sealed class Before<K, V> : PagingState<K, V>() {

        class Loading<K, V> : Before<K, V>()

        class Success<K, V> : Before<K, V>()

        data class Failure<K, V>(
            val params: LoadParams<K>,
            val callback: LoadCallback<K, V>,
            val throwable: Throwable
        ) : Before<K, V>()
    }
}