package com.example.paging.architecture.delegate

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import timber.log.Timber
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AutoClearedValue<T : Any> : ReadWriteProperty<Fragment, T>, LifecycleObserver {

    private var value: T? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return value
            ?: throw IllegalStateException(
                "Should never call auto-cleared-value get when it might not be available"
            )
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        thisRef.viewLifecycleOwner.lifecycle.addObserver(this)
        this.value = value
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        Timber.e("onDestroy")
        value = null
    }
}