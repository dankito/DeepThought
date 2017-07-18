package net.dankito.deepthought.android.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import net.dankito.deepthought.android.service.ActivityParameterHolder
import net.dankito.deepthought.android.service.CurrentActivityTracker
import javax.inject.Inject


open class BaseActivity : AppCompatActivity() {

    companion object {
        val ParametersId = "BASE_ACTIVITY_PARAMETERS_ID"
    }


    @Inject
    protected lateinit var currentActivityTracker: CurrentActivityTracker

    @Inject
    protected lateinit var parameterHolder: ActivityParameterHolder


    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        parameterHolder.clearActivityResults() // a new Activity is started -> clear all results of previous activities
    }

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

    override fun onDestroy() {
        getParametersId()?.let { parametersId ->
            parameterHolder.clearParameters(parametersId)
        }

        super.onDestroy()
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