package net.dankito.deepthought.android.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.ActivityParameterHolder
import net.dankito.deepthought.android.service.CurrentActivityTracker
import net.dankito.deepthought.android.ui.UiStatePersister
import net.dankito.filechooserdialog.service.IPermissionsService
import net.dankito.filechooserdialog.service.PermissionsService
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

    @Inject
    protected lateinit var uiStatePersister: UiStatePersister


    private var waitingForResultWithId: String? = null

    private val registeredPermissionsServices = mutableListOf<IPermissionsService>()


    init {
        AppComponent.component.inject(this)
    }


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

        registeredPermissionsServices.clear()

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


    internal fun setWaitingForResult(targetResultId: String) {
        waitingForResultWithId = targetResultId

        parameterHolder.clearActivityResults(targetResultId)
    }

    protected fun getAndClearResult(targetResultId: String): Any? {
        val result = parameterHolder.getActivityResult(targetResultId)

        parameterHolder.clearActivityResults(targetResultId)

        return result
    }

    protected fun clearAllActivityResults() {
        parameterHolder.clearActivityResults()
    }


    /**
     * Sometimes objects are too large for bundle in onSaveInstance() which would crash application, so save them in-memory as almost always activity gets destroyed but not Application
     */

    protected fun serializeStateToDiskIfNotNull(outState: Bundle, bundleKey: String, state: Any?) {
        uiStatePersister.serializeStateToDiskIfNotNull(outState, bundleKey, state)
    }

    protected fun<T> restoreStateFromDisk(savedInstanceState: Bundle, bundleKey: String, stateClass: Class<T>): T? {
        return uiStatePersister.restoreStateFromDisk(savedInstanceState, bundleKey, stateClass)
    }


    /*          Permissions handling            */

    fun registerPermissionsService(): IPermissionsService {
        val permissionsService = PermissionsService(this)

        registeredPermissionsServices.add(permissionsService)

        return permissionsService
    }

    fun unregisterPermissionsService(permissionsService: IPermissionsService) {
        registeredPermissionsServices.remove(permissionsService)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        registeredPermissionsServices.forEach { permissionsService ->
            permissionsService.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}