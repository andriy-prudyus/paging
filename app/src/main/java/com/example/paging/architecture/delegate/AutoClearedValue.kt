package com.example.paging.architecture.delegate

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AutoClearedValue<T : Any>(
    private val lifecycleOwnerProducer: () -> LifecycleOwner
) : ReadWriteProperty<Any, T>, LifecycleObserver {

    private var value: T? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value
            ?: throw IllegalStateException(
                "Should never call auto-cleared-value get when it might not be available"
            )
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        lifecycleOwnerProducer.invoke().lifecycle.addObserver(this)
        this.value = value
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        value = null
    }
}