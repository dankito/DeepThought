package net.dankito.deepthought.android.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import net.dankito.deepthought.android.service.ActivityParameterHolder
import net.dankito.deepthought.android.service.CurrentActivityTracker
import org.slf4j.LoggerFactory
import javax.inject.Inject


open class BaseActivity : AppCompatActivity() {

    companion object {
        const val ParametersId = "BASE_ACTIVITY_PARAMETERS_ID"

        const val WaitingForResultForIdBundleExtraName = "WAITING_FOR_RESULT_FOR_ID"

        private val log = LoggerFactory.getLogger(BaseActivity::class.java)
    }


    @Inject
    protected lateinit var currentActivityTracker: CurrentActivityTracker

    @Inject
    protected lateinit var parameterHolder: ActivityParameterHolder


    private var waitingForResultWithId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        log.info("Creating Activity $this")
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            this.waitingForResultWithId = savedInstanceState.getString(WaitingForResultForIdBundleExtraName)
        }
    }

    override fun onStart() {
        super.onStart()

        currentActivityTracker.currentActivity = this

        log.info("Started Activity $this")
    }

    override fun onResume() {
        super.onResume()

        currentActivityTracker.currentActivity = this

        log.info("Resumed Activity $this")
    }

    override fun onPause() {
        super.onPause()
        log.info("Paused Activity $this")
    }

    override fun onStop() {
        if(currentActivityTracker.currentActivity == this) {
            currentActivityTracker.currentActivity = null
        }

        super.onStop()
        log.info("Stopped Activity $this")
    }

    override fun onDestroy() {
        getParametersId()?.let { parametersId ->
            parameterHolder.clearParameters(parametersId)
        }

        super.onDestroy()
        log.info("Destroyed Activity $this")
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            outState.putString(WaitingForResultForIdBundleExtraName, null)
            waitingForResultWithId?.let { outState.putString(WaitingForResultForIdBundleExtraName, it) }
        }
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


    protected fun setWaitingForResult(targetResultId: String) {
        waitingForResultWithId = targetResultId

        parameterHolder.clearActivityResults(targetResultId)
    }

    protected fun getAndClearResult(targetResultId: String): Any? {
        val result = parameterHolder.getActivityResult(targetResultId)

        parameterHolder.clearActivityResults(targetResultId)

        return result
    }

}