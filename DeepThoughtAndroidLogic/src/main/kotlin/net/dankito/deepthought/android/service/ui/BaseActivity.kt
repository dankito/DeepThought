package net.dankito.deepthought.android.service.ui

import android.support.v7.app.AppCompatActivity
import javax.inject.Inject


open class BaseActivity : AppCompatActivity() {

    @Inject
    protected lateinit var currentActivityTracker: CurrentActivityTracker


    override fun onResume() {
        super.onResume()

        currentActivityTracker.currentActivity = this
    }

    override fun onPause() {
        if(currentActivityTracker.currentActivity == this) {
            currentActivityTracker.currentActivity = null
        }

        super.onPause()
    }

}