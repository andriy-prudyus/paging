package com.example.paging.architecture.scope

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlin.coroutines.CoroutineContext

class LifecycleAwareMainScope : CoroutineScope, LifecycleObserver {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun destroy() {
        coroutineContext.cancelChildren()
    }
}