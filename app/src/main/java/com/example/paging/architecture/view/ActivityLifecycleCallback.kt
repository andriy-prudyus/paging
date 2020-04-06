package com.example.paging.architecture.view

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.paging.App
import com.example.paging.MainActivity
import com.example.paging.R

class ActivityLifecycleCallback : Application.ActivityLifecycleCallbacks {

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        activity.setTheme(R.style.AppTheme)
        (activity.application as App).appComponent.inject(activity as MainActivity)
        activity.supportFragmentManager.fragmentFactory = activity.fragmentFactory
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityResumed(activity: Activity) {}
}