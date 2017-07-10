package net.dankito.deepthought.android.service.ui

import android.support.v7.app.AppCompatActivity
import javax.inject.Inject


open class BaseActivity : AppCompatActivity() {

    @Inject
    @JvmField
    protected var currentActivityTracker: CurrentActivityTracker? = null


    override fun onResume() {
        super.onResume()

        // in MainActivity first DeepThoughtBackgroundAndroidService has to be started before dependencies can be injected -> currentActivityTracker is not set yet in onResume()
        currentActivityTracker?.currentActivity = this
    }

    override fun onPause() {
        if(currentActivityTracker?.currentActivity == this) {
            currentActivityTracker?.currentActivity = null
        }

        super.onPause()
    }

}