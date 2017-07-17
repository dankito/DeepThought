package net.dankito.deepthought.android.activities

import android.support.v7.app.AppCompatActivity
import net.dankito.deepthought.android.service.CurrentActivityTracker
import javax.inject.Inject


open class BaseActivity : AppCompatActivity() {

    @Inject
    @JvmField
    protected var currentActivityTracker: CurrentActivityTracker? = null


    override fun onStart() {
        super.onStart()

        // in MainActivity first DeepThoughtBackgroundAndroidService has to be started before dependencies can be injected -> currentActivityTracker is not set yet in onResume()
        currentActivityTracker?.currentActivity = this
    }

    override fun onStop() {
        if(currentActivityTracker?.currentActivity == this) {
            currentActivityTracker?.currentActivity = null
        }

        super.onStop()
    }

}