package net.dankito.deepthought.android.service

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.os.Bundle
import java.util.*
import kotlin.concurrent.schedule


class AppLifeCycleListener : Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    companion object {
        private val MAX_ACTIVITY_TRANSITION_TIME_MS = 2000L
    }


    private var justStartedApp = true

    private var isInForeground = true

    private var activityTransitionTimer: Timer? = null

    private val appCameToForegroundListeners = HashSet<() -> Unit>()
    private val appWentToBackgroundListeners = HashSet<() -> Unit>()

    private val activityResumedListeners = HashSet<(Activity) -> Unit>()



    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        setIsInForegroundValue(true)
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        stopActivityTransitionTimer()

        callActivityResumedListeners(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        justStartedApp = false

        startActivityTransitionTimer()
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
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            // We're in the Background
            setIsInForegroundValue(false)
        }
    }

    private fun startActivityTransitionTimer() {
        activityTransitionTimer?.cancel()

        activityTransitionTimer = Timer()

        activityTransitionTimer?.schedule(MAX_ACTIVITY_TRANSITION_TIME_MS) {
            setIsInForegroundValue(false)
        }
    }

    private fun stopActivityTransitionTimer() {
        activityTransitionTimer?.cancel()
        activityTransitionTimer = null

        setIsInForegroundValue(true)
    }

    private fun setIsInForegroundValue(newIsInForegroundValue: Boolean) {
        if(this.isInForeground != newIsInForegroundValue) {
            this.isInForeground = newIsInForegroundValue

            if(isInForeground) {
                appCameToForegroundListeners.forEach { it() }
            }
            else {
                appWentToBackgroundListeners.forEach { it() }
            }
        }
    }


    fun isAppInForeground(): Boolean {
        return isInForeground
    }

    fun didAppJustStart(): Boolean {
        return justStartedApp
    }


    fun addAppCameToForegroundListener(listener: () -> Unit) {
        appCameToForegroundListeners.add(listener)
    }

    fun removeAppCameToForegroundListeners(listener: () -> Unit) {
        appCameToForegroundListeners.remove(listener)
    }

    fun addAppWentToBackgroundListener(listener: () -> Unit) {
        appWentToBackgroundListeners.add(listener)
    }

    fun removeAppWentToBackgroundListeners(listener: () -> Unit) {
        appWentToBackgroundListeners.remove(listener)
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