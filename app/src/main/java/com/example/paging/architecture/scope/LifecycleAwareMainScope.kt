package com.example.paging.architecture.scope

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlin.coroutines.CoroutineContext

class LifecycleAwareMainScope : CoroutineScope, LifecycleObserver {

    override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun destroy() {
        coroutineContext.cancelChildren()
    }
}