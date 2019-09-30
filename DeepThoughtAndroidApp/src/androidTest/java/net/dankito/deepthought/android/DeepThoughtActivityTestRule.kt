package net.dankito.deepthought.android

import android.app.Activity
import android.content.Intent
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.core.deps.guava.collect.Iterables
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import android.support.test.runner.lifecycle.Stage
import net.dankito.utils.android.ui.activities.ActivityParameterHolder
import net.dankito.utils.windowregistry.android.ui.AndroidWindow


class DeepThoughtActivityTestRule<T : Activity>(activityClass: Class<T>, private val beforeActivityLaunched: (() -> Unit)? = null) : ActivityTestRule<T>(activityClass) {


    private var parameterHolder: ActivityParameterHolder? = null

    private var activityParameters: Any? = null


    override fun beforeActivityLaunched() {
        beforeActivityLaunched?.invoke()

        super.beforeActivityLaunched()
    }


    fun setActivityParameter(parameterHolder: ActivityParameterHolder, activityParameters: Any) {
        this.parameterHolder = parameterHolder
        this.activityParameters = activityParameters
    }

    override fun getActivityIntent(): Intent {
        val intent = super.getActivityIntent()

        activityParameters?.let { parameters ->
            parameterHolder?.let { parameterHolder ->
                val id = parameterHolder.setParameters(parameters)

                intent.putExtra(AndroidWindow.WindowStateIdIntentExtraName, id)
            }
        }

        return intent
    }


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