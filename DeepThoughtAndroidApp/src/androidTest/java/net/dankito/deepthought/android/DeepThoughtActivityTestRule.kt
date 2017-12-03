package net.dankito.deepthought.android

import android.app.Activity
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.core.deps.guava.collect.Iterables
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import android.support.test.runner.lifecycle.Stage


class DeepThoughtActivityTestRule<T : Activity>(activityClass: Class<T>) : ActivityTestRule<T>(activityClass) {


    /**
     * Returns the currently displayed Activity.
     * @return
     */
    fun getCurrentActivity(): Activity {
        getInstrumentation().waitForIdleSync()
        val activity = arrayOfNulls<Activity>(1)

        getInstrumentation().runOnMainSync(Runnable {
            val activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
            if (activities.size > 0) {
                activity[0] = Iterables.getOnlyElement(activities)
            }
        })

        return activity[0]!!
    }


}