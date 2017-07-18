package net.dankito.deepthought.android.activities

import android.support.v7.app.AppCompatActivity
import net.dankito.deepthought.android.service.CurrentActivityTracker
import javax.inject.Inject


open class BaseActivity : AppCompatActivity() {

    @Inject
    protected lateinit var currentActivityTracker: CurrentActivityTracker


    override fun onStart() {
        super.onStart()

        currentActivityTracker.currentActivity = this
    }

    override fun onStop() {
        if(currentActivityTracker.currentActivity == this) {
            currentActivityTracker.currentActivity = null
        }

        super.onStop()
    }

}