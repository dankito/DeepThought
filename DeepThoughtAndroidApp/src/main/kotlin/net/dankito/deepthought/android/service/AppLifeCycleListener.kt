package net.dankito.deepthought.android.service

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.os.Bundle


class AppLifeCycleListener : Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {


    private var justStartedApp = true

    private val activityResumedListeners = HashSet<(Activity) -> Unit>()


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        callActivityResumedListeners(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        justStartedApp = false
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
    }


    override fun onLowMemory() {
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
    }

    override fun onTrimMemory(level: Int) {
    }


    fun didAppJustStart(): Boolean {
        return justStartedApp
    }


    fun addActivityResumedListener(listener: (Activity) -> Unit) {
        activityResumedListeners.add(listener)
    }

    fun removeActivityResumedListener(listener: (Activity) -> Unit) {
        activityResumedListeners.remove(listener)
    }

    private fun callActivityResumedListeners(activity: Activity) {
        activityResumedListeners.forEach { it(activity) }
    }

}