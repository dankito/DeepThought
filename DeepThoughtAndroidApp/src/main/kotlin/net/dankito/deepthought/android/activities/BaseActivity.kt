package net.dankito.deepthought.android.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import net.dankito.deepthought.android.service.ActivityParameterHolder
import net.dankito.deepthought.android.service.CurrentActivityTracker
import org.slf4j.LoggerFactory
import javax.inject.Inject


open class BaseActivity : AppCompatActivity() {

    companion object {
        val ParametersId = "BASE_ACTIVITY_PARAMETERS_ID"

        private val log = LoggerFactory.getLogger(BaseActivity::class.java)
    }


    @Inject
    protected lateinit var currentActivityTracker: CurrentActivityTracker

    @Inject
    protected lateinit var parameterHolder: ActivityParameterHolder


    override fun onCreate(savedInstanceState: Bundle?) {
        log.info("Creating Activity $javaClass")
        super.onCreate(savedInstanceState)

        parameterHolder.clearActivityResults() // a new Activity is started -> clear all results of previous activities
    }

    override fun onStart() {
        super.onStart()

        currentActivityTracker.currentActivity = this

        log.info("Started Activity $javaClass")
    }

    override fun onResume() {
        super.onResume()

        currentActivityTracker.currentActivity = this

        log.info("Resumed Activity $javaClass")
    }

    override fun onPause() {
        super.onPause()
        log.info("Paused Activity $javaClass")
    }

    override fun onStop() {
        if(currentActivityTracker.currentActivity == this) {
            currentActivityTracker.currentActivity = null
        }

        super.onStop()
        log.info("Stopped Activity $javaClass")
    }

    override fun onDestroy() {
        getParametersId()?.let { parametersId ->
            parameterHolder.clearParameters(parametersId)
        }

        super.onDestroy()
        log.info("Destroyed Activity $javaClass")
    }


    protected fun getParameters(): Any? {
        getParametersId()?.let { parametersId ->
            return parameterHolder.getParameters(parametersId) // we're done with activity. remove parameters from cache to not waste any memory
        }

        return null
    }

    private fun getParametersId(): String? {
        return intent?.getStringExtra(ParametersId)
    }

}