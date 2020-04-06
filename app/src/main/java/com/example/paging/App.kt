package com.example.paging

import android.app.Application
import com.example.paging.architecture.view.ActivityLifecycleCallback
import com.example.paging.di.AppComponent
import com.example.paging.di.DaggerAppComponent
import com.facebook.stetho.Stetho
import timber.log.Timber

class App : Application() {

    val appComponent by lazy { initAppComponent() }

    override fun onCreate() {
        super.onCreate()
        initLogging()
        registerActivityLifecycleCallbacks(ActivityLifecycleCallback())
    }

    private fun initLogging() {
        if (BuildConfig.isLoggingEnabled) {
            Timber.plant(Timber.DebugTree())
            Stetho.initializeWithDefaults(this)
        }
    }

    private fun initAppComponent(): AppComponent {
        return DaggerAppComponent.factory().create(applicationContext)
    }
}